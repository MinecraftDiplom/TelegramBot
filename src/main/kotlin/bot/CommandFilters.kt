package bot

import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.handlers.*
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.extensions.filters.Filter
import storage.AdminsDB

fun Dispatcher.commands(vararg commands: String, handleCommand: HandleCommand) {
    commands.forEach { command ->
        addHandler(CommandHandler(command, handleCommand))
    }
}

fun Dispatcher.commandMenu(command: String, menuPreview: String, handleCommand: HandleText) {
    addHandler(TextHandler("/$command", handleCommand))
    addHandler(TextHandler(menuPreview, handleCommand))
}

fun Dispatcher.adminCommand(message: String, handleMessage: HandleMessage) {
    addHandler(MessageHandler(AdminFilter(message), handleMessage))
}

fun Message.arguments(): List<String> {
    return this.text?.split("\\s+".toRegex())?.drop(1) ?: emptyList()
}

//725757421L
class AdminFilter(private val command:String) : Filter {
    override fun Message.predicate(): Boolean = text != null && text?.split("\\s+".toRegex())?.firstOrNull() == command && AdminsDB.isAdmin(from?.id)
}

object SuccessfulPaymentFilter : Filter{
    override fun Message.predicate(): Boolean = successfulPayment != null
}