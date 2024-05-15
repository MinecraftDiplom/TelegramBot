package models.invoice

import kotlinx.serialization.Serializable

@Serializable
data class Receipt(
    val email: String? = null,
    val items: List<Item>
)