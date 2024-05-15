package models.braks

import com.github.kotlintelegrambot.entities.User
import storage.UsersDB
import utils.getTimeMillis

data class Babyold(
    var userID: Long? = null,
    var time: Long? = null
){
    fun user() = UsersDB.getFromUserID(userID)

    fun timeToString():String{
        val time = getTimeMillis() - (time ?: 0L)
        val seconds = time / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val years = days / 365
        return when {
            years > 0 -> "$years лет"
            days > 0 -> "$days дней"
            hours > 0 -> "$hours ч. ${minutes%60} мин."
            minutes > 5 -> "$minutes мин. ${seconds%60} сек."
            else -> "Новорождённый"
        }
    }

    fun stat():String{
        return "@${user()?.username} [${timeToString()}]"
    }

    companion object{
        fun create(userID: Long):Babyold{
            return Babyold(userID, getTimeMillis())
        }
        fun create(user: User):Babyold{
            return Babyold(user.id, getTimeMillis())
        }
    }
}