package ru.isrla.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.request.*
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.replaceOneById
import ru.isrla.accommodationCollection
import ru.isrla.eventCollection
import ru.isrla.plugins.model.Accommodation
import ru.isrla.plugins.model.Event
import ru.isrla.plugins.model.User
import ru.isrla.userCollection
import java.io.File
import java.util.*

fun Application.configureRouting() {

    routing {
        route("/api/v1") {
            /* Auth */
            route("/auth") {
                post("/login") {
                    val user = call.receive<User>()

                    val userFromDb = userCollection.findOne {
                        User::username eq user.username
                        User::password eq user.password
                    } ?: return@post call.respondText(
                        "Invalid username or password",
                        status = HttpStatusCode.BadRequest
                    )

                    val token = JWT.create()
                        .withAudience("isrla.ru")
                        .withIssuer("isrla.ru")
                        .withClaim("uuid", userFromDb.uuid)
                        .withExpiresAt(Date(System.currentTimeMillis() + 36_000_000))
                        .sign(Algorithm.HMAC256("secret"))

                    call.respond(hashMapOf("token" to token))
                }

                post("/register") {
                    val user = call.receive<User>()

                    userCollection.findOne {
                        User::username eq user.username
                    } ?: run {
                        val result = userCollection.insertOne(user)

                        if (result.wasAcknowledged()) {

                            val token = JWT.create()
                                .withAudience("isrla.ru")
                                .withIssuer("isrla.ru")
                                .withClaim("uuid", user.uuid)
                                .withExpiresAt(Date(System.currentTimeMillis() + 36_000_000))
                                .sign(Algorithm.HMAC256("secret"))

                            return@post call.respond(hashMapOf("token" to token))
                        } else {
                            return@post call.respond(HttpStatusCode.InternalServerError)
                        }
                    }

                    return@post call.respond(HttpStatusCode.BadRequest)
                }
            }

            route("/upload") {
                get("{uuid}") {
                    val fileName = call.parameters["uuid"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest)
                    val file = File("C://Users/Qbit982/Downloads/$fileName")
                    if (file.exists()) {
                        call.respondFile(file)
                    }
                    else call.respond(HttpStatusCode.NotFound)
                }
            }

            authenticate("auth-jwt") {
                /* Upload */
                route("/upload") {
                    post {
                        val multipart = call.receiveMultipart()
                        val uuid = UUID.randomUUID().toString()
                        multipart.forEachPart { part ->
                            if(part is PartData.FileItem) {
                                val file = File("C://Users/Qbit982/Downloads/$uuid")

                                part.streamProvider().use { stream ->
                                    file.outputStream().buffered().use {
                                        stream.copyTo(it)
                                    }
                                }
                            }
                            part.dispose()
                        }

                        call.respond(hashMapOf("uuid" to uuid))
                    }
                }

                /* User */
                route("user") {
                    get {
                        val principal = call.principal<JWTPrincipal>()
                            ?: return@get call.respondText("Invalid token", status = HttpStatusCode.BadRequest)
                        val uuid = principal.payload.getClaim("uuid").asString()
                            ?: return@get call.respondText("No such uuid in token", status = HttpStatusCode.BadRequest)
                        val user = userCollection.findOne(User::uuid eq uuid)
                            ?: return@get call.respondText("No such user", status = HttpStatusCode.NotFound)

                        call.respond(user)
                    }

                    put {
                        val user = call.receive<User>()
                        val result = userCollection.replaceOneById(user.uuid, user)

                        if (result.wasAcknowledged()) {
                            call.respond(HttpStatusCode.OK)
                        } else {
                            call.respond(HttpStatusCode.InternalServerError)
                        }
                    }
                }

                /* Event */
                route("event") {
                    get {
                        val events = eventCollection.find().toList()
                        call.respond(events)
                    }

                    get("{uuid}") {
                        val uuid = call.parameters["uuid"] ?: return@get call.respond(HttpStatusCode.BadRequest)

                        val event = eventCollection.findOne {
                            Event::uuid eq uuid
                        } ?: return@get call.respond(HttpStatusCode.NotFound)

                        call.respond(event)
                    }

                    post {
                        val uuid = UUID.randomUUID().toString()
                        val event = call.receive<Event>().copy(uuid = uuid)
                        val result = eventCollection.insertOne(event)

                        if (result.wasAcknowledged()) {
                            call.respond(hashMapOf("uuid" to event.uuid))
                        } else {
                            call.respond(HttpStatusCode.InternalServerError)
                        }
                    }

                    put {
                        val event = call.receive<Event>()
                        val result = eventCollection.replaceOneById(event.uuid, event)

                        if (result.wasAcknowledged()) {
                            call.respond(HttpStatusCode.OK)
                        } else {
                            call.respond(HttpStatusCode.InternalServerError)
                        }
                    }

                    delete("{uuid}") {
                        val uuid = call.parameters["uuid"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                        val result = eventCollection.deleteOne(Event::uuid eq uuid)

                        if (result.wasAcknowledged()) {
                            call.respond(HttpStatusCode.OK)
                        } else {
                            call.respond(HttpStatusCode.InternalServerError)
                        }
                    }
                }

                /* Accommodation */
                route("accommodation") {
                    get {
                        val accommodations = accommodationCollection.find().toList()
                        call.respond(accommodations)
                    }

                    get("{uuid}") {
                        val uuid = call.parameters["uuid"] ?: return@get call.respond(HttpStatusCode.BadRequest)

                        val accommodation = accommodationCollection.findOne {
                            Accommodation::uuid eq uuid
                        } ?: return@get call.respond(HttpStatusCode.NotFound)

                        call.respond(accommodation)
                    }

                    post {
                        val accommodation = call.receive<Accommodation>()
                        val result = accommodationCollection.insertOne(accommodation)

                        if (result.wasAcknowledged()) {
                            call.respond(hashMapOf("uuid" to accommodation.uuid))
                        } else {
                            call.respond(HttpStatusCode.InternalServerError)
                        }
                    }

                    put {
                        val accommodation = call.receive<Accommodation>()
                        val result = accommodationCollection.replaceOneById(accommodation.uuid, accommodation)

                        if (result.wasAcknowledged()) {
                            call.respond(HttpStatusCode.OK)
                        } else {
                            call.respond(HttpStatusCode.InternalServerError)
                        }
                    }

                    delete("{uuid}") {
                        val uuid = call.parameters["uuid"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                        val result = accommodationCollection.deleteOne(Accommodation::uuid eq uuid)

                        if (result.wasAcknowledged()) {
                            call.respond(HttpStatusCode.OK)
                        } else {
                            call.respond(HttpStatusCode.InternalServerError)
                        }
                    }
                }

            }
        }
    }
}
