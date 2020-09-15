package com.smartmovesystems.keycloak.firebasemigrator.model

data class FirebaseHashParameters(
    val base64_signer_key: String,
    val base64_salt_separator: String,
    val rounds: Int,
    val mem_cost: Int
) {
    fun toRepresentation(default: Boolean = false): ScryptHashParametersRepresentation {
        return ScryptHashParametersRepresentation(
            rounds,
            mem_cost,
            base64_signer_key,
            base64_salt_separator,
            default
        )
    }
}