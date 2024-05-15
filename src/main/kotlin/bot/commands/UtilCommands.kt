package bot.commands

import bot.commandMenu
import bot.commands
import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ReplyKeyboardRemove
import models.telegram.buttons.Buttons
import org.koliy82.models.telegram.buttons.generateMenuButtons
import utils.chatId


fun Dispatcher.utilsCommands(){

    commands("help", "start", "помощь", "начать"){
        bot.sendMessage(message.chatId, text = bot.getMyCommands().get().joinToString("\n"){"/${it.command} - ${it.description}"}, replyMarkup = generateMenuButtons(message.from ?: return@commands))
    }

    command("menu"){
        if(message.chat.type != "private"){
            bot.sendMessage(
                chatId = ChatId.fromId(message.chat.id),
                text = "Меню работает только в личных сообщениях.",
            )
            return@command
        }
        bot.sendMessage(
            chatId = ChatId.fromId(message.chat.id),
            text = "Меню показано",
            replyMarkup = generateMenuButtons(message.from ?: return@command),
        )
    }

    commandMenu("menuclose", "❌ Закрыть"){
        if(message.chat.type != "private"){
            return@commandMenu
        }
        bot.sendMessage(
            chatId = ChatId.fromId(message.chat.id),
            text = "Меню закрыто, повторно открыть его можно написав /menu.",
            replyMarkup = ReplyKeyboardRemove(),
        )
    }

    command("web"){
        bot.sendMessage(message.chatId, text = "test web", replyMarkup = Buttons.web.buttons)
    }
}