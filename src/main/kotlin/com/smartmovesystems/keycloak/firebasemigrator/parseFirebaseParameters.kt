package com.smartmovesystems.keycloak.firebasemigrator

import com.smartmovesystems.keycloak.firebasemigrator.model.FirebaseHashConfig
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

fun parseFirebaseHashParameters(filename: String): FirebaseHashConfig? {
    val text = File(filename).readText()
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    val jsonAdapter: JsonAdapter<FirebaseHashConfig> = moshi.adapter(FirebaseHashConfig::class.java)
    return jsonAdapter.fromJson(text)
}