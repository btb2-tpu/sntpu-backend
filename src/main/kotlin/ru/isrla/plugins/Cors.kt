package ru.isrla.plugins

import io.ktor.application.*
import io.ktor.features.*

fun Application.configureCors() {
    install(CORS) {
        anyHost()
    }
}
