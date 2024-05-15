package bot.callbacks

import com.github.kotlintelegrambot.dispatcher.handlers.CallbackQueryHandlerEnvironment
import com.github.kotlintelegrambot.entities.ChatId
import com.mongodb.client.model.Filters
import database.MongoDB
import models.mongo.MongoBaby
import models.mongo.MongoUser
import org.bson.types.ObjectId
import utils.getTimeMillis
import utils.timeToString

data class MongoBrakDTO(
    var firstUserID: Long? = null,
    var secondUserID: Long? = null,
    var baby: MongoBaby? = null,
    var firstUser: MongoUser? = null,
    var secondUser: MongoUser? = null,
    var babyUser: MongoUser? = null,
    var messageCount: Long? = null,
    var firstUserMessages: Long? = null,
    var secondUserMessages: Long? = null,
    var time: Long? = getTimeMillis(),
    var _id: ObjectId? = null,
) {
    suspend fun messageCount() = MongoDB.messages.collection.countDocuments(
        Filters.and(
            Filters.or(
                Filters.eq(
                    "from.id",
                    firstUserID
                ), Filters.eq("from.id", secondUserID)
            ), Filters.not(Filters.eq("chat.type", "private"))
        )
    )

    suspend fun messageCountLocal(chatId: Long) = MongoDB.messages.collection.countDocuments(
        Filters.and(
            Filters.eq(
                "chat.id",
                chatId
            ), Filters.or(Filters.eq("from.id", firstUserID), Filters.eq("from.id", secondUserID))
        )
    )

    fun statistic(index: Int): String {
        return "${index}. <a href=\"https://t.me/${firstUser?.username ?: ""}\">${firstUser?.username ?: ""}</a> + <a href=\"https://t.me/${secondUser?.username ?: ""}\">${secondUser?.username ?: ""}</a> (${time.timeToString()} - ${(firstUserMessages ?: 0L) + (secondUserMessages ?: 0L)} сообщений)"
    }
}

class Pages(
    val chatId: Long,
    val ownerIds: List<Long>? = null,
    val limit: Int = 10,
) {
    val pages = mutableListOf<MessageBox>()
    var currentPage: Int = 0
    var curMessageBox: MessageBox = MessageBox(ChatId.fromId(chatId))

    fun fillPages(lines: List<MongoBrakDTO>, newBox: () -> MessageBox) {

        var index = 1

        lines.filter { it.messageCount != 0L }
//                    .sortedByDescending {it.messageCount}
            .chunked(limit).map { pageList ->
                val page = newBox()
                pageList.map {
                    page.text += it.statistic(index++) + "\n"
                }
                pages += page
            }

        if (pages.size > 1) {
            curMessageBox.button("⬅️", ContextType.TEMPORARY, ownerIds) {
                backPage()
                update()
            }
            curMessageBox.button("${currentPage + 1}", ContextType.TEMPORARY) {
                answer("Страница №${currentPage + 1} (На ней же не написано)")
            }
            curMessageBox.button("➡️", ContextType.TEMPORARY, ownerIds) {
                nextPage()
                update()
            }
        }

    }

    fun CallbackQueryHandlerEnvironment.answer(text: String) {
        CallbackManager.callbacks[callbackQuery.data]?.answerText = text
    }

    fun getPage(): MessageBox {
        return pages[currentPage]
    }

    fun nextPage() {
        if (currentPage == pages.size - 1) currentPage = 0
        else currentPage++
    }

    fun backPage() {
        if (currentPage == 0) currentPage = pages.size - 1
        else currentPage--
    }

    fun update() {
        if (pages.isEmpty()) return
        val msgbox = getPage()
        curMessageBox.text = ""

        curMessageBox.buttons.getOrNull(1)?.label = "${currentPage + 1}"

        curMessageBox.let {
            it.parseType = msgbox.parseType
            it.buttons += msgbox.buttons
            it.text += msgbox.text
        }

        if (curMessageBox.lastSendedId == 0L)
            curMessageBox.sendApplied()
        else
            curMessageBox.editApplied()
    }

}

data class Page(var messageBox: MessageBox)