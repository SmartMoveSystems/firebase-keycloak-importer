package com.smartmovesystems.keycloak.firebasemigrator

import com.smartmovesystems.keycloak.firebasemigrator.model.*
import org.json.JSONObject
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
    val users = parseFile<UserList>(arguments.fileName)

    if (users != null) {

        log.info("Found ${users.users.size} users in file.")
        log.info("About to connect to Keycloak instance at ${arguments.serverUrl} as user ${arguments.adminUser}")
        val keycloak = KeycloakBuilder.builder()
            .serverUrl(arguments.serverUrl)
            .realm(arguments.realm)
            .grantType(OAuth2Constants.PASSWORD)
            .clientId("admin-cli")
            .clientSecret(arguments.clientSecret)
            .username(arguments.adminUser)
            .password(arguments.adminPassword)
            .build()

        val realmResource = keycloak.realm(arguments.realm)
        val usersResource = realmResource.users()

        val hashParamsId = parseFile<FirebaseHashConfig>(arguments.hashParamsFile)?.let {
            log.info("Adding scrypt hash parameters...")
            createHashParameters(keycloak, arguments.realm, arguments.serverUrl, it.hash_config, arguments.default)
        }

        users.users.forEach {
            createOneUser(it, usersResource, hashParamsId)?.let { user ->
                addUserRoles(user, realmResource, arguments.clientId, arguments.roles ?: emptyList())
            }
        }
        log.info("Users added")
    } else {
        log.warning("No users found in file")
    }
}

fun createOneUser(user: FirebaseUser, usersResource: UsersResource, hashParamsId: String?): UserResource? {
    log.info("Creating user ${user.email}...")
    val keycloakUser = user.convert()
    return try {
        val response: Response = usersResource.create(keycloakUser)
        val userId = CreatedResponseUtil.getCreatedId(response)
        val resource = usersResource.get(userId)
        addUserCredential(resource, user, hashParamsId)
        log.info("Created user ID $userId")
        usersResource.get(userId)
    } catch (e: Exception) {
        log.warning("Error creating user: ${e.message}")
        null
    }
}

fun addUserCredential(userResource: UserResource, user: FirebaseUser, hashParamsId: String?) {
    val credential = CredentialRepresentation()
    val representation = userResource.toRepresentation()
    credential.isTemporary = false
    credential.credentialData = ScryptPasswordCredentialData().jsonData
    credential.secretData = "{\"value\":\"${user.passwordHash}\$$hashParamsId\",\"salt\":\"${user.salt}\"}"
    credential.type = CredentialRepresentation.PASSWORD
    representation.credentials = listOf(credential)
    userResource.update(representation)
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
        user.singleAttribute("phone_verified","true")
        user.singleAttribute("phone_number", it)
    }

    customAttributes?.let { attrString ->
        val json = JSONObject(attrString)
        json.keys().forEachRemaining {
            user.singleAttribute(it, json.get(it).toString())
        }
    }

    return user
}