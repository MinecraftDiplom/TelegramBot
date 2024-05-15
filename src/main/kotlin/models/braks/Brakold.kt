package models.braks

import storage.BraksDB
import storage.UsersDB
import utils.getTimeMillis

data class Brakold(
    var firstUserID: Long? = null,
    var secondUserID: Long? = null,
    var baby: Babyold? = null,
    var time: Long? = null
){
    fun firstUser() = UsersDB.getFromUserID(firstUserID)
    fun secondUser() = UsersDB.getFromUserID(secondUserID)

    fun partner(userID: Long) = UsersDB.getFromUserID(partnerID(userID))

    fun partnerID(userID: Long):Long?{
        return if(firstUserID == userID) secondUserID else firstUserID
    }

    fun isBabyCreate():Boolean{
        return baby != null
    }

    fun timeToString():String{
        val time = getTimeMillis() - (time?:0L)
        val seconds = time / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val years = days / 365
        return when {
            years > 0 -> "$years лет"
            days > 0 -> "$days дн. ${hours%24} ч."
            hours > 0 -> "$hours ч. ${minutes%60} мин."
            minutes > 5 -> "$minutes мин. ${seconds%60} сек."
            else -> "Молодожёны"
        }
    }

    fun create(firstUserID: Long, secondUserID: Long):Brakold{
        return Brakold(firstUserID, secondUserID, null, getTimeMillis())
    }

    fun getIndex():Int{
        return BraksDB.list.indexOf(this)
    }

}
