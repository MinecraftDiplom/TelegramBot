package org.koliy82

import bot.MainLogger.Companion.logger
import bot.botStart
import com.mongodb.client.model.Filters.eq
import database.MongoDB.users
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import models.mongo.MongoUser
import models.telegram.ids.UserIds
import storage.KeyManager
import storage.Keys
import utils.consoleStart
import java.io.File

fun main() {
    logger.info(System.getProperty("os.name"))
    botStart(KeyManager.getKey(Keys.TelegramTest))
    consoleStart()
}

@Serializable
data class MongoUserDTO(
    val user: UserDTO,
    var braksCount: Int = 0,
    var avatar: PhotoSizeDTO? = null,
    var _id: ObjectIdDTO? = null,
)
@Serializable
data class ObjectIdDTO(
    @SerialName("\$oid")
    val oid: String
)

@Serializable
data class UserDTO(
    val id: Long,
    val isBot: Boolean,
    val firstName: String,
    val lastName: String? = null,
    val username: String? = null,
    val languageCode: String? = null,
    val canJoinGroups: Boolean? = null,
    val canReadAllGroupMessages: Boolean? = null,
    val supportsInlineQueries: Boolean? = null,
)
@Serializable
data class PhotoSizeDTO(
     val fileId: String,
     val fileUniqueId: String,
     val width: Int,
     val height: Int,
     val fileSize: Int? = null,
)


private class codeTest {

    companion object {
        @JvmStatic
        fun main(args: Array<String>): Unit = runBlocking {
            val file = File("C:\\Users\\rutop\\Рабочий стол\\aratossik.users.json")
            val json = Json.decodeFromString<List<MongoUserDTO>>(file.readText())
            val usersList = json.map {
                MongoUser(
                    it.user.id,
                    it.user.isBot,
                    it.user.firstName,
                    it.user.lastName,
                    it.user.username,
                    it.user.languageCode,
                    it.braksCount,
                    null,
                )
            }
//            println(usersList.firstOrNull())
//            users.collection.insertMany(usersList)
            println(users.collection.find(eq("id", UserIds.koliy82.id)).toList())
//            runBlocking {
//                MongoUser.getFromUserID(userId)?.let {
//                    val profile = it.profile(groupId)
//                    println(profile)
//                    telegramBot.sendMessage(ChatId.fromId(UserIds.koliy82.id), profile, parseMode = ParseMode.MARKDOWN_V2).apply {
//                        println(this)
//                    }
//                }
//            }
//            runBlocking {
//                val test = messages.collection.distinct<Long>("chat.id").toList()
//                println(test)
            //val test = messages.collection.find().toList()
//                test.forEach {
//                    val count = messages.collection.countDocuments(eq("chat.id", it))
//                    val message = messages.find(eq("chat.id", it))
//                    println(it)
//                    println(message?.chat?.title)
//                    println(count)
//                }
//                messages.collection.find(eq("chat.id", 6025395983)).toList().forEach {
//                    println(it)
//                }
//            }
        }
    }
}