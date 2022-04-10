package ru.isrla

import com.mongodb.client.MongoDatabase
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection
import ru.isrla.plugins.*
import ru.isrla.plugins.model.Accommodation
import ru.isrla.plugins.model.Event
import ru.isrla.plugins.model.User

val database: MongoDatabase = KMongo.createClient().getDatabase("sntpu")
val userCollection = database.getCollection<User>("user")
val eventCollection = database.getCollection<Event>("event")
val accommodationCollection = database.getCollection<Accommodation>("accommodation")

fun main() {
    embeddedServer(Netty, port = 8080, host = "localhost") {
        configureMonitoring()
        configureCors()
        configureSerialization()
        configureSecurity()
        configureRouting()
    }.start(wait = true)
}
