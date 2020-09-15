package com.smartmovesystems.keycloak.firebasemigrator.model

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

data class ScryptPasswordCredentialData(
    val hashParametersId: String,
    val hashIterations: Int = 0,
    val algorithm: String = "firebase-scrypt"
) {
    val jsonData: String?
    get() {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val jsonAdapter: JsonAdapter<ScryptPasswordCredentialData> = moshi.adapter(ScryptPasswordCredentialData::class.java)
        return jsonAdapter.toJson(this)
    }
}