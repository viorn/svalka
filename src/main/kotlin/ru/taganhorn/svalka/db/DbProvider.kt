package ru.taganhorn.svalka.db

import org.jetbrains.exposed.sql.Database
import ru.taganhorn.rootData

val defaultDb by lazy {
    Database.connect(
        //url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        url = "jdbc:h2:./${rootData}database",
        user = "root",
        driver = "org.h2.Driver",
        password = "wwsdcx"
    )
}