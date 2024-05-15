package database

import bot.MainLogger.Companion.logger
import models.BotSettings
import java.sql.DriverManager

object MineDB {
    val settings: BotSettings = BotSettings.parseFromFile()
    val connection = DriverManager.getConnection(settings.jdbcUrl, settings.postgres_user, settings.postgres_password)

    init {
//        println("[Minecraft] Connect to database - "+ connection.isValid(0))
        logger.info("[Minecraft] Connect to database - "+ connection.isValid(0))
    }
}