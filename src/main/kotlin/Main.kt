package org.koliy82

import bot.MainLogger.Companion.logger
import bot.botStart
import storage.KeyManager
import storage.Keys
import utils.consoleStart

fun main() {
    logger.info(System.getProperty("os.name"))
    botStart(KeyManager.getKey(Keys.TelegramMain))
    consoleStart()
}