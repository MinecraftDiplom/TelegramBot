package models.mongo

import com.github.kotlintelegrambot.entities.User
import com.mongodb.client.model.Filters.*
import database.MongoDB.messages
import database.MongoDB.payments
import database.MongoDB.users
import models.minecraft.MinecraftUser
import org.bson.types.ObjectId
import utils.cut
import utils.toForm
import java.io.InputStream

data class MongoUser(
    var id: Long,
    var isBot: Boolean,
    var firstName: String,
    var lastName: String? = null,
    var username: String? = null,
    var languageCode: String? = null,
    var braksCount: Int = 0,
    var _id: ObjectId? = null,
) {

    suspend fun messageCount() =
        messages.collection.countDocuments(and(eq("from.id", this.id), not(eq("chat.type", "private"))))

    suspend fun messageCountLocal(chatId: Long) =
        messages.collection.countDocuments(and(eq("chat.id", chatId), eq("from.id", this.id)))

    suspend fun subsCount() = payments.collection.countDocuments(
        and(
            eq("from.id", this.id),
            eq("payment.invoicePayload", "1")
        )
    ) + (payments.collection.countDocuments(and(eq("from.id", this.id), eq("payment.invoicePayload", "3"))) * 3)

    suspend fun profile(chatId: Long): String {

        val username = "${this.firstName} ${this.lastName ?: ""}".trim().cut(30, "\\.\\.\\.")

        val lines = mutableListOf(
            "[*\uD83D\uDCAC Пользователь*](tg://user?id=${this.id})",
            "Ник: `$username`",
            "Сообщений в группе: ${messageCountLocal(chatId)}",
            "Всего сообщений: ${messageCount()}",
            "Было браков: $braksCount\n",
            "[*\uD83E\uDDCA Майнкрафт*](https://t.me/kissotaru)"
        )

        val mineUser = MinecraftUser.getFromDatabase(this.id)

        if (mineUser == null) {
            lines += "Нет аккаунта \uD83E\uDD7A"
            lines += "Зарегистрируйся в лс /register"
        } else {
            lines += "Логин: `${mineUser.username}`"
            val sub = mineUser.getSubscriptionDays()
            lines += if (sub == null) "Подписка: Нет" else "Подписка: $sub дней"
            val count = subsCount()
            lines += if (count == 0L) "У меня 0 на балансе" else "Купил подписок: $count \uD83D\uDD25"
        }

        return lines.toForm(9, "\uD83C\uDF5E", "\uD83C\uDF5E")

    }

    fun isEquals(user: User?): Boolean {
        user ?: return false

        if (firstName != user.firstName) return false
        if (lastName != user.lastName) return false
        if (username != user.username) return false
        if (languageCode != user.languageCode) return false

        return true
    }


    companion object {
        fun create(user: User): MongoUser {
            return MongoUser(
                user.id,
                user.isBot,
                user.firstName,
                user.lastName,
                user.username,
                user.languageCode
            )
        }

        fun update(user: User, braksCount: Int): MongoUser {
            return MongoUser(
                user.id,
                user.isBot,
                user.firstName,
                user.lastName,
                user.username,
                user.languageCode,
                braksCount
            )
        }

        fun readCSV(inputStream: InputStream): List<MongoUser> {
            val reader = inputStream.bufferedReader()
            val header = reader.readLine()
            return reader.lineSequence()
                .filter { it.isNotBlank() }
                .map {
                    val fields = it.split(',', ignoreCase = false, limit = 13)
                    println(it)
                    MongoUser(
                        _id = null,
                        id = fields[1].toLong(),
                        isBot = fields[2].toBoolean(),
                        firstName = fields[3],
                        lastName = fields[4],
                        username = fields[5],
                        languageCode = fields[6],
                        braksCount = fields[7].toInt()
                    )
                }.toList()
        }

        suspend fun getFromUserID(userID: Long): MongoUser? {
            return users.find(eq("id", userID))
        }

        suspend fun getFromUser(user: User?): MongoUser? {
            return getFromUserID(user?.id ?: return null)
        }
    }
}