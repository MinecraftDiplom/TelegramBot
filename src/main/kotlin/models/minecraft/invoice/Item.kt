package models.invoice

import kotlinx.serialization.Serializable

@Serializable
data class Item(
    val description: String,
    val quantity: String,
    val amount: Amount,
    val vat_code: Int,
)