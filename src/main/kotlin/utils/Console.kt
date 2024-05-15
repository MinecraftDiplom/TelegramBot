package utils

import database.MongoDB.braks
import database.MongoDB.users
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import models.mongo.MongoBaby
import models.mongo.MongoBrak
import models.mongo.MongoUser
import storage.AdminsDB
import storage.BraksDB
import storage.UsersDB
import java.lang.management.ManagementFactory
import kotlin.concurrent.thread

fun consoleStart(){
    thread {
        while(true){
            try {
                val args = readln().split(" ")
                when(args[0]){
                    "mem"->{
                        printMemory()
                    }
                    "gc"->{
                        printMemory()
                        System.gc()
                        printMemory()
                    }
                    "op"->{
                        val id = args[1]
                        if(id.length == 9) print(AdminsDB.op(id.toLong())+"\n")
                        else println("Need 9 digits")
                    }
                    "deop"->{
                        val id = args[1]
                        if(id.length == 9) print(AdminsDB.deop(id.toLong())+"\n")
                        else println("Need 9 digits")
                    }
                    "ops"->{
                        AdminsDB.admins()
                    }
                    "mongo"->{
                        when(args[1]){
                            "transfer"-> {
                                when(args[2]){
                                    "users" ->{
                                        val list = mutableListOf<MongoUser>()
                                        UsersDB.list.forEach {
                                            BraksDB.getFromUser(it)?.let { brak ->
                                                list.add(MongoUser.update(it, 1))
                                            } ?: run {
                                                list.add(MongoUser.create(it))
                                            }
                                        }
                                        runBlocking {
                                            users.collection.drop()
                                            users.transferDatabase(list.toList())
                                        }
                                    }
                                    "braks" -> {
                                        val list = mutableListOf<MongoBrak>()
                                        BraksDB.list.forEach {
                                            if(it.baby != null)
                                                list.add(MongoBrak(it.firstUserID, it.secondUserID, MongoBaby(it.baby?.userID, it.baby?.time), it.time))
                                            else
                                                list.add(MongoBrak(it.firstUserID, it.secondUserID, null, it.time))
                                        }
                                        runBlocking {
                                            braks.collection.drop()
                                            braks.transferDatabase(list.toList())
                                        }
                                    }
                                    else -> println("Select transfer database")
                                }

                            }
                            "list"->{
                                when(args[2]){
                                    "users" ->{
                                        runBlocking {
                                            val col = users.collection.find().toList()
                                            println(col)
                                            println(col.size)
                                            println(UsersDB.list.size)
                                        }
                                    }
                                    "braks" -> {
                                        runBlocking {
                                            val col = braks.collection.find().toList()
                                            println(col)
                                            println(col.size)
                                            println(BraksDB.list.size)
                                        }
                                    }
                                    else -> println("Select transfer database")
                                }

                            }
                        }
                    }
                    else -> println("Command not found")
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }
}

fun readMemory():String{
    val heapMemoryUsage = ManagementFactory.getMemoryMXBean().heapMemoryUsage
    val usage = heapMemoryUsage.used / (1024 * 1024)
    val max = heapMemoryUsage.max / (1024 * 1024)
    val commited = heapMemoryUsage.committed / (1024 * 1024)
    return " RAM min-max: $commited-$max MB\n Used: $usage MB (${commited-usage})"
}

fun printMemory(){
    println("--------------------------")
    println(readMemory())
    println("--------------------------")
}