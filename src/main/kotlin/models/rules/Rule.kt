package org.example.models

import kotlinx.serialization.Serializable

@Serializable
data class LinkRule (
    val link: String,
){
    companion object{
        fun create(link: String): LinkRule{
            return LinkRule(
                link = link
            )
        }
    }
}