package ru.isrla.plugins

import io.ktor.auth.*
import io.ktor.auth.jwt.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.*

fun Application.configureSecurity() {

    authentication {
        jwt("auth-jwt") {
            verifier(
                JWT
                    .require(Algorithm.HMAC256("secret"))
                    .withIssuer("isrla.ru")
                    .withAudience("isrla.ru")
                    .build()
            )
            validate { credential ->
                val uuid = credential.payload.getClaim("uuid").asString()
                if (uuid.isNotBlank()) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }

}
