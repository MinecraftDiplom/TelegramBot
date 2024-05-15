package bot

import bot.MainLogger.Companion.logger
import bot.callbacks.CallbackManager.initMainHandler
import bot.commands.*
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.dice
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.dispatcher.telegramError
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.logging.LogLevel
import com.mongodb.client.model.Filters.eq
import database.MineDB
import database.MongoDB
import database.MongoDB.users
import kotlinx.coroutines.flow.firstOrNull
import models.mongo.MongoUser
import org.koliy82.bot.commands.downloadCommands
import storage.AdminsDB
import storage.ChatWhiteListDB
import utils.chatId
import utils.getTimeMillis


lateinit var telegramBot: Bot

/*
 Main function of
 initialization of all
 components and
 launching the bot
 @param [token] telegram token
*/
fun botStart(token: String) {

    //region init

    MongoDB
    ChatWhiteListDB
    AdminsDB
    MineDB

    //endregion

    telegramBot = bot {
        logLevel = LogLevel.Error
        this.token = token
        timeout = 30
        dispatch {
            initMainHandler()

            //region commands

            brakCommands()
            utilsCommands()
            minecraftCommands()
            subscribeCommands()
            adminCommands()
            downloadCommands()

            //endregion

            //region logging

            dice {
                bot.deleteMessage(message.chatId, message.messageId)
                logger.trace("[DELETE] (${getTimeMillis()}) @${message.from?.username} [${dice.emoji.emojiValue}]")
            }

            message {
                if (
                    message.chat.type != "private" &&
                    ChatWhiteListDB.isWhitelisted(message.chat.id).not() && ChatWhiteListDB.isEnable
                ) {
                    logger.warn("[${message.chat.id}] Not in whitelisted chat")
                    bot.sendMessage(
                        message.chatId,
                        "Бот разработан для [стримера aratossik](https://t.me/aratosssikchat), если вы хотите добавить бота в свой чат напишите [разработчику бота](https://t.me/koliy822).",
                        parseMode = ParseMode.MARKDOWN
                    )
                    bot.leaveChat(message.chatId)
                    return@message
                }
//                val urls = RuleDB.check(message)
//                if(urls.isNotEmpty()){
//                    bot.copyMessage(ChatId.fromId(GroupIds.logs.id), ChatId.fromId(message.chat.id), message.messageId)
//                    bot.deleteMessage(ChatId.fromId(message.chat.id), message.messageId)
//                    bot.sendMessage(ChatId.fromId(GroupIds.logs.id), text = "[#AUTOMOD] обнаружена ссылка из бан-листа: ${urls.joinToString(separator = ", ")}")
//                    return@message
//                }
                val user = users.collection.find(eq("id", message.from?.id ?: -1L)).firstOrNull()
                user?.let {
                    if (it.isEquals(message.from).not()) {
                        users.update(
                            eq("id", message.from?.id ?: -1L),
                            MongoUser.update(message.from ?: return@message, it.braksCount)
                        ).apply { println("edit user @${message.from} id = $this") }
                    }
                } ?: run {
                    users.add(MongoUser.create(message.from ?: return@message))
                        .apply { println("add user @${message.from} id = $this") }
                }
                MainLogger.saveMessage(message)
            }

            //endregion

            telegramError {
                MainLogger.telegramError("[${error.getType()}] ${error.getErrorMessage()}")
            }

        }
    }

    telegramBot.startPolling().also {
        subscribeCheckerStart()
        logger.info("Telegram Bot started.")
    }

}
