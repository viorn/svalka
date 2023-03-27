package ru.taganhorn.svalka.examples

import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import io.ktor.server.application.*

fun Application.configureHTTP() {
    routing {
        swaggerUI(path = "openapi")
    }
}
