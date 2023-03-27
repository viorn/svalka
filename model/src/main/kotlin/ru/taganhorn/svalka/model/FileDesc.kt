package ru.taganhorn.svalka.model

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import kotlinx.serialization.Serializable

@Serializable
@JsonSerialize
data class FileDesc(
    val path: String,
    val md5: String?,
    val size: ULong,
    val type: Type,
    val thumbnail: String? = null
) {
    val name = path.split("/").lastOrNull()!!
    val parentPath = path.split("/").run {
        take(size - 1)
    }.joinToString("/") { it }
    val level = path.split("/").size
    val url = "/api/files/get/${path}"
    val thumbnailUrl = if (thumbnail!=null) "/api/files/thumbnail/${thumbnail}" else null

    enum class Type {
        FILE, FOLDER
    }
}