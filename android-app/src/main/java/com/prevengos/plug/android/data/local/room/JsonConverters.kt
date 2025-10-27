package com.prevengos.plug.android.data.local.room

import androidx.room.TypeConverter
import com.prevengos.plug.android.data.local.entity.RespuestaLocal
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class JsonConverters {
    private val moshi: Moshi = Moshi.Builder().build()
    private val respuestasAdapter: JsonAdapter<List<RespuestaLocal>> =
        moshi.adapter(Types.newParameterizedType(List::class.java, RespuestaLocal::class.java))
    private val listStringAdapter: JsonAdapter<List<String>> =
        moshi.adapter(Types.newParameterizedType(List::class.java, String::class.java))
    private val metadataAdapter: JsonAdapter<Map<String, String>> =
        moshi.adapter(Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))

    @TypeConverter
    fun fromRespuestas(value: List<RespuestaLocal>?): String? = value?.let { respuestasAdapter.toJson(it) }

    @TypeConverter
    fun toRespuestas(value: String?): List<RespuestaLocal> = value?.let {
        respuestasAdapter.fromJson(it) ?: emptyList()
    } ?: emptyList()

    @TypeConverter
    fun fromStringList(value: List<String>?): String? = value?.let { listStringAdapter.toJson(it) }

    @TypeConverter
    fun toStringList(value: String?): List<String> = value?.let {
        listStringAdapter.fromJson(it) ?: emptyList()
    } ?: emptyList()

    @TypeConverter
    fun fromMetadata(value: Map<String, String>?): String? = value?.let { metadataAdapter.toJson(it) }

    @TypeConverter
    fun toMetadata(value: String?): Map<String, String> = value?.let {
        metadataAdapter.fromJson(it) ?: emptyMap()
    } ?: emptyMap()
}
