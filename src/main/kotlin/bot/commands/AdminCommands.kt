package bot.commands

import bot.MainLogger.Companion.logger
import bot.adminCommand
import bot.arguments
import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.payments.InvoiceUserDetail
import com.github.kotlintelegrambot.entities.payments.LabeledPrice
import com.github.kotlintelegrambot.entities.payments.PaymentInvoiceInfo
import com.mongodb.client.model.Filters
import database.MineDB
import database.MongoDB
import models.invoice.Amount
import models.invoice.Item
import models.invoice.ProviderData
import models.mongo.MongoPromocode
import models.telegram.ids.GroupIds
import models.telegram.ids.UserIds
import storage.AdminsDB
import storage.ChatWhiteListDB
import storage.RuleDB
import utils.chatId
import java.math.BigInteger

fun Dispatcher.adminCommands(){

    adminCommand("/text"){
        bot.deleteMessage(message.chatId, message.messageId)
        bot.sendMessage(message.chatId, text = message.arguments().joinToString(" "))
    }

    adminCommand("/chat"){
        bot.deleteMessage(message.chatId, message.messageId)
        val args = message.arguments()
        val chatId = ChatId.fromId(UserIds.koliy82.id)
        when(args[0]){
            "add"->{
                val text = ChatWhiteListDB.op(message.chat)
                logger.info(text)
                bot.sendMessage(chatId, text)
            }
            "remove"->{
                val text = ChatWhiteListDB.deop(message.chat.id)
                logger.info(text)
                bot.sendMessage(chatId, text)
            }
            "list"->{
                bot.sendMessage(chatId, ChatWhiteListDB.chats().joinToString { "${it.title}\n" })
            }
            "check"->{
                bot.sendMessage(chatId, message.chat.toString())
            }
            "id"->{
                logger.info(message.chat.id.toString())
            }
            "off"->{
                val text = ChatWhiteListDB.off()
                logger.info(text)
                bot.sendMessage(chatId, text)
            }
            "on"->{
                val text = ChatWhiteListDB.on()
                logger.info(text)
                bot.sendMessage(chatId, text)
            }
        }
    }

    adminCommand("/createpromo"){
        val args = message.arguments()
        try { args[0].toInt() } catch (e:Exception){
            bot.sendMessage(message.chatId, text = "Неправильный формат: /createpromo дни код")
            return@adminCommand
        }
        val code = when(args.count()){
            1 -> MongoPromocode.generateSubCode(args[0].toInt())
            2 -> MongoPromocode.customSubCode(args[0], args[1])
            else -> {
                bot.sendMessage(message.chatId, text = "Неправильный формат: /createpromo дни код")
                return@adminCommand
            }
        }
        try {
            logger.info(code.toString())
            if(MongoDB.promos.collection.countDocuments(Filters.eq("code", code.code)) != 0L){
                logger.warn("Промокод не уникален")
                bot.sendMessage(message.chatId, text = "Промокод не уникальный")
                return@adminCommand
            }
            MongoDB.promos.collection.insertOne(code).insertedId
            bot.sendMessage(message.chatId, text = "Промокод успешно создан: `${code.code}`", parseMode = ParseMode.MARKDOWN)
        }catch (e: Exception){
            bot.sendMessage(message.chatId, text = "Ошибка.")
            logger.error(e.message)
            return@adminCommand
        }
    }

    adminCommand("/op"){
        val reply = message.replyToMessage
        if(reply == null){
            bot.sendMessage(ChatId.fromId(message.chat.id), text = "need reply to message")
            return@adminCommand
        }
        val result = AdminsDB.op(reply.from?.id ?: return@adminCommand)
        bot.sendMessage(ChatId.fromId(message.chat.id), text = result)
    }

    adminCommand("/deop"){
        val reply = message.replyToMessage
        if(reply == null){
            bot.sendMessage(ChatId.fromId(message.chat.id), text = "need reply to message")
            return@adminCommand
        }
        val result = AdminsDB.deop(reply.from?.id ?: return@adminCommand)
        bot.sendMessage(ChatId.fromId(message.chat.id), text = result)
    }

    adminCommand("/testsub"){
        logger.info(
            ProviderData.create(
                Item(
                    description = "Игровая подписка на майнкрафт сервер (30 дней)",
                    quantity = "1",
                    amount = Amount(
                        currency = "RUB",
                        value = "60.00"
                    ),
                    vat_code = 1
                )
            ).toJson()
        )
        bot.sendInvoice(
            ChatId.fromId(message.from?.id?:return@adminCommand),
            PaymentInvoiceInfo(
                title = "Подписка на MasturLand",
                description = "Подписка для доступа к приватному серверу Майнкрафт на 30 дней.",
                payload = "1",
                providerToken = MineDB.settings.yoomany_test,
                currency = "RUB",
                prices = listOf(
                    LabeledPrice(
                        label = "Подписка на 30 дней",
                        amount = BigInteger.valueOf(60_00)
                    ),
                ),
                startParameter = "",
                invoiceUserDetail = InvoiceUserDetail(
                    needEmail = true,
                    sendEmailToProvider = true
                ),
                providerData = ProviderData.create(
                    Item(
                        description = "Игровая подписка на майнкрафт сервер (30 дней)",
                        quantity = "1",
                        amount = Amount(
                            currency = "RUB",
                            value = "60.00"
                        ),
                        vat_code = 1
                    )
                ).toJson()
            )
        )
    }

    adminCommand("/spam"){
        val reply = message.replyToMessage
        if(reply == null){
            bot.sendMessage(ChatId.fromId(message.chat.id), text = "need reply to message")
            return@adminCommand
        }
        if(reply.text == null){
            bot.sendMessage(ChatId.fromId(message.chat.id), text = "reply text is null")
            return@adminCommand
        }
        val urls = RuleDB.getUrls(reply)
        println(urls)
        if(urls.isEmpty()){
            bot.sendMessage(ChatId.fromId(message.chat.id), text = "urls is empty")
            return@adminCommand
        }
        urls.forEach{
            if(RuleDB.exist(it).not()){
                RuleDB.add(it)
                logger.info("added $it")
            }
        }
        bot.copyMessage(ChatId.fromId(GroupIds.logs.id), ChatId.fromId(reply.chat.id), reply.messageId)
        bot.deleteMessage(ChatId.fromId(reply.chat.id), reply.messageId)
        bot.sendMessage(ChatId.fromId(GroupIds.logs.id), text = "[#ADDRULE] Добавлено правило в бан-лист: ${urls.joinToString(separator = ", ")}")
    }

}