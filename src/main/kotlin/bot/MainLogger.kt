package bot

import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Message
import database.MongoDB
import models.mongo.MongoMessage
import models.telegram.ids.UserIds
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class Log {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass)
}

class MainLogger {
    companion object : Log() {
        fun telegramError(message: String) {
            logger.error(message)
            telegramBot.sendMessage(ChatId.fromId(UserIds.koliy82.id), message)
        }

        suspend fun saveMessage(message: Message){
            logger.trace(message.text)
            MongoDB.messages.add(MongoMessage.fromMessage(message))
        }
    }

}