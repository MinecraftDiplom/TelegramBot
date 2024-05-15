package org.koliy82.bot.commands

import bot.MainLogger
import bot.callbacks.CallbackManager
import bot.callbacks.ContextType
import bot.commandMenu
import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.TelegramFile
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import models.telegram.buttons.Buttons
import utils.chatId
import java.net.URL


fun Dispatcher.downloadCommands(){

    commandMenu("mobile", "📱 Приложение (apk)"){
        val callback = CallbackManager.dynamicQuery(
            "MineProfile", ContextType.ONE_CLICK, null
        ){
            MainLogger.logger.trace("${callbackQuery.from.username} скачивает мобильное приложение")
            val scope = CoroutineScope(Dispatchers.Default)
            scope.launch {
                try{
                    val url = URL("http://kissota.ru:9000/files/download/apk")
                    val imageData = url.readBytes()
                    bot.sendDocument(
                        chatId = ChatId.fromId(callbackQuery.from.id),
                        caption = "Для загрузки мобильного приложения скачайте данный файл и следуйте дальнейшей инструкции.",
                        document = TelegramFile.ByByteArray(imageData, filename = "MineProfile-arm64.apk")
                    )
                }catch (e: Exception){
                    bot.sendMessage(chatId = ChatId.fromId(callbackQuery.from.id), e.message.toString())
                }
            }
        }
        bot.sendMessage(
            chatId = ChatId.fromId(message.chat.id),
            text = "Для загрузки мобильного приложения нажмите на кнопку ниже.",
            replyMarkup = InlineKeyboardMarkup.createSingleRowKeyboard(InlineKeyboardButton.CallbackData(callback.label ?: "", callback.data)),
        )
    }

    commandMenu("download", "🖥 Лаунчер (.exe/.jar)"){
        bot.sendMessage(message.chatId, "Для загрузки лаунчера нажмите на кнопку ниже.", replyMarkup = Buttons.download.buttons)
    }

}