package com.smartmovesystems.keycloak.firebasemigrator.model

data class FirebaseUser(
    val email: String,
    val emailVerified: Boolean,
    val passwordHash: String,
    val salt: String,
    val displayName: String?,
    val phoneNumber: String?
)