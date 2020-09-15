package com.smartmovesystems.keycloak.firebasemigrator

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

inline fun <reified T> parseFile(filename: String): T? {
    val text = File(filename).readText()
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    val jsonAdapter: JsonAdapter<T> = moshi.adapter(T::class.java)
    return jsonAdapter.fromJson(text)
}