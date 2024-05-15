package models.mongo

import com.github.kotlintelegrambot.entities.User
import com.mongodb.client.model.Filters.eq
import database.MongoDB.users
import utils.getTimeLocal
import utils.getTimeMillis
import java.time.temporal.ChronoUnit

data class MongoBaby(
    var userID: Long? = null,
    var time: Long? = null
){
    suspend fun user() = users.find(eq("id", userID))

    suspend fun username() = user()?.username

    suspend fun statistic():String{
        return "@${username()} [${timeToString()}]"
    }
    fun timeToString():String{
        val now = getTimeLocal(getTimeMillis())
        val started = getTimeLocal(time?:0L)
        val seconds = ChronoUnit.SECONDS.between(started, now)
        val minutes = ChronoUnit.MINUTES.between(started, now)
        val hours = ChronoUnit.HOURS.between(started, now)
        val days = ChronoUnit.DAYS.between(started, now)
        val month = ChronoUnit.MONTHS.between(started, now)
        val years = ChronoUnit.YEARS.between(started, now)

        return when {
            years > 0 -> "$years лет"
            month > 0 -> "$month мес."
            days > 9 -> "$days дней"
            days > 0 -> "$days дн. ${hours%24} ч."
            hours > 0 -> "$hours ч. ${minutes%60} мин."
            minutes > 5 -> "$minutes мин. ${seconds%60} сек."
            minutes >= 1 -> "$minutes минут"
            else -> "Молодожёны"
        }
    }
    companion object{
        fun create(userID: Long): MongoBaby {
            return MongoBaby(userID, getTimeMillis())
        }
        fun create(user: User): MongoBaby {
            return MongoBaby(user.id, getTimeMillis())
        }
    }
}