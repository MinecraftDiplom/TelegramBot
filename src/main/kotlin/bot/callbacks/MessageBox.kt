package bot.callbacks

import bot.Messages.Button
import bot.telegramBot
import com.github.kotlintelegrambot.dispatcher.handlers.HandleCallbackQuery
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.types.TelegramBotResult
import database.MongoDB.braks
import kotlinx.coroutines.flow.toList
import models.mongo.MongoBrak
import java.util.concurrent.TimeUnit

fun msgbox(chatId: ChatId, block: MessageBox.()->Unit) : MessageBox {
    return MessageBox(chatId).apply(block)
}

private val menus = mutableMapOf<Long, BraksMenu>()

data class BraksMenu(
    var chatId:ChatId,
    var limit:Int = 10,
    val grantedUserIDs: MutableList<Long> = mutableListOf(),
    var lastSendedMessageId:Long = 0,
    var callbackID:Int = 0,
    var disableWebPagePreview: Boolean = true,
    var parseType: ParseMode = ParseMode.MARKDOWN_V2,
){
    val buttons = mutableListOf<Button>()
    var index = 0
    var id = 0
    suspend fun currentPage(): List<MongoBrak> = braks.collection.find().skip(skip = (index) * limit).limit(limit = limit).partial(true).toList()
    suspend fun previousPage(): List<MongoBrak> = navigate(-1)
    suspend fun nextPage(): List<MongoBrak> = navigate(1)

    private suspend fun navigate(direction: Int): List<MongoBrak> {
        index += direction
        val pages = (braks.collection.countDocuments()/10L).toInt()
        index = when {
            index > pages -> 0
            index < 0 -> pages
            else -> index
        }
        return currentPage()
    }

    suspend fun loadPage(page: Int): List<MongoBrak> {
        index = page
        return currentPage()
    }

    suspend fun send(){
        var i = 1
        var text = "Браки:\n"
        loadPage(index).forEach {
            text += it.statistic(i++)+"\n"
        }
        telegramBot.sendMessage(chatId, text,
            replyMarkup = InlineKeyboardMarkup.createSingleRowKeyboard(buttons.buttonsBuild() ?: return),
            parseMode = parseType, disableWebPagePreview = disableWebPagePreview).apply {
            this.getOrNull()?.let { message ->
                menus[message.messageId] = this@BraksMenu
            }
        }

    }

    private fun List<Button>.buttonsBuild(): List<InlineKeyboardButton>?{
        if(this.isEmpty()) return null
        val buttonsBuild = mutableListOf<InlineKeyboardButton>()
        this.forEach { button ->
            buttonsBuild += InlineKeyboardButton.CallbackData(
                text = button.label,
                callbackData = "${callbackID}:${button.data}"
            )
        }
        return buttonsBuild
    }

    fun edit(){

    }

    fun grant(user:User){
        grantedUserIDs += user.id
    }

    fun button(label:String, tag:String, onClicked: HandleCallbackQuery){
        buttons += Button(label, tag, onClicked)
    }
}

data class MessageBox(
    var chatId: ChatId,
    var text: String = "",
    var parseType: ParseMode? = null,
    var deleteTime: Long = 60,
    var singleRowMode: Boolean = true,
    var disableWebPagePreview: Boolean = true,
) {
    var id: Int = 0
    val buttons = mutableListOf<Callback>()
    var lastSendedId:Long = 0

    fun button(label:String, type: ContextType, ownerIds: List<Long>? = null, answerText: String? = null, onClicked: HandleCallbackQuery){
        buttons += CallbackManager.dynamicQuery(
           label, type, ownerIds, deleteTime, answerText, onClicked
        )
    }

//    fun sendApplied(query: CallbackQuery?){
//        query?.message?.apply {
//            sendApplied(this.chat.id)
//        }
//    }

    fun sendApplied(){
        if(buttons.isNotEmpty()){
            addToCache()
            CallbackManager.eraser.schedule({
                remove()
            }, deleteTime, TimeUnit.MINUTES)
        }
        sendMessage()
    }

    fun editApplied(){
        if(buttons.isNotEmpty()){
            addToCache()
            CallbackManager.eraser.schedule({
                remove()
            }, deleteTime, TimeUnit.MINUTES)
        }
        editMessage()
    }

    fun addToCache(){
        id = CallbackManager.NEXT_ID
        CallbackManager.boxes += this
    }

    fun keyboardBuild(): InlineKeyboardMarkup? {
        if(singleRowMode)
            return InlineKeyboardMarkup.createSingleRowKeyboard(buttons.buttonsBuild() ?: return null)
        else
            return InlineKeyboardMarkup.create(buttons.twoModeButtonsBuild() ?: return null)
    }

    fun List<Callback>.buttonsBuild(): List<InlineKeyboardButton>?{
        if(this.isEmpty()) return null
        val buttonsBuild = mutableListOf<InlineKeyboardButton>()
        this.forEach { button ->
            buttonsBuild += InlineKeyboardButton.CallbackData(
                text = button.label?:"",
//                callbackData = "${id}:${button.tag}"
                callbackData = button.data
            )
        }
        return buttonsBuild
    }

    fun List<Callback>.twoModeButtonsBuild(): List<List<InlineKeyboardButton>>? {
        if(this.isEmpty()) return null
        val buttonsBuild = mutableListOf<List<InlineKeyboardButton>>()
        this.forEach { button ->
            buttonsBuild += listOf(
                InlineKeyboardButton.CallbackData(
                    text = button.label?:"",
//                    callbackData = "${id}:${button.tag}"
                    callbackData = button.data
                )
            )
        }
        return buttonsBuild
    }

    fun sendMessage(){
        telegramBot.sendMessage(chatId, text, replyMarkup = keyboardBuild(), parseMode = parseType, disableWebPagePreview = disableWebPagePreview, disableNotification = true).writeID()
    }

    fun sendMessage(chatID:Long){
        chatId = ChatId.fromId(chatID)
        telegramBot.sendMessage(chatId, text, replyMarkup = keyboardBuild(), parseMode = parseType, disableWebPagePreview = disableWebPagePreview, disableNotification = true).writeID()
    }

    fun editMessage(){
        telegramBot.editMessageText(chatId, lastSendedId, text = text, replyMarkup = keyboardBuild(), parseMode = parseType, disableWebPagePreview = disableWebPagePreview)
    }

    fun answerMessage(text:String){
        telegramBot.sendMessage(chatId, text, replyToMessageId = lastSendedId, disableNotification = true)
    }


    //Записывает последний ID сообщения
    private fun TelegramBotResult<Message>.writeID(){
        apply {
            if(this.isError)
                println(this)
            this.getOrNull()?.apply {
                this@MessageBox.lastSendedId = this.messageId
            }
        }
    }

    fun remove() {
        CallbackManager.boxes -= this
    }
}