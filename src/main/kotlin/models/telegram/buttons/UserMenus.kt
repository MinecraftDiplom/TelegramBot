package org.koliy82.models.telegram.buttons

import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.User
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import models.mongo.MongoBrak

suspend fun generateMenuButtons(user: User): KeyboardReplyMarkup {
    val brak = MongoBrak.getFromUserID(user.id)
    val buttons = mutableListOf(mutableListOf<KeyboardButton>())

    if(brak != null){
        buttons += mutableListOf(
            KeyboardButton("👤 Профиль"),
            KeyboardButton("💍 Брак"),
            KeyboardButton("💔 Развод"),
        )
        buttons += mutableListOf(
            KeyboardButton("🌱 Древо (текст)"),
            KeyboardButton("🌳 Древо (картинка)"),
        )
    }else{
        buttons += mutableListOf(
            KeyboardButton("👤 Профиль"),
        )
    }
    buttons += mutableListOf(
        KeyboardButton("💬 Браки чата"),
        KeyboardButton("🌍 Браки всех чатов"),
    )
    buttons += mutableListOf(
        KeyboardButton("💳 Подписка"),
    )
    buttons += mutableListOf(
        KeyboardButton("🖥 Лаунчер (.exe/.jar)"),
        KeyboardButton("📱 Приложение (apk)"),
    )
    buttons += mutableListOf(
        KeyboardButton("❌ Закрыть"),
    )
    return KeyboardReplyMarkup(
        keyboard = buttons,
        resizeKeyboard = true,
        oneTimeKeyboard = false
    )
}