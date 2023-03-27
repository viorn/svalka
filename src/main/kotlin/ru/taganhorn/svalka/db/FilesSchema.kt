package ru.taganhorn.svalka.db

import io.ktor.server.util.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import ru.taganhorn.svalka.model.FileDesc

class FileSchema(private val database: Database = defaultDb) {
    object Files : Table() {
        val path = text("path")
        val parent = text("parent").index(isUnique = false)
        val level = integer("level")
        val name = text("name")
        val md5 = varchar("md5", 32).nullable()
        val size = ulong("size")
        val type = enumeration("type", FileDesc.Type::class)
        val thumbnail = text("thumbnail").nullable()

        override val primaryKey = PrimaryKey(path)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Files)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(fileDesc: FileDesc) = dbQuery {
        Files.insert {
            it[path] = fileDesc.path
            it[md5] = fileDesc.md5
            it[parent] = fileDesc.parentPath
            it[name] = fileDesc.name
            it[level] = fileDesc.level
            it[size] = fileDesc.size
            it[type] = fileDesc.type
            it[thumbnail] = fileDesc.thumbnail
        }[Files.path]
    }

    suspend fun get(path: String): FileDesc? {
        return dbQuery {
            Files.select { Files.path eq path }
                .map {
                    FileDesc(
                        path = it[Files.path],
                        md5 = it[Files.md5],
                        size = it[Files.size],
                        type = it[Files.type],
                        thumbnail = it[Files.thumbnail]
                    )
                }
                .singleOrNull()
        }
    }

    suspend fun getByParent(parent: String): List<FileDesc> {
        return dbQuery {
            Files.select { Files.parent eq parent }
                .map {
                    FileDesc(
                        path = it[Files.path],
                        md5 = it[Files.md5],
                        size = it[Files.size],
                        type = it[Files.type],
                        thumbnail = it[Files.thumbnail]
                    )
                }
        }
    }

    suspend fun update(fileDesc: FileDesc) {
        dbQuery {
            Files.update({ Files.path eq fileDesc.path }) {
                it[md5] = fileDesc.md5
                it[size] = fileDesc.size
                it[type] = fileDesc.type
                it[thumbnail] = fileDesc.thumbnail
            }
        }
    }

    suspend fun delete(path: String) {
        dbQuery {
            Files.deleteWhere { Files.path.eq(path) }
        }
    }

    suspend fun deleteAll() {
        dbQuery {
            Files.deleteAll()
        }
    }
}