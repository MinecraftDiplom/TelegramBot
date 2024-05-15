package models.invoice

import kotlinx.serialization.Serializable

@Serializable
data class Amount(
    val currency: String,
    val value: String
)