package ru.isrla.plugins.model

import org.bson.codecs.pojo.annotations.BsonId
import java.util.UUID

@kotlinx.serialization.Serializable
data class Accommodation(
    @BsonId
    val uuid: String = UUID.randomUUID().toString(),

    val type: AccommodationType,
    val name: String,
)

enum class AccommodationType {
    Dormitory;
}
