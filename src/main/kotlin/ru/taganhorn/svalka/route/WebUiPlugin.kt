package ru.taganhorn.svalka.route

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import ru.taganhorn.svalka.db.FileSchema
import java.io.File

fun Application.configureWebUIRoute() {
    routing {
        static("/ui/") {
            staticRootFolder = File("web")
            files(".")
        }
    }
}