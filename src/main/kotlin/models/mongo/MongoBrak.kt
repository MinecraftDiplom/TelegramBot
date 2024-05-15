package models.mongo

import com.github.kotlintelegrambot.entities.User
import com.mongodb.client.model.Filters.*
import database.MongoDB
import database.MongoDB.braks
import database.MongoDB.users
import org.bson.types.ObjectId
import utils.getTimeMillis
import utils.timeToString
import utils.toDownLineForm

data class MongoBrak(
    var firstUserID: Long? = null,
    var secondUserID: Long? = null,
    var baby: MongoBaby? = null,
    var time: Long? = getTimeMillis(),
    var _id: ObjectId? = null,
){
    suspend fun firstUser() = users.find(eq("id", firstUserID))
    suspend fun secondUser() = users.find(eq("id", secondUserID))
    suspend fun firstUsername() = firstUser()?.username
    suspend fun secondUsername() = secondUser()?.username

    suspend fun messageCount() = MongoDB.messages.collection.countDocuments(and(or(eq("from.id", firstUserID), eq("from.id", secondUserID)), not(eq("chat.type", "private"))))
    suspend fun messageCountLocal(chatId: Long) = MongoDB.messages.collection.countDocuments(and(eq("chat.id", chatId), or(eq("from.id", firstUserID), eq("from.id", secondUserID))))

    private fun partnerID(userID: Long) = if(firstUserID == userID) secondUserID else firstUserID
    suspend fun partner(userID: Long) = users.find(eq("id", partnerID(userID)))

     suspend fun statistic(index:Int):String{
        val firstUser = firstUser()
        val secondUser = secondUser()
        return "${index}. <a href=\"https://t.me/${firstUser?.username}\">${firstUser?.firstName} ${firstUser?.lastName?:""}</a> + <a href=\"https://t.me/${secondUser?.username}\">${secondUser?.firstName} ${secondUser?.lastName ?: ""}</a> (${time.timeToString()} - ${messageCount()} сообщений)"
    }

    fun isBabyCreate():Boolean{
        return baby != null
    }

    fun create(firstUserID: Long, secondUserID: Long): MongoBrak {
        return MongoBrak(firstUserID, secondUserID)
    }

    suspend fun getIndex():Int?{
        return braks.find(eq("_id", _id)).hashCode()
    }

    suspend fun removeBaby() {
        this.baby = null
        braks.collection.findOneAndReplace(eq("_id", _id), this)
    }

    suspend fun createBaby(id: Long) {
        baby = MongoBaby.create(id)
        braks.collection.findOneAndReplace(eq("_id", _id), this)
    }

    suspend fun toForm(chatId: Long): String {
       return listOf(
            "[Брак - ${time.timeToString()}]",
            "❤\uFE0F\u200D\uD83D\uDD25 - @${firstUsername()} и @${secondUsername()}",
            if(baby==null) "Бэбик ещё не родился" else "\uD83D\uDC7C - ${baby?.statistic()}",
            "\uD83D\uDCAC - ${messageCountLocal(chatId)} сообщений"
        ).toDownLineForm(9, "♣\uFE0F", "♠\uFE0F")
    }

    companion object{
        suspend fun getFromUserID(userID: Long): MongoBrak? {
            return braks.find(or(eq("firstUserID", userID), eq("secondUserID", userID)))
        }

        suspend fun getFromUser(user: User?): MongoBrak?{
            return getFromUserID(user?.id ?: return null)
        }

        suspend fun getFromKidID(id: Long): MongoBrak? {
            return braks.find(eq("baby.userID", id))
        }

        suspend fun getFromKid(kid: User): MongoBrak? {
            return getFromKidID(kid.id)
        }
    }
}
