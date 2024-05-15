package bot.commands

import bot.MainLogger.Companion.logger
import bot.callbacks.ContextType
import bot.callbacks.MongoBrakDTO
import bot.callbacks.Pages
import bot.callbacks.msgbox
import bot.commandMenu
import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.ParseMode
import com.mongodb.client.model.*
import com.mongodb.client.model.Aggregates.*
import database.MongoDB
import database.MongoDB.braks
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import models.braks.FamilyTree
import models.mongo.MongoBrak
import models.mongo.MongoUser
import org.bson.Document
import utils.chatId
import utils.getTimeMillis
import utils.telegramTreeFileFromID
import utils.timeToString
import kotlin.random.Random
import kotlin.system.measureTimeMillis

fun Dispatcher.brakCommands() {

    commandMenu("mybrak", "💍 Брак") {
        val fromUser = message.from ?: return@commandMenu
        val brak = MongoBrak.getFromUser(fromUser)
        if (brak == null) {
            bot.sendMessage(message.chatId, "@${fromUser.username}, ты не состоишь в браке. 😥")
            return@commandMenu
        }
        bot.sendMessage(message.chatId, brak.toForm(message.chat.id))
    }

    command("gosex") {
        val fromUser = message.from ?: return@command
        if (message.replyToMessage == null) {
            bot.sendMessage(
                message.chatId,
                "@${fromUser.username}, ответь на любое сообщение партнёра. 😘💬",
                replyToMessageId = message.messageId
            )
            bot.deleteMessage(message.chatId, message.messageId)
            return@command
        }

        val toUser = message.replyToMessage!!.from ?: return@command
        if (fromUser.id == toUser.id) {
            bot.sendMessage(
                message.chatId,
                "@${fromUser.username}, брак с собой нельзя, придётся искать пару. 😥",
                replyToMessageId = message.messageId
            )
            bot.deleteMessage(message.chatId, message.messageId)
            return@command
        }
        if (toUser.isBot) {
            bot.sendMessage(
                message.chatId, "@${fromUser.username}, бота не трогай. 👿", replyToMessageId = message.messageId
            )
            bot.deleteMessage(message.chatId, message.messageId)
            return@command
        }
        if (MongoBrak.getFromUser(fromUser) != null) {
            bot.sendMessage(
                message.chatId, "@${fromUser.username}, у тебя уже есть брак. 👿", replyToMessageId = message.messageId
            )
            bot.deleteMessage(message.chatId, message.messageId)
            return@command
        }
        if (MongoBrak.getFromUser(toUser) != null) {
            bot.sendMessage(
                message.chatId, "@${fromUser.username}, у него уже есть брак. 👿", replyToMessageId = message.messageId
            )
            bot.deleteMessage(message.chatId, message.messageId)
            return@command
        }

        msgbox(message.chatId) {
            deleteTime = 360
            text =
                "\uD83D\uDC8D @${toUser.username}, минуточку внимания.\n" + "\uD83D\uDC96 @${fromUser.username} сделал вам предложение руки и сердца."
            button("Да! ❤\uFE0F\u200D\uD83D\uDD25", ContextType.CHOOSE_ONE, listOf(toUser.id)) {
                braks.add(MongoBrak(fromUser.id.toLong(), toUser.id.toLong()))?.apply {
                    if (isNull.not()) {
                        answerMessage("Внимание! ⚠\uFE0F\n@${fromUser.username} и @${toUser.username} теперь вместе ❤\uFE0F\u200D\uD83D\uDD25")
                        val first = Filters.eq("id", fromUser.id)
                        val second = Filters.eq("id", toUser.id)
                        val query = Filters.or(first, second)
                        val updates = Updates.combine(
                            Updates.inc(MongoUser::braksCount.name, 1),
                        )
                        val options = UpdateOptions().upsert(true)
                        MongoDB.users.collection.updateMany(query, updates, options)
                    } else answerMessage("еррор")
                }
            }
            button("Нет! \uD83D\uDC94", ContextType.CHOOSE_ONE, listOf(toUser.id)) {
                answerMessage("Отказ \uD83D\uDDA4")
            }
        }.sendApplied()
    }

    commandMenu("endsex", "💔 Развод") {
        val user = message.from ?: return@commandMenu
        val brak = MongoBrak.getFromUser(user)
        if (brak == null) {
            bot.sendMessage(
                message.chatId, "@${user.username}, ты не состоишь в браке. 😥", replyToMessageId = message.messageId
            )
            return@commandMenu
        }
        msgbox(message.chatId) {
            text = "@${user.username}, ты уверен? \uD83D\uDC94"
            button("Да.", ContextType.ONE_CLICK, listOf(user.id)) {
                answerMessage("Брак между @${brak.firstUsername()} и @${brak.secondUsername()} распался. \uD83D\uDDA4\nОни прожили вместе ${brak.time.timeToString()}")
                braks.delete(Filters.eq("_id", brak._id))
            }
        }.sendApplied()
    }

    val globalPipeline = listOf(
        lookup("users", "firstUserID", "id", "firstUser"),
        lookup("users", "secondUserID", "id", "secondUser"),
        unwind("\$firstUser"),
        unwind("\$secondUser"),
        lookup("messages", "firstUserID", "from.id", "firstUserMessages"),
        lookup("messages", "secondUserID", "from.id", "secondUserMessages"),
        project(
            Projections.fields(
                Projections.excludeId(),
                Projections.include("firstUserID"),
                Projections.include("secondUserID"),
                Projections.include("firstUser"),
                Projections.include("secondUser"),
                Projections.include("time"),
                Projections.computed(
                    "firstUserMessages",
                    Document(
                        "\$size",
                        Document(
                            "\$filter", Document("input", "\$firstUserMessages")
                                .append("as", "message")
                                .append("cond", Document("\$ne", listOf("\$\$message.chat.type", "private")))
                        )
                    ),
                ),
                Projections.computed(
                    "secondUserMessages",
                    Document(
                        "\$size",
                        Document(
                            "\$filter", Document("input", "\$secondUserMessages")
                                .append("as", "message")
                                .append("cond", Document("\$ne", listOf("\$\$message.chat.type", "private")))
                        )
                    ),
                ),
            )
        ),
        addFields(
            Field("messageCount", Document("\$sum", listOf("\$firstUserMessages", "\$secondUserMessages"))),
        ),
        sort(Sorts.descending("messageCount")),
    )

    commandMenu("braksglobal", "🌍 Браки всех чатов") {
        measureTimeMillis {
            val list = braks.collection.aggregate<MongoBrakDTO>(globalPipeline).toList()
            if (list.isEmpty()) return@commandMenu
            Pages(message.chat.id, listOf(message.from?.id ?: return@commandMenu)).apply {
                fillPages(list) {
                    msgbox(message.chatId) {
                        parseType = ParseMode.HTML
                        text = "\uD83D\uDC8D БРАКИ (${list.size}) \uD83D\uDC8D\n"
                    }
                }
            }.update()
        }.apply {
            logger.info("braks global send $this ms")
        }
    }

    commandMenu("braks", "💬 Браки чата") {
        measureTimeMillis {
            val localPipeline = listOf(
                lookup("users", "firstUserID", "id", "firstUser"),
                lookup("users", "secondUserID", "id", "secondUser"),
                unwind("\$firstUser"),
                unwind("\$secondUser"),
                lookup("messages", "firstUserID", "from.id", "firstUserMessages"),
                lookup("messages", "secondUserID", "from.id", "secondUserMessages"),
                project(
                    Projections.fields(
                        Projections.excludeId(),
                        Projections.include("firstUserID"),
                        Projections.include("secondUserID"),
                        Projections.include("firstUser"),
                        Projections.include("secondUser"),
                        Projections.include("time"),
                        Projections.computed(
                            "firstUserMessages", Document(
                                "\$size", Document(
                                    "\$filter", Document("input", "\$firstUserMessages")
                                        .append("as", "message")
                                        .append(
                                            "cond",
                                            Document("\$eq", listOf("\$\$message.chat.id", message.chat.id))
                                        )
                                )
                            )
                        ),
                        Projections.computed(
                            "secondUserMessages", Document(
                                "\$size", Document(
                                    "\$filter", Document("input", "\$secondUserMessages")
                                        .append("as", "message")
                                        .append(
                                            "cond",
                                            Document("\$eq", listOf("\$\$message.chat.id", message.chat.id))
                                        )
                                )
                            )
                        ),
                    )
                ),
                addFields(
                    Field("messageCount", Document("\$sum", listOf("\$firstUserMessages", "\$secondUserMessages"))),
                ),
                sort(Sorts.descending("messageCount")),
            )

            val list = braks.collection.aggregate<MongoBrakDTO>(localPipeline).toList()
            if (list.isEmpty()) return@commandMenu
            Pages(message.chat.id, listOf(message.from?.id ?: return@commandMenu)).apply {
                fillPages(list) {
                    msgbox(message.chatId) {
                        parseType = ParseMode.HTML
                        text = "\uD83D\uDC8D БРАКИ В ГРУППЕ \uD83D\uDC8D\n"
                    }
                }
            }.update()

        }.apply {
            logger.info("braks local send $this ms")
        }

    }

    command("kid") {
        val fromUser = message.from ?: return@command
        val brak = MongoBrak.getFromUser(fromUser)
        if (brak == null) {
            bot.sendMessage(
                message.chatId,
                "@${fromUser.username}, для рождения ребёнка необходимо состоять в браке. 😥",
                replyToMessageId = message.messageId
            )
            return@command
        }
        if (brak.baby != null) {
            bot.sendMessage(
                message.chatId,
                "@${fromUser.username}, у тебя уже есть @${brak.baby?.username()}. 👪",
                replyToMessageId = message.messageId
            )
            return@command
        }
        if (message.replyToMessage == null) {
            bot.sendMessage(
                message.chatId,
                "@${fromUser.username}, для рождения ребёнка нужно ответить на его сообщение. 😘💬",
                replyToMessageId = message.messageId
            )
            return@command
        }

        val toUser = message.replyToMessage?.from ?: return@command
        if (toUser.isBot) {
            bot.sendMessage(
                message.chatId, "@${fromUser.username}, бота не трож. 👿", replyToMessageId = message.messageId
            )
            return@command
        }
        if (toUser.id == brak.secondUserID || toUser.id == brak.firstUserID) {
            bot.sendMessage(
                message.chatId,
                "@${fromUser.username}, вы не можете родить себя или своего партнёра. 🤔",
                replyToMessageId = message.messageId
            )
            return@command
        }
        if (MongoBrak.getFromKid(toUser) != null) {
            bot.sendMessage(
                message.chatId,
                "@${fromUser.username}, этот ребёнок уже у кого-то родился. 🤔",
                replyToMessageId = message.messageId
            )
            return@command
        }
        if (getTimeMillis() < (brak.time ?: 0L) + 604800000L) {
            bot.sendMessage(
                message.chatId,
                "@${fromUser.username}, для рождения ребёнка вы должны быть женаты неделю. ⌚",
                replyToMessageId = message.messageId
            )
            return@command
        }

        msgbox(message.chatId) {
            deleteTime = 360
            runBlocking {
                text =
                    "@${toUser.username}, тебе предложили родиться в семье @${brak.firstUsername()} и @${brak.secondUsername()}. 🧑🏽‍👩🏽‍🧒🏿"
            }
            button("Родиться! 🤱🏻", ContextType.CHOOSE_ONE, listOf(toUser.id)) {
                brak.createBaby(toUser.id)
                answerMessage("Внимание! ⚠\uFE0F\n@${toUser.username} родился у @${brak.firstUsername()} и @${brak.secondUsername()}! \uD83E\uDD31\uD83C\uDFFB")
            }
            button("Выкидыш! 😶‍🌫️", ContextType.CHOOSE_ONE, listOf(toUser.id)) {
                answerMessage("@${toUser.username} отказался появляться на этот свет. \uD83D\uDC80")
            }
        }.sendApplied()
    }

    command("detdom") {
        val user = message.from ?: return@command
        val brak = MongoBrak.getFromKid(user)
        if (brak == null) {
            bot.sendMessage(
                message.chatId, "@${user.username}, ты ещё не родился. ⌚", replyToMessageId = message.messageId
            )
            return@command
        }
        msgbox(message.chatId) {
            runBlocking {
                text =
                    "@${user.username}, ты точно хочешь уйти в детский дом от @${brak.firstUsername()} и @${brak.secondUsername()}? 🏠"
            }
            button("Да.", ContextType.ONE_CLICK, listOf(user.id)) {
                brak.removeBaby()
                val text = when (Random.nextInt(45)) {
                    in 0..15 -> "@${user.username} шёл по дороге к детскому дому, когда вдруг на него напало стадо белых и пушистых котиков! Они носили его на лапках, подбрасывали в воздух и играли с ним в прятки. Веселая котячья армия проводила его прямо до дверей детского дома, где его ждали с радостью и открытыми объятиями."
                    in 16..20 -> "@${user.username} решил бежать от своих родителей и отправиться в детский дом, мечтая о лучшей жизни и большей заботе. Он собрал свои немногочисленные вещи и тихонько вышел из дома в темноте. По пути он столкнулся с непредвиденными преградами, но его решимость не ослабевала. Однако, в долине, которую он пытался пересечь, случился сильный ливень. Ребёнок оказался в беде и без надежды на помощь. Он лежал на земле, мокрый и испуганный, пока его силы постепенно оставляли его тело. Никто не знал о его печали и потере, и его мечты о лучшей жизни исчезли вместе с его последним дыханием."
                    in 21..30 -> "@${user.username} устав от бесконечных правил и запретов, которые накладывали на него родители, решил покинуть свой дом и отправиться в детский дом.  Когда он пришёл туда, сотрудники были поражены его решимостью и сказали: \"Мы рады принять тебя с открытыми объятиями, малыш! Здесь ты найдёшь новый дом и новую семью, которая будет заботиться о тебе.\" Ребёнок улыбнулся и понял, что он сделал правильный выбор, и вместе с новыми друзьями начал своё новое приключение в детском доме."
                    in 31..40 -> "@${user.username} шёл на концерт Моргенштерна, он был настолько взбудоражен и восторжен, что его энергия переполняла его самого. Он подпрыгивал и танцевал на своем пути, неся с собой невероятное веселье. Но внезапно его энтузиазм перешел в пределы возможного, и он начал сверкать ярким светом, превращаясь в маленькую звезду. Все, кто видел это чудо, восхищенно замирали, понимая, что это было что-то особенное. И хотя его приключение закончилось раньше времени, ребёнок оставил память о своей неподражаемой энергии и радости в сердцах всех, кто видел его."
                    in 41..45 -> "@${user.username}, шёл к детскому дому, неся с собой свою крутую механику из Dota 2. Внезапно, перед ним выскочил сильный вражеский герой, и ребёнок сразу же активировал свои навыки. Он прыгнул в воздух, развернулся и нанёс мощный удар, отправляя врага в космос. Прохожие ошарашено остановились, а ребёнок уверенно продолжил свой путь, собирая аплодисменты и восхищённые взгляды."
                    else -> ""
                }
                answerMessage(text)
            }
        }.sendApplied()
    }

    command("kidannihilate") {
        val user = message.from ?: return@command
        val brak = MongoBrak.getFromUser(user)
        if (brak == null) {
            bot.sendMessage(
                message.chatId,
                "@${user.username}, ты не состоишь в браке. \uD83D\uDE25",
                replyToMessageId = message.messageId
            )
            return@command
        }
        val baby = brak.baby
        if (baby == null) {
            bot.sendMessage(
                message.chatId, "@${user.username}, у вас пока нет детей. 🤔", replyToMessageId = message.messageId
            )
            return@command
        }
        val partner = brak.partner(user.id) ?: return@command
        msgbox(message.chatId) {
            deleteTime = 360
            runBlocking {
                text = "@${partner.username}, ты тоже хочешь аннигилировать @${baby.username()}? 😐"
            }
            button("Да.", ContextType.ONE_CLICK, listOf(partner.id)) {
                brak.removeBaby()
                answerMessage("Внимание! ⚠\uFE0F\n@${brak.baby?.username()} был аннигилирован @${user.username} и @${partner.username}! Он прожил ${brak.baby?.timeToString()}")
            }
        }.sendApplied()
    }

    commandMenu("treetext", "🌱 Древо (текст)") {
        bot.sendMessage(
            message.chatId, FamilyTree.create(message.from?.id ?: -1).build(), replyToMessageId = message.messageId
        )
    }

    commandMenu("treeimage", "🌳 Древо (картинка)") {
        bot.sendPhoto(
            message.chatId, telegramTreeFileFromID(message.from?.id ?: -1), replyToMessageId = message.messageId
        )
    }

    commandMenu("profile", "👤 Профиль") {
        val user = message.from ?: return@commandMenu
        MongoUser.getFromUserID(user.id)?.let {
            bot.sendMessage(
                message.chatId,
                it.profile(message.chat.id),
                parseMode = ParseMode.MARKDOWN_V2,
                disableWebPagePreview = false
            )
        }
    }
}