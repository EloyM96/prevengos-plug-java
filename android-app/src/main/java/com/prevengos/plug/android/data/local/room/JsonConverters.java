package com.prevengos.plug.android.data.local.room;

import androidx.room.TypeConverter;

import com.prevengos.plug.android.data.local.entity.RespuestaLocal;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JsonConverters {
    private final Moshi moshi;
    private final JsonAdapter<List<RespuestaLocal>> respuestasAdapter;
    private final JsonAdapter<List<String>> listStringAdapter;
    private final JsonAdapter<Map<String, String>> metadataAdapter;

    public JsonConverters() {
        moshi = new Moshi.Builder().build();
        Type respuestasType = Types.newParameterizedType(List.class, RespuestaLocal.class);
        respuestasAdapter = moshi.adapter(respuestasType);
        Type listStringType = Types.newParameterizedType(List.class, String.class);
        listStringAdapter = moshi.adapter(listStringType);
        Type metadataType = Types.newParameterizedType(Map.class, String.class, String.class);
        metadataAdapter = moshi.adapter(metadataType);
    }

    @TypeConverter
    public String fromRespuestas(List<RespuestaLocal> value) {
        if (value == null) {
            return null;
        }
        return respuestasAdapter.toJson(value);
    }

    @TypeConverter
    public List<RespuestaLocal> toRespuestas(String value) {
        if (value == null) {
            return Collections.emptyList();
        }
        List<RespuestaLocal> parsed = respuestasAdapter.fromJson(value);
        return parsed == null ? Collections.emptyList() : parsed;
    }

    @TypeConverter
    public String fromStringList(List<String> value) {
        if (value == null) {
            return null;
        }
        return listStringAdapter.toJson(value);
    }

    @TypeConverter
    public List<String> toStringList(String value) {
        if (value == null) {
            return Collections.emptyList();
        }
        List<String> parsed = listStringAdapter.fromJson(value);
        return parsed == null ? Collections.emptyList() : parsed;
    }

    @TypeConverter
    public String fromMetadata(Map<String, String> value) {
        if (value == null) {
            return null;
        }
        return metadataAdapter.toJson(value);
    }

    @TypeConverter
    public Map<String, String> toMetadata(String value) {
        if (value == null) {
            return Collections.emptyMap();
        }
        Map<String, String> parsed = metadataAdapter.fromJson(value);
        return parsed == null ? Collections.emptyMap() : parsed;
    }
}
