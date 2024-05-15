package bot.commands

import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.command
import database.MineDB
import models.minecraft.MinecraftUser
import utils.block
import utils.chatId

fun Dispatcher.minecraftCommands(){

    command("register"){
        if(message.chat.type != "private"){
            bot.sendMessage(message.chatId, "В целях безопасности команда доступна только в личных сообщениях бота.")
            return@command
        }

        if(args.count() != 2 ){
            bot.sendMessage(message.chatId, "Неверный формат: /register ник пароль")
            return@command
        }
        
        val nick = args[0]
        val password = args[1]

        val nickCheck = Regex("^[a-zA-Z0-9_]{2,32}$")
        if(nickCheck.matches(nick).not()){
            bot.sendMessage(message.chatId, "Неверный формат, можно только англ. символы и цифры с нижними подчёркиваниеями, длинной от 2 до 32 символов. \n(Пример: /register aboba_2010 MyMegaPassword228)")
            return@command
        }

        block {
            val userQuery = MineDB.connection.prepareStatement("select count(*) from users where username = ?")
            userQuery.setString(1, nick)
            val result = userQuery.executeQuery()
            result.next()
            val count = result.getInt("count")
            if(count != 0){
                bot.sendMessage(message.chatId, "Пользователь с таким ником уже зарегистрирован.")
                result.close()
                return@command
            }
            result.close()
        }

        block {
            val query = MineDB.connection.prepareStatement("select count(*) from users where telegram_id = ?")
            query.setString(1, message.from?.id?.toString() ?:return@command)
            val result = query.executeQuery()
            result.next()
            val count = result.getInt("count")
            if(count != 0){
                bot.sendMessage(message.chatId, "Ты уже зарегистрирован")
                result.close()
                return@command
            }
        }

        block {
            val query = MineDB.connection.prepareStatement("INSERT INTO users (username, password, telegram_id) VALUES (?, ?, ?)")
            query.use {
                query.setString(1, nick)
                query.setString(2, password)
                query.setString(3, message.from?.id?.toString() ?: return@command)
                query.execute()
            }
            bot.sendMessage(message.chatId, "Ты успешно зарегистрировался! Для доступа к приватному серверу необходимо оформить подписку командой /subscribe.")
        }

    }

    command("password"){
        if(message.chat.type != "private"){
            bot.sendMessage(message.chatId, "В целях безопасности команда доступна только в личных сообщениях бота.")
            return@command
        }
        if(args.count() != 1 ){
            bot.sendMessage(message.chatId, "Неверный формат: /password новый_пароль")
            return@command
        }
        val user = MinecraftUser.getFromDatabase(message.from?.id?:return@command)
        if(user == null){
            bot.sendMessage(message.chatId, "Зарегистрируйтесь командой /register ник пароль")
            return@command
        }
        user.newPassword(args[0])
        bot.sendMessage(message.chatId, "Пароль успешно изменён.")
    }
}