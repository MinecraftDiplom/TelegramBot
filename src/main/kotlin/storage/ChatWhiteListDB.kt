package storage

import com.github.kotlintelegrambot.entities.Chat
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class ChatDTO(
    val id: Long,
    val type: String,
    val title: String? = null,
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val bio: String? = null,
    val description: String? = null,
    val inviteLink: String? = null,
    val pinnedMessage: String? = null,
    val slowModeDelay: Int? = null,
    val stickerSetName: String? = null,
    val canSetStickerSet: Boolean? = null,
    val linkedChatId: Long? = null,
){
    companion object {
        fun fromChat(chat: Chat): ChatDTO {
            return ChatDTO(
                id = chat.id,
                type = chat.type,
                title = chat.title,
                username = chat.username,
                firstName = chat.firstName,
                lastName = chat.lastName,
                bio = chat.bio,
                description = chat.description,
                inviteLink = chat.inviteLink,
                pinnedMessage = chat.pinnedMessage,
                slowModeDelay = chat.slowModeDelay,
                stickerSetName = chat.stickerSetName,
                canSetStickerSet = chat.canSetStickerSet,
                linkedChatId = chat.linkedChatId,
            )
        }
    }

}

object ChatWhiteListDB {
    private val FILE = File("${SAVE_DIR}/chat_whitelist.json")
    var list: MutableList<ChatDTO>
    var isEnable: Boolean = true
    init {
        if(SAVE_DIR.exists().not()){
            SAVE_DIR.mkdir()
            println("[ChatsList] Directory is not exists! Create...")
        }
        if(FILE.exists() || FILE.length() != 0L) {
            list = Json.decodeFromString<MutableList<ChatDTO>>(FILE.readText())
            println("[ChatsList] WhiteList is loaded.")
        }else{
            list = mutableListOf()
            println("[ChatsList] File is not exists!")
        }
    }

    @Synchronized
    fun save(){
        FILE.delete()
        FILE.createNewFile()
        FILE.writeText(Json.encodeToString(list))
    }

    fun isWhitelisted(chatid: Long): Boolean{
        return list.firstOrNull { it.id == chatid } != null
    }

    fun op(chat: Chat):String{
        return if(isWhitelisted(chat.id)) "${chat.title} уже в белом списке."
        else{
            add(chat)
            "Теперь $chat.title в белом списке."
        }
    }

    fun deop(userID:Long):String{
        return if(isWhitelisted(userID)){
            remove(userID)
            "$userID больше не админ."
        }
        else{
            "$userID не админ."
        }
    }

    private fun add(chat: Chat){
        list.add(ChatDTO.fromChat(chat))
        save()
    }

    private fun remove(chatid: Long){
        list.removeIf { it.id == chatid }
        save()
    }

    fun chats(): List<ChatDTO> {
        return list
    }

    fun on(): String {
        isEnable = true
        return isEnable.toString()
    }

    fun off(): String {
        isEnable = false
        return isEnable.toString()
    }
}