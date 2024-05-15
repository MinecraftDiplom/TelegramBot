package storage

import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.MessageEntity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File


object RuleDB {
    private val FILE = File("${SAVE_DIR}/rules.json")
    var list: MutableList<String>
    init {
        if(SAVE_DIR.exists().not()){
            SAVE_DIR.mkdir()
            println("[RULES] Directory is not exists! Create...")
        }
        if(FILE.exists() || FILE.length() != 0L) {
            list = Json.decodeFromString<MutableList<String>>(FILE.readText())
            println("[RULES] Rules is loaded.")
        }else{
            list = mutableListOf()
            println("[RULES] File is not exists!")
        }
    }

    @Synchronized
    fun save(){
        FILE.delete()
        FILE.createNewFile()
        FILE.writeText(Json.encodeToString(list))
    }

    fun check(message: Message): List<String> {
        val blocks = mutableListOf<String>()
        message.entities?.forEach { link ->
            when(link.type){
                MessageEntity.Type.TEXT_LINK -> {
                    if(list.contains(link.url))
                        blocks += link.url ?: "null"
                }
                else -> {}
            }
        }
        message.captionEntities?.forEach { link ->
            when(link.type){
                MessageEntity.Type.TEXT_LINK -> {
                    if(list.contains(link.url))
                        blocks += link.url ?: "null"
                }
                else -> {}
            }
        }
        if(exist(message.text))
            blocks += "внутри сообщения"
        return blocks
    }

    fun getUrls(message: Message): List<String> {
        val urls = mutableListOf<String>()
        message.entities?.forEach { link ->
            when(link.type){
                MessageEntity.Type.TEXT_LINK ->{
                    urls += link.url ?: "null"
                }
                else -> {}
            }
        }
        message.captionEntities?.forEach { link ->
            when(link.type){
                MessageEntity.Type.TEXT_LINK ->{
                    urls += link.url ?: "null"
                }
                else -> {}
            }
        }
        return urls
    }

    fun exist(text:String?): Boolean {
        if(text==null) return false
        list.forEach {
            if(text.contains(it))
                return true
        }
        return false
    }

    fun add(link: String){
        list.add(link)
        save()
    }

    fun remove(link: String){
        list.removeIf { it == link }
        save()
    }

}