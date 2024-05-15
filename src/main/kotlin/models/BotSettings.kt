package models

import com.mongodb.ConnectionString
import java.io.File

data class BotSettings(
    var host:String,
    var port:String,
    var user:String,
    var password:String,
    var dbname:String,
    var yoomany_test:String,
    var yoomany_prod:String,
    var jdbcUrl:String,
    var postgres_user:String,
    var postgres_password:String,
){

    fun toMongoConnection(): ConnectionString {
        return ConnectionString("mongodb://$user:$password@$host:$port/")
    }
    companion object {
        @Throws(IllegalArgumentException::class)
        fun parseFromFile(file: File = File("./bot.env")) : BotSettings {
            require(file.exists()){ "File not found ${file.absoluteFile}" }
            val map = mutableMapOf<String, String>()
            file.readLines().forEach {
                val args = it.split("=")
                val key = args[0]
                val va = args[1]
                map[key] = va
            }
            return BotSettings(
                host = map["host"] ?:throw IllegalArgumentException("host not found"),
                port = map["port"] ?:throw IllegalArgumentException("port not found"),
                user = map["user"] ?:throw IllegalArgumentException("user not found"),
                password = map["password"] ?:throw IllegalArgumentException("password not found"),
                dbname = map["dbname"] ?:throw IllegalArgumentException("dbname not found"),
                yoomany_test = map["yoomanyTEST"] ?:throw IllegalArgumentException("yoomany test token not found"),
                yoomany_prod = map["yoomanyPROD"] ?:throw IllegalArgumentException("yoomany prod token not found"),
                jdbcUrl = map["jdbcUrl"] ?:throw IllegalArgumentException("jdbcUrl not found"),
                postgres_user = map["postgres_user"] ?:throw IllegalArgumentException("postgres_user not found"),
                postgres_password = map["postgres_password"] ?:throw IllegalArgumentException("postgres_password not found"),
            )
        }

    }
}