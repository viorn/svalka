package ru.taganhorn.svalka.route

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.coobird.thumbnailator.Thumbnails
import net.coobird.thumbnailator.name.Rename
import ru.taganhorn.svalka.db.FileSchema
import ru.taganhorn.rootFile
import ru.taganhorn.rootThumbnails
import ru.taganhorn.svalka.model.FileDesc
import ru.taganhorn.svalka.model.HashUtils
import ru.taganhorn.svalka.model.toHex
import java.io.File
import java.nio.file.DirectoryStream
import java.nio.file.Path
import java.security.MessageDigest
import javax.print.attribute.standard.Compression

val IMAGE_EXTS = arrayOf(
    "jpg", "png", "jpeg"
)
val createFoldersMutex = Mutex()
private suspend fun createFolders(fileSchema: FileSchema, parentFile: FileDesc) = createFoldersMutex.withLock {
    var parentFile = parentFile
    while (parentFile.level > 1) {
        parentFile = FileDesc(
            path = parentFile.parentPath,
            md5 = null,
            size = 0u,
            type = FileDesc.Type.FOLDER
        )
        if (fileSchema.get(parentFile.path) == null) {
            fileSchema.create(parentFile)
        } else {
            break
        }
    }
}

fun Application.configureFilesRoute(
    fileSchema: FileSchema
) {
    routing {
        route("api/files") {
            post("upload") {
                val originMd5 = call.request.header("md5")
                val dstPath = call.request.header("path")!!
                val filePath = rootFile + dstPath
                if (fileSchema.get(dstPath) != null) {
                    call.respond(HttpStatusCode.InternalServerError, "duplicate file")
                    return@post
                }
                val mFile = File(filePath)
                mFile.parentFile.mkdirs()
                val md = MessageDigest.getInstance("MD5")
                var size: ULong = 0u
                withContext(Dispatchers.IO) {
                    call.receiveStream().use { stream ->
                        mFile.outputStream().use { fileStream ->
                            do {
                                val read = stream.readNBytes(256 * 1024)
                                fileStream.write(read)
                                md.update(read, 0, read.size)
                                size += read.size.toUInt()
                            } while (read.isNotEmpty())
                        }
                    }
                }
                val md5 = md.digest().toHex()
                if (originMd5 != null && md5 != originMd5) {
                    call.respond(HttpStatusCode.InternalServerError, "checksum doesn't match")
                    mFile.delete()
                    return@post
                }
                val thumbnailUrl = if (IMAGE_EXTS.contains(mFile.extension.lowercase())) {
                    val tFile = File(rootThumbnails + dstPath)
                    tFile.parentFile.mkdirs()
                    Thumbnails.of(mFile)
                        .size(480, 480)
                        .outputFormat("jpg")
                        .toFile(tFile)
                    dstPath
                } else null
                val outFile = FileDesc(
                    path = dstPath,
                    md5 = md5,
                    size = size,
                    type = FileDesc.Type.FILE,
                    thumbnail = thumbnailUrl
                )
                fileSchema.create(outFile)
                createFolders(fileSchema, outFile)
                call.respond(outFile)
            }

            get("folder") {
                val dstPath = call.request.header("folder") ?: ""
                val list = fileSchema.getByParent(dstPath)
                call.respond(list)
            }

            get("get/{urlPath...}") {
                val check = call.parameters["check"] != "false"
                val urlPath = call.parameters.getAll("urlPath")!!.mapNotNull {
                    if (it.isBlank()) return@mapNotNull null
                    return@mapNotNull it
                }
                val dstPath = urlPath.joinToString("/") { it } //call.request.header("path") ?: ""
                val fileDes = fileSchema.get(dstPath)
                val filePath = rootFile + dstPath
                val file = File(filePath)
                if (file.exists()) {
                    if (fileDes?.type == FileDesc.Type.FILE || file.isFile) {
                        if (check) {
                            if (fileDes == null) {
                                val fileDes = FileDesc(
                                    path = dstPath,
                                    md5 = HashUtils.getCheckSumFromFile(MessageDigest.getInstance("MD5"), file),
                                    size = file.length().toULong(),
                                    type = FileDesc.Type.FILE
                                )
                                fileSchema.create(
                                    fileDes
                                )
                                createFolders(fileSchema, fileDes)
                            } else {
                                val md5 = HashUtils.getCheckSumFromFile(
                                    MessageDigest.getInstance("MD5"), file
                                )
                                if (
                                    fileDes.md5 != md5 || file.length().toULong() != fileDes.size
                                ) {
                                    fileSchema.update(
                                        FileDesc(
                                            path = dstPath,
                                            md5 = md5,
                                            size = file.length().toULong(),
                                            type = FileDesc.Type.FILE
                                        )
                                    )
                                }
                            }
                        }
                        call.respondFile(file)
                        return@get
                    } else if (dstPath.isEmpty() || fileDes?.type == FileDesc.Type.FOLDER) {
                        val list = fileSchema.getByParent(dstPath)
                        call.respond(list)
                        return@get
                    } else {
                        call.respond(HttpStatusCode.NotFound, "file not found")
                        return@get
                    }
                } else {
                    call.respond(HttpStatusCode.NotFound, "file not found")
                }
            }

            get("thumbnail/{urlPath...}") {
                val urlPath = call.parameters.getAll("urlPath")!!
                val dstPath = urlPath.joinToString("/") { it } //call.request.header("path") ?: ""
                val filePath = rootThumbnails + dstPath
                val file = File(filePath)
                if (file.exists()) {
                    call.respondFile(file)
                    return@get
                }
                call.respond(HttpStatusCode.NotFound, "file not found")
            }

            get("sync") {
                val dstPath = call.request.header("folder") ?: ""
                val folder = rootFile + dstPath
                fileSchema.deleteAll()
                val files = ArrayList<File>()
                fun scanFolder(folder: File) {
                    folder.list()?.map { File(folder, it) }?.forEach {
                        it.exists()
                        if (!it.isDirectory) {
                            files += it
                        } else {
                            scanFolder(it)
                        }
                    }
                }

                val root = File(folder)
                scanFolder(root)
                val task = arrayListOf<Deferred<Unit>>()
                val folders = hashSetOf<String>()
                files.forEach { file ->
                    task += GlobalScope.async {
                        val dstPath = file.path.removePrefix(rootFile)
                        val thumbnail = if (IMAGE_EXTS.contains(file.extension.lowercase())) {
                            val tPath = rootThumbnails + dstPath
                            val tFile = File(tPath)
                            tFile.parentFile.mkdirs()
                            Thumbnails.of(file)
                                .size(480, 480)
                                .outputFormat("jpg")
                                .toFile(tFile)
                            dstPath
                        } else null
                        val fileDes = FileDesc(
                            path = dstPath,
                            thumbnail = thumbnail,
                            md5 = HashUtils.getCheckSumFromFile(
                                MessageDigest.getInstance("MD5"), file
                            ),
                            size = file.length().toULong(),
                            type = FileDesc.Type.FILE
                        )
                        fileSchema.create(
                            fileDes
                        )
                        folders += fileDes.path.removeSuffix(fileDes.name)
                        //createFolders(fileSchema, fileDes)
                    }
                }
                task.awaitAll()
                folders.filter { it.isNotBlank() }.forEach { folder ->
                    fileSchema.create(
                        FileDesc(
                            path = folder.removeSuffix("/").removePrefix("/"),
                            md5 = null,
                            size = 0u,
                            type = FileDesc.Type.FOLDER
                        )
                    )
                }
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}