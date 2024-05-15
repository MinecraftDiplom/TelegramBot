package bot.callbacks

import com.github.kotlintelegrambot.dispatcher.handlers.CallbackQueryHandlerEnvironment

data class Callback(
    val data: String,
    val type: ContextType,
    val ownerIds: List<Long>? = null,
    var label: String? = null,
    var answerText: String? = null,
    val callback: suspend (CallbackQueryHandlerEnvironment) -> Unit,
)