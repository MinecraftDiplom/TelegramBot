package storage

import com.beust.klaxon.Klaxon
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.User
import utils.getTimeMillis
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

val SAVE_DIR = File("./data")
object UsersDB {
    private val FILE = File("${SAVE_DIR}/users.json")
    var list: MutableList<User> = fileLoad()
    private val messageLogger = Logger("messages")
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    init {
        scheduler.scheduleWithFixedDelay({
            save()
            println("[UsersDB] File is saved.")
        }, 5L, 30L, TimeUnit.MINUTES)
    }

    private fun fileLoad(): MutableList<User>{
        if(SAVE_DIR.exists().not()){
            SAVE_DIR.mkdir()
            println("[UsersDB] Directory is not exists! Create...")
            return mutableListOf()
        }
        if(FILE.exists().not() || FILE.length() == 0L) {
            println("[UsersDB] File is not exists!")
            return mutableListOf()
        }
        println("[UsersDB] Admins is loaded.")
        return Klaxon().parseArray<User>(FILE.readText())!!.toMutableList()
    }

    fun load(){
        list = fileLoad()
    }

    @Synchronized
    fun save(){
        if(FILE.exists().not()) FILE.createNewFile()
        FILE.writeText(Klaxon().toJsonString(list))
    }

    fun getFromUserID(userID:Long?):User?{
        return list.firstOrNull { it.id == userID }
    }

    fun checkAndLog(message: Message){
        messageLogger.writeln("[${getTimeMillis()}] @${message.from?.username}: ${message.text}")
        checkUser(message.from?:return)
    }
    fun checkUser(user: User){
        val item = list.firstOrNull{it.id == user.id}
        if(item == null){
            list.add(user)
            println("add user @${user.username}")
            return
        }
        if(item != user){
            list[list.indexOf(item)] = user
            println("edit $item \nto $user")
            return
        }
    }

    fun add(user: User){
        list.add(user)
    }

    fun remove(user: User){
        list.remove(user)
    }
}