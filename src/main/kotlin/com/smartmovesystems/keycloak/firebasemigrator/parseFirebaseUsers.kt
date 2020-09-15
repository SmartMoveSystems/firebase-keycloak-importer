package com.smartmovesystems.keycloak.firebasemigrator

import com.smartmovesystems.keycloak.firebasemigrator.model.UserList
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

fun parseFirebaseUsers(filename: String): UserList? {
    val text = File(filename).readText()
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    val jsonAdapter: JsonAdapter<UserList> = moshi.adapter(UserList::class.java)
    return jsonAdapter.fromJson(text)
}