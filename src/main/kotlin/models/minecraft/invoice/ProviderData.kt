package models.invoice

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ProviderData(
    val receipt: Receipt
){
    fun toJson(): String{
        return Json.encodeToString(this)
    }

    companion object{
        fun create(vararg items:Item):ProviderData{
            return ProviderData(
                Receipt(
                    items = items.toList()
                )
            )
        }
        fun create(items: List<Item> ):ProviderData{
            return ProviderData(
                Receipt(
                    items = items
                )
            )
        }
    }
}