package ru.isrla.plugins.model

import org.bson.codecs.pojo.annotations.BsonId
import java.util.*

@kotlinx.serialization.Serializable
data class User(
    @BsonId
    val uuid: String = UUID.randomUUID().toString(),

    val username: String,
    val password: String,

    val firstName: String = "",
    val lastName: String = "",
    val middleName: String = "",
    val description: String = "",
    val group: String = "",
    val school: String = "",
    val education: String = "",
    val floor: String = "",
    val dormitory: String = "",
    val room: String = "",

    val avatarUUID: String? = null,
)
