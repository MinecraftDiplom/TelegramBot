package models.mongo

import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.files.*
import org.bson.types.ObjectId

data class MongoMessage(
    val messageId: Long,
    val from: User? = null,
    val senderChat: Chat? = null,
    val date: Long,
    val chat: Chat,
    val forwardFrom: User? = null,
    val forwardFromChat: Chat? = null,
    val forwardFromMessageId: Int? = null,
    val forwardDate: Int? = null,
    val replyToMessage: MongoMessage? = null,
    val viaBot: User? = null,
    val editDate: Int? = null,
    val mediaGroupId: String? = null,
    val authorSignature: String? = null,
    val text: String? = null,
    val audio: Audio? = null,
    val document: Document? = null,
    val animation: Animation? = null,
    val diceEmoji: String? = null,
    val diceValue: Int? = null,
    val photo: List<PhotoSize>? = null,
    val video: Video? = null,
    val voice: Voice? = null,
    val videoNote: VideoNote? = null,
    val caption: String? = null,
    val _id:ObjectId? = null,
){
    companion object{

        fun fromMessage(message: Message):MongoMessage{

            return MongoMessage(
                messageId = message.messageId,
                from = message.from,
                senderChat = message.senderChat,
                date = message.date,
                chat = message.chat,
                forwardFrom = message.forwardFrom,
                forwardFromChat = message.forwardFromChat,
                forwardFromMessageId = message.forwardFromMessageId,
                forwardDate = message.forwardDate,
                replyToMessage = message.replyToMessage?.let { fromMessage(it) },
                viaBot = message.viaBot,
                editDate = message.editDate,
                mediaGroupId = message.mediaGroupId,
                authorSignature = message.authorSignature,
                text = message.text,
                audio = message.audio,
                document = message.document,
                animation = message.animation,
                diceEmoji = message.dice?.emoji?.emojiValue,
                diceValue = message.dice?.value,
                photo = message.photo,
                video = message.video,
                voice = message.voice,
                videoNote = message.videoNote,
                caption = message.caption,
            )
        }
    }
}