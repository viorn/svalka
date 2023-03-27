package ru.taganhorn

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.coroutines.runBlocking
import ru.taganhorn.svalka.db.FileSchema
import ru.taganhorn.svalka.model.FileDesc
import ru.taganhorn.svalka.route.configureFilesRoute
import ru.taganhorn.svalka.route.configureWebUIRoute
import java.io.File

val rootData = "data${File.separator}"
val rootFile = "${rootData}files${File.separator}"
val rootThumbnails = "${rootData}thumbnails${File.separator}"

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module() = runBlocking {
    val fileSchema = FileSchema()
    File(rootFile).mkdirs()
    File(rootThumbnails).mkdirs()

    install(Compression) {
        minimumSize(1024L * 1024)
    }
    install(ContentNegotiation) {
        json()
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }
    configureFilesRoute(fileSchema)
    configureWebUIRoute()
}
