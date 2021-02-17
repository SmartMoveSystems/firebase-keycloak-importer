package com.smartmovesystems.keycloak.firebasemigrator

import org.jboss.resteasy.client.jaxrs.ResteasyClient
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget
import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.JacksonProvider
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import javax.ws.rs.client.ClientRequestContext
import javax.ws.rs.client.ClientRequestFilter

private val Keycloak.webTarget : ResteasyWebTarget
    get() {
        val targetField = this::class.java.getDeclaredField("target")
        targetField.isAccessible = true
        return targetField.get(this) as ResteasyWebTarget
    }

/**
 * Making repeated calls to the RestEasyClient seems to eventually exhaust the client pool, regardless of size and
 * configuration. Therefore, request the server close connections aggressively to avoid hangs
 */
private fun Keycloak.setCloseConnections() {
    this.webTarget.register(object : ClientRequestFilter {
        override fun filter(requestContext: ClientRequestContext?) {
            requestContext!!.headers.add("Connection", "close")
        }
    })
}

private val resteasyClientBuilder: ResteasyClientBuilder by lazy {
    ResteasyClientBuilder()
        .register(JacksonProvider::class.java, 100)
}

private val restEasyClient: ResteasyClient by lazy {
    resteasyClientBuilder.useAsyncHttpEngine().build()
}

/**
 * Returns a logged-in Keycloak instance based on the provided arguments
 */
fun getKcInstance(arguments: Arguments): Keycloak {
    val keycloak = KeycloakBuilder.builder()
        .serverUrl(arguments.serverUrl)
        .realm(arguments.realm)
        .grantType(OAuth2Constants.PASSWORD)
        .clientId("admin-cli")
        .clientSecret(arguments.clientSecret)
        .resteasyClient(restEasyClient)
        .username(arguments.adminUser)
        .password(arguments.adminPassword)
        .build()
    keycloak.setCloseConnections()
    return keycloak
}