package storage

import com.beust.klaxon.Klaxon
import com.github.kotlintelegrambot.entities.User
import models.braks.Babyold
import models.braks.Brakold
import java.io.File


object BraksDB {
    private val FILE = File("${SAVE_DIR}/braks.json")
    var list: MutableList<Brakold> = fileLoad()
    private fun fileLoad(): MutableList<Brakold>{
        if(SAVE_DIR.exists().not()){
            SAVE_DIR.mkdir()
            println("[BraksDB] Directory is not exists! Create...")
            return mutableListOf()
        }
        if(FILE.exists().not() || FILE.length() == 0L) {
            println("[BraksDB] File is not exists!")
            return mutableListOf()
        }
        println("[BraksDB] Admins is loaded.")
        return Klaxon().parseArray<Brakold>(FILE.readText())!!.toMutableList()
    }

    fun listUpdate(){
        list = fileLoad()
    }

    @Synchronized
    fun save(){
        if(FILE.exists().not()) FILE.createNewFile()
        FILE.writeText(Klaxon().toJsonString(list))
        println("[BraksDB] File is saved.")
    }

    fun getFromUser(user: User?): Brakold?{
        return list.firstOrNull { it.firstUser()?.id == user?.id || it.secondUser()?.id == user?.id }
    }

    fun braksCount(user:User?): List<Brakold> {
        return list.filter { it.firstUser()?.id == user?.id || it.secondUser()?.id == user?.id }
    }

    fun getFromUserID(userID: Long): Brakold?{
        return list.firstOrNull { it.firstUserID == userID || it.secondUserID == userID }
    }

    fun getFromKid(kid:Babyold): Brakold? {
        return list.firstOrNull { it.baby?.userID == kid.userID }
    }

    fun getFromKidID(kidID:Long): Brakold? {
        return list.firstOrNull { it.baby?.userID == kidID }
    }

    fun add(firstUser: User, secondUser: User){
        list.add(Brakold().create(firstUser.id, secondUser.id))
        save()
    }

    fun remove(brak: Brakold){
        list.remove(brak)
        save()
    }

    fun top():List<Brakold>{
        return list.take(10)
    }

    fun edit(brak:Brakold, newBrak: Brakold){
        list[list.indexOf(brak)] = newBrak
        save()
    }

    fun createBaby(brak:Brakold, toUserID:Long){
        list.find { it == brak }?.baby = Babyold.create(toUserID)
        save()
    }

    fun removeBaby(brak:Brakold){
        list.find { it == brak }?.baby = null
        save()
    }
}