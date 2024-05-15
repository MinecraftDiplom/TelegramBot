package models.mongo

import com.mongodb.client.model.Filters.eq
import models.minecraft.MinecraftUser
import org.bson.types.ObjectId
import database.MongoDB.promos
import java.security.SecureRandom
import java.time.Instant

data class MongoPromocode(
    var code: String,
    var details: String = "sub:30",
    var activated_to: MinecraftUser? = null,
    var created_at: Long? = Instant.now().toEpochMilli(),
    var activated_at: Long? = null,
    var _id: ObjectId? = null,
){
    suspend fun activate(user: MinecraftUser){
        promos.update(eq("code", code),
            this.apply {
                activated_at = Instant.now().toEpochMilli()
                activated_to = user
            }
        )
    }
    companion object{
        fun customSubCode(days: String, code: String): MongoPromocode {
            return MongoPromocode(
                code,
                details = "sub:$days"
            )
        }
        suspend fun generateSubCode(days: Int = 30): MongoPromocode {
            return MongoPromocode(
                generateCode(),
                details = "sub:$days"
            )
        }
        private suspend fun generateCode(codeLength: Int = 12):String{
            val chars = "abcdefghijklmnopqrstuvwxyz1234567890".toCharArray()
            val random = SecureRandom()
            val code = buildString {
                for (i in 1..codeLength) {
                    append(chars[random.nextInt(chars.size)])
                    if(i != codeLength && i % 6 == 0) append("-")
                }
            }
            if(promos.collection.countDocuments(eq("code", code)) != 0L){
                return generateCode(codeLength)
            }
            return code
        }
    }
}

