package bot.callbacks

import bot.MainLogger.Companion.logger
import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.handlers.HandleCallbackQuery
import kotlinx.coroutines.delay
import utils.withDaemon
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

object CallbackManager {

    val callbacks: MutableMap<String, Callback> = mutableMapOf()
    val boxes = mutableListOf<MessageBox>()
    val eraser: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    var NEXT_ID:Int = 0
        get(){
            val last = field
            field++
            return last
        }

    fun staticCallbackQuery(data: String, ownerIds: List<Long>? = null, callback: HandleCallbackQuery){
        if(callbacks.containsKey(data))
            throw Exception("bot.callbacks.Callback with data $data already exists")
        callbacks[data] = Callback(data, ContextType.STATIC, ownerIds, null, null, callback)
    }

    fun dynamicQuery(label: String, type: ContextType, ownerIds: List<Long>? = null, minutes: Long = 60L, answerText: String? = null, callback: HandleCallbackQuery): Callback {
        val data = UUID.randomUUID().toString()
        if(callbacks.containsKey(data))
            throw Exception("bot.callbacks.Callback with data $data already exists")
        val button = Callback(data, type, ownerIds, label, answerText, callback)
        callbacks[data] = button
        withDaemon {
            delay(minutes * 60_000)
            callbacks.remove(data)
        }
        return button
    }

    fun Dispatcher.initMainHandler(){
        callbackQuery {
            logger.trace("click ${callbackQuery.data}")

            val query = this.callbackQuery

            val callback = callbacks[query.data] ?: run {
                bot.answerCallbackQuery(query.id, "Эта кнопка уже была нажата \uD83C\uDF5E.", false)
                return@callbackQuery
            }

            if(callback.ownerIds?.contains(query.from.id)?.not() == true){
                bot.answerCallbackQuery(query.id, "Эта кнопка запривачена \uD83E\uDD1A", false)
                return@callbackQuery
            }

            callback.callback(this)

            if(callback.answerText != null){
                bot.answerCallbackQuery(query.id, callback.answerText, true)
                return@callbackQuery
            }

            when(callback.type) {
                ContextType.STATIC,
                ContextType.TEMPORARY -> {}
                ContextType.ONE_CLICK -> {
                    callbacks.remove(query.data)
                }
                ContextType.CHOOSE_ONE -> {
                    val box = boxes.firstOrNull { it.buttons.contains(callback) } ?: return@callbackQuery
                    box.buttons.forEach {
                        callbacks.remove(it.data)
                    }
                    boxes.remove(box)
                }
                else -> {
                    bot.answerCallbackQuery(query.id, "Unknown callback type - ${callback.data}")
                    return@callbackQuery
                }
            }

            bot.answerCallbackQuery(query.id)

        }

    }
}