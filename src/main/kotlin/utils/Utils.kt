@file:OptIn(ExperimentalCoroutinesApi::class)

package utils

import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.TelegramFile
import kotlinx.coroutines.*
import models.braks.FamilyTree
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@DelicateCoroutinesApi
@OptIn(ExperimentalCoroutinesApi::class)
val DAEMON = newSingleThreadContext("TelegramBot-Daemon")

@OptIn(DelicateCoroutinesApi::class)
fun withDaemon(block: suspend () ->Unit) {
    CoroutineScope(DAEMON).launch {
        block()
    }
}

val Message.chatId: ChatId
    get() = ChatId.fromId(this.chat.id)

val moscowZone: TimeZone = TimeZone.getTimeZone(ZoneId.of("Europe/Moscow"))
fun calendarMoscow() : Calendar = GregorianCalendar.getInstance(moscowZone)

fun getTimeMillis(): Long{
    calendarMoscow().apply {
        return timeInMillis
    }
}

fun String.cut(requiredLength: Int, postFix: String = "..."): String {
    return if (length > requiredLength) {
        substring(0, requiredLength - 3) + postFix
    }else this.toString()
}

fun List<String>.toForm(formLength: Int = 10, wallCornerEmoji :String =  "➕", wallEmoji: String = "➖", ):String{
    val builder = StringBuilder()
    builder.append(wallCornerEmoji)
    builder.append(wallEmoji.repeat(formLength-2))
    builder.appendLine(wallCornerEmoji)
    this.forEach {
        builder.appendLine(it)
    }
    builder.append(wallCornerEmoji)
    builder.append(wallEmoji.repeat(formLength-2))
    builder.append(wallCornerEmoji)
    return builder.toString()
}

fun List<String>.toUpperLineForm(formLength: Int = 10, wallCornerEmoji :String =  "➕", wallEmoji: String = "➖", ):String{
    val builder = StringBuilder()
    this.forEach {
        builder.appendLine(it)
    }
    builder.append(wallCornerEmoji)
    builder.append(wallEmoji.repeat(formLength-2))
    builder.append(wallCornerEmoji)
    return builder.toString()
}

fun List<String>.toDownLineForm(formLength: Int = 10, wallCornerEmoji :String =  "➕", wallEmoji: String = "➖", ):String{
    val builder = StringBuilder()
    this.forEach {
        builder.appendLine(it)
    }
    builder.append(wallCornerEmoji)
    builder.append(wallEmoji.repeat(formLength-2))
    builder.append(wallCornerEmoji)
    return builder.toString()
}


fun Long?.timeToString(): String{
    val now = getTimeLocal(getTimeMillis())
    val started = getTimeLocal(this ?: 0L)
    val seconds = ChronoUnit.SECONDS.between(started, now)
    val minutes = ChronoUnit.MINUTES.between(started, now)
    val hours = ChronoUnit.HOURS.between(started, now)
    val days = ChronoUnit.DAYS.between(started, now)
    val month = ChronoUnit.MONTHS.between(started, now)
    val years = ChronoUnit.YEARS.between(started, now)

    return when {
        years > 0 -> "$years лет"
        month > 0 -> "$month мес."
        days > 9 -> "$days дней"
        days > 0 -> "$days дн. ${hours%24} ч."
        hours > 0 -> "$hours ч. ${minutes%60} мин."
        minutes > 5 -> "$minutes мин. ${seconds%60} сек."
        minutes >= 1 -> "$minutes минут"
        else -> "Молодожёны"
    }
}

fun getTimeLocal(time: Long): ZonedDateTime{
    return Date(time).toInstant().atZone(moscowZone.toZoneId())
}

fun testTime():Long{
    return System.currentTimeMillis()
}

suspend fun telegramTreeFileFromID(userID: Long): TelegramFile.ByByteArray{
    return try {
        FamilyTree.create(userID).let {
            TelegramFile.ByByteArray(it.toImage(), "$userID-tree.png")
        }
    }catch (e:Exception) {
        e.printStackTrace()
        TelegramFile.ByByteArray("Ошибка при создании семейного древа".toByteArray(), "error.png")
    }
}

inline fun <T> block(block:() -> T) : T {
    return block()
}