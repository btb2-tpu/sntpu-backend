package ru.isrla.plugins.model

import org.bson.codecs.pojo.annotations.BsonId
import java.util.*

@kotlinx.serialization.Serializable
data class Event(
    @BsonId
    val uuid: String = UUID.randomUUID().toString(),

    val type: String,
    val creationDate: String,
    val expirationDate: String,

    val header: String = "",
    val description: String = "",

    val avatarUUID: String? = null,
)
