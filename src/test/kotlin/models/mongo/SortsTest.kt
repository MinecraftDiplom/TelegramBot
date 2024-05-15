package models.mongo

import bot.TimSort
import bot.callbacks.*
import com.github.kotlintelegrambot.entities.ChatId
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Projections
import com.mongodb.client.model.Sorts
import database.MongoDB
import database.MongoDB.braks
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import models.telegram.ids.GroupIds
import org.bson.Document
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled("disable")
class SortsTest {
    init {
        MongoDB
    }
    private val testScope = TestScope()

    @Test
    fun `default sort`() = testScope.runTest {
        val pages = mutableListOf<Page>()

        val pipelineGlobal = listOf(
            Aggregates.lookup("users", "firstUserID", "id", "firstUser"),
            Aggregates.lookup("users", "secondUserID", "id", "secondUser"),
            Aggregates.unwind("\$firstUser"),
            Aggregates.unwind("\$secondUser"),
            Aggregates.lookup("messages", "firstUserID", "from.id", "firstUserMessages"),
            Aggregates.lookup("messages", "secondUserID", "from.id", "secondUserMessages"),
            Aggregates.project(
                Projections.fields(
                    Projections.excludeId(),
                    Projections.include("firstUser"),
                    Projections.include("secondUser"),
                    Projections.computed("firstUserMessages", Document("\$size", "\$firstUserMessages")),
                    Projections.computed("secondUserMessages", Document("\$size", "\$secondUserMessages")),
                )
            ),
            Aggregates.sort(Sorts.descending("firstUserMessages", "secondUserMessages")),
        )

        val list = braks.collection.aggregate<MongoBrakDTO>(pipelineGlobal).toList()
        val chatId = GroupIds.hinkal.id
        val curMessageBox = MessageBox(ChatId.fromId(chatId))

        require(list.isNotEmpty())
        val temp = mutableListOf<MongoBrakDTO>()
        list.forEach {
            if(it.messageCount != 0L) temp += it
        }
        var index = 1
        temp.sortedByDescending { it.messageCount }.chunked(10).forEach {
            val nPage = msgbox(ChatId.fromId(chatId)){}
            it.forEach {
                nPage.text += it.statistic((index++))+"\n"
            }
            pages += Page(nPage)
        }
        if(pages.size>1){
            curMessageBox.button("⬅️", ContextType.TEMPORARY){}
            curMessageBox.button("1", ContextType.TEMPORARY){}
            curMessageBox.button("➡️", ContextType.TEMPORARY){}
        }

        assertTrue(pages.isNotEmpty())
    }

    data class MongoBrakCachedSort(val obj:MongoBrak, val count:Long) : Comparable<MongoBrakCachedSort> {
        override fun compareTo(other: MongoBrakCachedSort): Int {
            return count.compareTo(other.count)
        }

    }

    private suspend fun List<MongoBrak>.mapping() : List<MongoBrakCachedSort> {
        val map = mutableListOf<MongoBrakCachedSort>()
        for (a in this) {
            map += MongoBrakCachedSort(a, a.messageCount())
        }
        return map
    }

    @Test
    fun `tim sort`() = testScope.runTest {
        val pages = mutableListOf<Page>()
        val chatId = GroupIds.hinkal.id
        val curMessageBox = MessageBox(ChatId.fromId(chatId))

        val list = MongoDB.braks.collection.find().toList().mapping()
        require(list.isNotEmpty())

        val tim = TimSort<MongoBrakCachedSort>()
        val arr = list.toTypedArray()

        tim.sort(arr)

        var index = 1
        arr.toList().chunked(10).forEach {
            val nPage = msgbox(ChatId.fromId(chatId)){}
            it.forEach {
                nPage.text += it.obj.statistic((index++))+"\n"
            }
            pages += Page(nPage)
        }

        if(pages.size>1){
            curMessageBox.button("⬅️", ContextType.TEMPORARY){}
            curMessageBox.button("1", ContextType.TEMPORARY){}
            curMessageBox.button("➡️", ContextType.TEMPORARY){}
        }

        assertTrue(pages.isNotEmpty())
    }

    companion object {
        @JvmStatic
        @AfterAll
        fun afterAll() {
            println("Tests completed")
        }
    }


}