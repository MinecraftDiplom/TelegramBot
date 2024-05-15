plugins {
    kotlin("jvm") version "1.8.21"
    application
    kotlin("plugin.serialization") version "1.8.21"
}

group = "org.koliy82"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    //---------------------- Telegram ---------------------
//    implementation(files("telegram-6.1.0.jar"))
//    implementation("com.google.code.gson:gson:2.8.5")
//    implementation("com.squareup.okhttp3:logging-interceptor:3.8.0")
//    implementation("com.squareup.retrofit2:retrofit:2.9.0")
//    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    //---------------------- Telegram ---------------------
    implementation ("io.github.kotlin-telegram-bot.kotlin-telegram-bot:telegram:6.1.0")
    //---------------------- Serialization ----------------
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("com.beust:klaxon:5.6")
    //---------------------- Retrofit ---------------------
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    //---------------------- MongoDB ----------------------
    implementation("org.mongodb:mongodb-driver-kotlin-coroutine:4.11.0")
    //---------------------- LOGGER  ----------------------
    implementation("org.slf4j:slf4j-simple:2.0.5")
    //---------------------- POSTGRESQL  ----------------------
    implementation("org.postgresql:postgresql:42.5.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    //---------------------- TESTS  -----------------------
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    //---------------------- Dropbox ----------------------
    //    implementation("com.dropbox.core:dropbox-core-sdk:5.4.5")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("org.koliy82.MainKt")
}

tasks.withType<Jar>{
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    configurations["runtimeClasspath"].forEach { file->
        from(zipTree(file.absoluteFile))
    }
    manifest{
        archiveBaseName.set("TelegramBot")
        archiveVersion.set("")
        attributes(mapOf(
            "Main-Class" to "org.koliy82.MainKt",
        ))
    }
//    destinationDirectory.set(file("D:\\build"))
}