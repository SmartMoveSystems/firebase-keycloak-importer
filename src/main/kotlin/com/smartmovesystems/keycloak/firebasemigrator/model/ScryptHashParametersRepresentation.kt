package com.smartmovesystems.keycloak.firebasemigrator.model

data class ScryptHashParametersRepresentation(
    val rounds: Int,
    val memCost: Int,
    val base64Signer: String,
    val saltSeparator: String,
    val isDefault: Boolean = false,
    val id: String? = null
)