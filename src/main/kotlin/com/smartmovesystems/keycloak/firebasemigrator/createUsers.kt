package com.smartmovesystems.keycloak.firebasemigrator

import com.smartmovesystems.keycloak.firebasemigrator.model.FirebaseUser
import com.smartmovesystems.keycloak.firebasemigrator.model.ScryptPasswordCredentialData
import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.CreatedResponseUtil
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.admin.client.resource.RealmResource
import org.keycloak.admin.client.resource.UserResource
import org.keycloak.admin.client.resource.UsersResource
import org.keycloak.representations.idm.ClientRepresentation
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation
import java.lang.Exception
import java.util.logging.Logger
import javax.ws.rs.core.Response

private val log = Logger.getLogger("createUsers")

fun createUsers(arguments: Arguments) {

    log.info("Parsing users file...")
    val users = parseFirebaseUsers(arguments.fileName)

    if (users != null) {

        log.info("Found ${users.users.size} users in file.")
        val keycloak = KeycloakBuilder.builder()
            .serverUrl(arguments.serverUrl)
            .realm(arguments.realm)
            .grantType(OAuth2Constants.PASSWORD)
            .clientId("admin-cli")
            .clientSecret(arguments.clientSecret)
            .username("admin")
            .password("admin")
            .build()

        val realmResource = keycloak.realm(arguments.realm)
        val usersResource = realmResource.users()

        parseFirebaseHashParameters(arguments.hashParamsFile)?.let {
            createHashParameters(keycloak, arguments.realm, arguments.serverUrl, it.hash_config)
        }

        users.users.forEach {
            createOneUser(it, usersResource)?.let { user ->
                addUserRoles(user, realmResource, arguments.clientId, arguments.roles ?: emptyList())
            }
        }
        log.info("Users added")
    } else {
        log.warning("No users found in file")
    }
}



fun createOneUser(user: FirebaseUser, usersResource: UsersResource): UserResource? {
    log.info("Creating user ${user.email}...")
    val keycloakUser = user.convert()
    try {
        val response: Response = usersResource.create(keycloakUser)
        val userId = CreatedResponseUtil.getCreatedId(response)
        log.info("Created user ID $userId")
        return usersResource.get(userId)
    } catch (e: Exception) {
        log.warning("Error creating user: ${e.message}")
        return null
    }
}

fun addUserRoles(userResource: UserResource, realmResource: RealmResource, clientId: String?, roles: List<String>) {

    log.info("Adding roles for user...")

    // Add realm-level offline_access role to allow refresh token flow
    val role = realmResource.roles()["offline_access"].toRepresentation()
    userResource.roles().realmLevel()
        .add(listOf(role))

    clientId?.let { id ->
        val appClient: ClientRepresentation = realmResource.clients()
            .findByClientId(id)[0]

        log.info("Found appClient: ${appClient.id}")

        roles.forEach {
            val userClientRole = realmResource.clients()[appClient.id]
                .roles()[it].toRepresentation()

            log.info("Found userClientRole: ${userClientRole.id}")

            // Assign client level role to user
            userResource.roles()
                .clientLevel(appClient.id).add(listOf(userClientRole))
        }
    }
}

fun FirebaseUser.convert() : UserRepresentation {
    val user = UserRepresentation()
    user.isEnabled = true
    user.username = email
    user.email = email
    val name = displayName?.split(" ")
    user.firstName = name?.let { name[0] }
    if (name?.size ?: 0 > 1) {
        user.lastName = name!!.subList(1, name.size).joinToString(" ")
    }

    phoneNumber?.let {
        user.attributes = hashMapOf("PHONE_VERIFIED" to listOf("true"))
        user.attributes = hashMapOf("PHONE_NUMBER" to listOf(it))
    }

    val credential = CredentialRepresentation()
    credential.isTemporary = false
    credential.type = CredentialRepresentation.SECRET
    credential.credentialData = ScryptPasswordCredentialData("1").jsonData
    credential.secretData = "{\"value\":\"$passwordHash\",\"salt\":\"$salt\"}"

    user.credentials = listOf(credential)

    return user
}