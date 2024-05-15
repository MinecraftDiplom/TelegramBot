package bot.Messages

import com.github.kotlintelegrambot.dispatcher.handlers.HandleCallbackQuery

class Button(var label: String, val data: String, val onClicked: HandleCallbackQuery)