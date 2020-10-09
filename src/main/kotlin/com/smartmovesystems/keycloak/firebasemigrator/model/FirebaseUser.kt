package com.smartmovesystems.keycloak.firebasemigrator.model

data class FirebaseUser(
    val email: String,
    val emailVerified: Boolean,
    val salt: String? = null,
    val displayName: String? = null,
    val phoneNumber: String? = null,
    val passwordHash: String? = null,
    val customAttributes: String? = null
)