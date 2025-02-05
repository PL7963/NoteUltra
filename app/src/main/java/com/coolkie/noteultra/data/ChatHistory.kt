package com.coolkie.noteultra.data

import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.HnswIndex
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.converter.PropertyConverter
import java.time.LocalDate
import java.time.LocalTime

@Entity
data class ChatHistory(
    @Id
    var id: Long = 0,

    @Convert(converter = LocalDateConverter::class, dbType = Int::class)
    @Index
    var date: LocalDate,

    @Convert(converter = LocalTimeConverter::class, dbType = Int::class)
    var time: LocalTime,

    var content: String,

    @HnswIndex(dimensions = 100) val contentVector: FloatArray? //umm 100(? weird number
)

class LocalDateConverter : PropertyConverter<LocalDate, Int> {
    override fun convertToDatabaseValue(entityProperty: LocalDate?): Int? {
        return entityProperty?.toEpochDay()?.toInt()
    }

    override fun convertToEntityProperty(databaseValue: Int?): LocalDate? {
        if (databaseValue != null) {
            return LocalDate.ofEpochDay(databaseValue.toLong())
        }
        return null
    }
}

class LocalTimeConverter : PropertyConverter<LocalTime, Int> {
    override fun convertToDatabaseValue(entityProperty: LocalTime?): Int? {
        return entityProperty?.toSecondOfDay()
    }

    override fun convertToEntityProperty(databaseValue: Int?): LocalTime? {
        if (databaseValue != null) {
            return LocalTime.ofSecondOfDay(databaseValue.toLong())
        }
        return null
    }
}