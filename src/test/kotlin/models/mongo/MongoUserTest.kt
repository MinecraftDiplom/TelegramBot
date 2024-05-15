package models.mongo

import bot.botStart
import bot.telegramBot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import models.telegram.ids.GroupIds
import models.telegram.ids.UserIds
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import storage.ChatWhiteListDB
import storage.KeyManager
import storage.Keys

class MongoUserTest {
    private val testScope = TestScope()
    companion object {
        @JvmStatic
        @BeforeAll
        fun beforeAll(){
            botStart(KeyManager.getKey(Keys.TelegramTest))
        }

        @JvmStatic
        @AfterAll
        fun afterAll() {
            println("Tests completed")
        }
    }

    @Test
    fun `profile markdown is send`() = testScope.runTest {
        val userId = UserIds.banshi.id
        val groupId = GroupIds.hinkal.id

        val user = MongoUser.getFromUserID(userId)
        requireNotNull(user)

        val profile = user.profile(groupId)
        val result = telegramBot.sendMessage(
            ChatId.fromId(UserIds.koliy82.id), profile, parseMode = ParseMode.MARKDOWN_V2
        ).isError

        assertFalse(result)
    }

    @Test
    fun `whitelist check`(){
        val chats = ChatWhiteListDB.list
        chats.forEach {
            val result = ChatWhiteListDB.isWhitelisted(it.id)
            assertTrue(result)
        }
    }

}