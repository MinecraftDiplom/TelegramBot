package storage

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import java.io.File

class Logger(filename:String, val logChannel:ChatId = ChatId.fromId(-1001949788485L)) {
    private val file = File("${SAVE_DIR}/$filename.logs")
    init {
        if(SAVE_DIR.exists().not()) SAVE_DIR.mkdir()
        if(file.exists().not()) file.createNewFile()
    }

    fun print(text: String, bot: Bot){
        //bot.sendMessage(logChannel, text)
        println(text)
    }

    fun write(text:String){
        file.appendText(text)
    }

    fun writeln(text:String){
        file.appendText(text+"\n")
    }

    fun printAndWrite(text: String, bot: Bot){
        writeln(text)
        print(text, bot)
    }
}