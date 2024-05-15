package bot.commands

import bot.MainLogger.Companion.logger
import bot.SuccessfulPaymentFilter
import bot.callbacks.CallbackManager.staticCallbackQuery
import bot.commandMenu
import bot.telegramBot
import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.dispatcher.preCheckoutQuery
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.payments.InvoiceUserDetail
import com.github.kotlintelegrambot.entities.payments.LabeledPrice
import com.github.kotlintelegrambot.entities.payments.PaymentInvoiceInfo
import com.mongodb.client.model.Filters
import database.MineDB
import database.MineDB.connection
import database.MongoDB
import kotlinx.coroutines.*
import models.invoice.Amount
import models.invoice.Item
import models.invoice.ProviderData
import models.minecraft.MinecraftUser
import models.mongo.MongoPayment
import models.mongo.MongoPrecheckout
import models.telegram.buttons.Buttons
import utils.chatId
import java.math.BigInteger

fun Dispatcher.subscribeCommands(){

    commandMenu("subscribe", "💳 Подписка"){
        val user = MinecraftUser.getFromDatabase(message.from?.id?: return@commandMenu)
        if(user == null){
            bot.sendMessage(message.chatId, "Перед оформлении подписки нужно зарегистрироваться командой /register логин пароль")
            return@commandMenu
        }
        val days = user.getSubscriptionDays()
        if(days == null){
            bot.sendMessage(message.chatId, "Для оформления подписки выберите, на какое время оформлять подписку.", replyMarkup = Buttons.payment.buttons)
            return@commandMenu
        }
        bot.sendMessage(message.chatId, "На вашем аккаунте подписка ещё действует $days дней. Вы хотите продлить её?", replyMarkup = Buttons.payment.buttons)
        return@commandMenu
    }

    staticCallbackQuery("1:m"){
        logger.trace("1m")
        bot.sendInvoice(
            ChatId.fromId(callbackQuery.from.id),
            PaymentInvoiceInfo(
                title = "Подписка на MasturLand",
                description = "Подписка для доступа к приватному серверу Майнкрафт на 30 дней.",
                payload = "1",
                providerToken = MineDB.settings.yoomany_prod,
                currency = "RUB",
                prices = listOf(
                    LabeledPrice(
                        label = "Подписка на 30 дней",
                        amount = BigInteger.valueOf(200_00)
                    ),
                ),
                startParameter = "",
                invoiceUserDetail = InvoiceUserDetail(
                    needEmail = true,
                    sendEmailToProvider = true
                ),
                providerData = ProviderData.create(
                    Item(
                        "Игровая подписка на майнкрафт сервер (30 дней)",
                        "1",
                        Amount(
                            currency = "RUB",
                            value = "200.00"
                        ),
                        vat_code = 1
                    )
                ).toJson()
            )
        ).apply {
            println(this)
        }
    }

    staticCallbackQuery("3:m") {
        logger.trace("3m")
        bot.sendInvoice(
            ChatId.fromId(callbackQuery.from.id),
            PaymentInvoiceInfo(
                title = "Подписка на MasturLand",
                description = "Подписка для доступа к приватному серверу Майнкрафт на 90 дней",
                payload = "3",
                providerToken = MineDB.settings.yoomany_prod,
                currency = "RUB",
                prices = listOf(
                    LabeledPrice(
                        label = "Подписка на 90 дней",
                        amount = BigInteger.valueOf(700_00)
                    ),
                ),
                startParameter = "",
                invoiceUserDetail = InvoiceUserDetail(
                    needEmail = true,
                    sendEmailToProvider = true
                ),
                providerData = ProviderData.create(
                    Item(
                        "Игровая подписка на майнкрафт сервер (90 дней)",
                        "1",
                        Amount(
                            currency = "RUB",
                            value = "700.00"
                        ),
                        vat_code = 1
                    )
                ).toJson()
            )
        ).apply {
            println(this)
        }
    }

    preCheckoutQuery {
        println(this.preCheckoutQuery)
        requireNotNull(preCheckoutQuery.orderInfo?.email)
        bot.answerPreCheckoutQuery(preCheckoutQuery.id, true)
        MongoDB.preCheckouts.add(MongoPrecheckout.fromHandler(preCheckoutQuery))
        logger.info("preCheckout complete")
    }

    message(SuccessfulPaymentFilter) {
        logger.info("new SuccessfulPayment payment")
        this.message.successfulPayment?.apply {
            MongoDB.payments.add(MongoPayment.fromMessage(message))
            val user = MinecraftUser.getFromDatabase(message.from!!.id)!!
            when(invoicePayload){
                "1"->{
                    user.addSubscriptionDays(30)
                    bot.sendMessage(user.chatId, "Вы успешно оформили подписку на 30 дней!")
                    logger.info("Проходка на мастурленд 30 дней от user_id: ${user.chatId}")
                }
                "3"->{
                    user.addSubscriptionDays(90)
                    bot.sendMessage(user.chatId, "Вы успешно оформили подписку на 90 дней!")
                    logger.info("Проходка на мастурленд 90 дней от user_id: ${user.chatId}")
                }
            }
        }
    }

    command("promo"){
        val user = MinecraftUser.getFromDatabase(message.from?.id?: return@command)
        if(user == null){
            bot.sendMessage(message.chatId, "Перед активацией промокода нужно зарегистрироваться командой /register логин пароль")
            return@command
        }

        if(args.count() != 1 ){
            bot.sendMessage(message.chatId, "Неверный формат: /promo код")
            return@command
        }

        val input = args.first()

        val code = MongoDB.promos.find(Filters.eq("code", input))
        if(code == null){
            bot.sendMessage(message.chatId, "Промокод не найден.")
            return@command
        }
        if(code.activated_at != null){
            bot.sendMessage(message.chatId, "Промокод уже активирован.")
            return@command
        }

        val split = code.details.split(":")
        val action = split[0]
        val reward = split[1]

        when(action){
            "sub" -> {
                user.addSubscriptionDays(reward.toInt())
                code.activate(user)
                bot.sendMessage(message.chatId, "Промокод на $reward дней успешно активирован. Можете проверить командой /profile")
            }
            else ->{
                bot.sendMessage(message.chatId, "Неизвестный тип промокода.")
                return@command
            }
        }

    }
}

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
fun subscribeCheckerStart(){
    val ctx = newSingleThreadContext("Updater")
    CoroutineScope(ctx).launch {
        while(true){
            delay(10000)
            connection.prepareStatement("select * from users where subscribe_end < now();").use { users ->
                val query = users.executeQuery()
                while(query.next()){
                    val telegram_id = query.getString("telegram_id")
                    val user = MinecraftUser.getFromDatabase(telegram_id.toLong()) ?: return@launch
                    user.unsubscribe()
                    println("[Subscriptions] Expire unsub - ${user.username}")
                    telegramBot.sendMessage(user.chatId, "sub end")
                }
            }
            connection.prepareStatement("update users set subscribe_end = null where subscribe_end < now()").use { query->
                query.execute()
            }
        }
    }
}

