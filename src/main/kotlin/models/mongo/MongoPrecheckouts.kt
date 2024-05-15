package models.mongo

import com.github.kotlintelegrambot.entities.User
import com.github.kotlintelegrambot.entities.payments.OrderInfo
import com.github.kotlintelegrambot.entities.payments.PreCheckoutQuery
import org.bson.types.ObjectId
import java.time.Instant

data class MongoPrecheckout(
    val id: String,
    val from: User,
    val currency: String,
    val totalAmount: Long,
    val invoicePayload: String,
    val shippingOptionId: String?,
    val orderInfo: OrderInfo?,
    var time: Long? = Instant.now().toEpochMilli(),
    var _id: ObjectId? = null,
) {
    companion object {
        fun fromHandler(query: PreCheckoutQuery): MongoPrecheckout {
            return MongoPrecheckout(
                id = query.id,
                from = query.from,
                currency = query.currency,
                totalAmount = query.totalAmount.toLong(),
                invoicePayload = query.invoicePayload,
                shippingOptionId = query.shippingOptionId,
                orderInfo = query.orderInfo,
            )
        }
    }
}
