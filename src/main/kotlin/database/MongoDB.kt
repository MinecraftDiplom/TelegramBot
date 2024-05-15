package database

import bot.MainLogger.Companion.logger
import kotlinx.coroutines.runBlocking
import models.mongo.*

object MongoDB {
    private val _users = MongoDatabase("users", MongoUser::class)
    private val _braks = MongoDatabase("braks", MongoBrak::class)
    private val _messages = MongoDatabase("messages", MongoMessage::class)
    private val _payments = MongoDatabase("payments", MongoPayment::class)
    private val _preCheckouts = MongoDatabase("checkouts", MongoPrecheckout::class)
    private val _promos = MongoDatabase("promo", MongoPromocode::class)
    val users get() = _users
    val braks get() = _braks
    val messages get() = _messages
    val payments get() = _payments
    val preCheckouts get() = _preCheckouts
    val promos get() = _promos


    init {
        _users.initialize()
        _braks.initialize(MongoBaby::class)
        _messages.initialize()
        _payments.initialize()
        _promos.initialize()
        _preCheckouts.initialize()
        runBlocking {
            _users.indexNumber("id")
            _braks.indexCompound("firstUserID", "secondUserID")
            _braks.indexNumber("baby.userID")
            _messages.indexNumber("from.id")
            _messages.indexNumber("chat.id")
            _messages.indexText("chat.type")
            _payments.indexNumber("from.id")
            _promos.indexText("code")
        }
        logger.info("[MongoDB] database initialized")
    }

}