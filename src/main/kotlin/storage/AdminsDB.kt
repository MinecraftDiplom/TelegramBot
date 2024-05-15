package storage

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object AdminsDB {
    private val FILE = File("${SAVE_DIR}/admins.json")
    private var list: MutableList<Long>
    init {
        if(SAVE_DIR.exists().not()){
            SAVE_DIR.mkdir()
            println("[ADMINS] Directory is not exists! Create...")
        }
        if(FILE.exists() || FILE.length() != 0L) {
            list = Json.decodeFromString<MutableList<Long>>(FILE.readText())
            println("[ADMINS] Admins is loaded.")
        }else{
            list = mutableListOf()
            println("[ADMINS] File is not exists!")
        }
    }

    @Synchronized
    fun save(){
        FILE.delete()
        FILE.createNewFile()
        FILE.writeText(Json.encodeToString(list))
    }

    fun isAdmin(userID: Long?):Boolean{
        return userID in list
    }

    fun op(userID:Long):String{
        return if(isAdmin(userID)) "$userID уже админ."
        else{
            add(userID)
            "$userID теперь админ."
        }
    }

    fun deop(userID:Long):String{
        return if(isAdmin(userID)){
            remove(userID)
            "$userID больше не админ."
        }
        else{
            "$userID не админ."
        }
    }

    fun add(userID:Long){
        list.add(userID)
        save()
    }

    fun remove(userID:Long){
        list.remove(userID)
        save()
    }

    fun admins(){
        list.forEach {
            println(UsersDB.getFromUserID(it))
        }
    }
}