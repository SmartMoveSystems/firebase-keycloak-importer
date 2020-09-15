package com.smartmovesystems.keycloak.firebasemigrator

import com.smartmovesystems.keycloak.firebasemigrator.model.FirebaseHashParameters
import org.keycloak.admin.client.CreatedResponseUtil
import org.keycloak.admin.client.Keycloak
import java.net.URI

fun createHashParameters(keycloak: Keycloak, realm: String, serverUrl: String, params: FirebaseHashParameters, default: Boolean = false): String {
    val uri = URI("$serverUrl/realms/$realm")
    val response = keycloak.proxy(HashParametersResource::class.java, uri)
        .createHashParameters(params.toRepresentation(default))
    return CreatedResponseUtil.getCreatedId(response);
}