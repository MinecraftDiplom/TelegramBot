package storage

object KeyManager {
    init{
        if(System.getProperty("os.name").isWindows()){
            System.loadLibrary("libkeys")
        }
        if(System.getProperty("os.name").isUnix()){
            System.load("/telegram/libkeys.so")
            //System.loadLibrary("libkeys")
        }
    }

    fun getKey(key: Keys):String{
        return KeysManager.getKey(key.ordinal)
    }

    private fun String.isWindows(): Boolean {
        return this.lowercase().contains("win")
    }

    private fun String.isUnix(): Boolean {
        return this.lowercase().contains("nix") || this.contains("nux") || this.contains("aix")
    }

}

enum class Keys{
    DiscordMain, DiscordTest, TelegramMain, TelegramTest, Twitch, DropBox
}

