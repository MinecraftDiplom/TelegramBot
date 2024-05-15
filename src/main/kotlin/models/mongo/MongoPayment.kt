package models.mongo

import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.User
import com.github.kotlintelegrambot.entities.payments.SuccessfulPayment
import org.bson.types.ObjectId
import java.time.Instant

data class MongoPayment(
    var payment: SuccessfulPayment,
    val from: User? = null,
    var time: Long? = Instant.now().toEpochMilli(),
    var _id: ObjectId? = null,
) {
    companion object {
        fun fromMessage(message: Message): MongoPayment {
            requireNotNull(message.successfulPayment)
            return MongoPayment(
                payment = message.successfulPayment!!,
                from = message.from,
            )
        }
    }
}
