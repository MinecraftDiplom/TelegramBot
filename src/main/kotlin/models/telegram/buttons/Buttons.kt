package models.telegram.buttons

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.keyboard.WebAppInfo

enum class Buttons(val buttons: InlineKeyboardMarkup) {
    payment(
        InlineKeyboardMarkup.create(
            listOf(
                listOf(
                    InlineKeyboardButton.CallbackData(
                        text = "Подписка на 30 ДНЕЙ",
                        callbackData = "1:m"
                    ),
                ),
                listOf(
                    InlineKeyboardButton.CallbackData(
                        text = "Подписка на 90 ДНЕЙ",
                        callbackData = "3:m"
                    ),
                )
            )
        )
    ),
    download(
        InlineKeyboardMarkup.createSingleRowKeyboard(
            InlineKeyboardButton.Url("Для Windows (.exe)","http://kissota.ru:9274/MasturLand.exe"),
            InlineKeyboardButton.Url("Для Linux/Mac (.jar)", "http://kissota.ru:9274/MasturLand.jar"),
        )
    ),
//    mobile(
//        InlineKeyboardMarkup.createSingleRowKeyboard(
//            InlineKeyboardButton.CallbackData("MineProfile","download_mobile"),
//        )
//    ),
    web(
        InlineKeyboardMarkup.createSingleRowKeyboard(
            InlineKeyboardButton.WebApp("kissota.ru", WebAppInfo("https://kissota.ru"))
        )
    ),
}