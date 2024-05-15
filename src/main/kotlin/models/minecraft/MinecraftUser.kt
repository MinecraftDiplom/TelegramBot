package models.minecraft

import com.github.kotlintelegrambot.entities.ChatId
import database.MineDB.connection
import utils.block
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit


data class MinecraftUser(
    val user_ID: Int,
    var username: String,
    var password: String,
    var telegram_id: String,
    var register_at: Date,
    var subscribe_end: Date?,
    var uuid: String? = null,
    var accessToken: String? = null,
    var serverID: String? = null,
){
    val isSubscribed: Boolean
        get() {
            val count = connection.prepareStatement("select count(*) from user_permissions where uuid = ?").use { query->
                query.setString(1, uuid)

                val result = query.executeQuery()
                result.next()

                result.getInt("count")
            }
            return count != 0
        }

    val hasSubscription:Boolean
        get() {
            return getSubscriptionDays() != null
        }

    val chatId:ChatId
        get() {
            return ChatId.fromId(telegram_id.toLong())
        }

    /**
     * @return null если нет ИЛИ закончилось, и days если есть.
     */
    fun getSubscriptionDays() : Long? {
        val daysUntil = subscribe_end?.let {
            val days = TimeUnit.MILLISECONDS.toDays(it.time - Instant.now().toEpochMilli())
            if(days < 0){
                null
            }else{
                days
            }
        }
        return daysUntil
    }

    fun profile(): String {
        val daysUntil = getSubscriptionDays()
        return """
            Ваш профиль:
            Ник: $username
            Дата регистрации: ${SimpleDateFormat("dd MMMM yyyy", Locale("ru")).format(register_at)}
            ${if(daysUntil != null) "Подписка - осталось $daysUntil дней" else "НЕТ ПОДПИСКИ"}
        """.trimIndent()
    }

    fun subscribe(): Boolean{
        if(isSubscribed) return false
        connection.prepareStatement("select count(*) from user_permissions where uuid = ?").use { check->
            check.setString(1, uuid)
            val result = check.executeQuery()
            result.next()
            val count = result.getInt(1)
            if(count == 0){
                connection.prepareStatement("INSERT INTO user_permissions (uuid, name) VALUES (?, '*');").use { query->
                    query.setString(1, uuid)
                    query.execute()
                }
            }
        }
        return true
    }

    fun unsubscribe(): Boolean{
        if(!isSubscribed) return false
        connection.prepareStatement("DELETE FROM user_permissions WHERE uuid = ?;").use { query->
            query.setString(1, uuid)
            query.execute()
        }
        return true
    }

    fun newPassword(password: String){
        connection.prepareStatement("UPDATE users SET password = ? WHERE telegram_id = ?;").use { query ->
            query.setString(1, password)
            query.setString(2, telegram_id)
            query.execute()
        }
    }

    fun addSubscriptionDays(days:Int){
        val newSubscriptionDate = block {
            val currentStamp = if (hasSubscription) {
                Timestamp.from((subscribe_end!!).toInstant())
            } else {
                Timestamp.valueOf(LocalDateTime.now())
            }
            return@block currentStamp.toLocalDateTime().plusDays(days.toLong())
        }

        block {
            val query = connection.prepareStatement("update users set subscribe_end = ? where telegram_id = ?")
            query.setTimestamp(1, Timestamp.valueOf(newSubscriptionDate))
            query.setString(2, telegram_id)
            query.execute()

            subscribe()
        }
    }
    companion object{
        fun getFromDatabase(telegram_id: Long) : MinecraftUser? {
            connection.prepareStatement("select * from users where telegram_id = ?;").use { query  ->
                query.setString(1, "$telegram_id")
                val result = query.executeQuery()

                if(result.next()){
                    return MinecraftUser(
                        user_ID = result.getInt("user_ID"),
                        username = result.getString("username"),
                        password = result.getString("password"),
                        telegram_id = result.getString("telegram_id"),
                        register_at = result.getTimestamp("register_at"),
                        uuid = result.getString("uuid"),
                        accessToken = result.getString("accessToken"),
                        subscribe_end = result.getTimestamp("subscribe_end"),
                        serverID = result.getString("serverID"),
                    )
                }
            }
            return null
        }
    }
}
