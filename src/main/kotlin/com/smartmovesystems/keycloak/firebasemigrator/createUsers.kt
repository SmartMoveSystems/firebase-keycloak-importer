package com.smartmovesystems.keycloak.firebasemigrator

import com.smartmovesystems.keycloak.firebasemigrator.model.*
import org.json.JSONObject
import org.keycloak.admin.client.*
import org.keycloak.admin.client.resource.RealmResource
import org.keycloak.admin.client.resource.UserResource
import org.keycloak.admin.client.resource.UsersResource
import org.keycloak.representations.idm.ClientRepresentation
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation
import java.lang.Exception
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger
import javax.ws.rs.core.Response

private val log = Logger.getLogger("createUsers")

fun createUsers(arguments: Arguments) {

    setLogLevel(arguments.debug)

    log.info("Parsing users file...")
    val users = parseFile<UserList>(arguments.fileName)

    if (users != null) {
        log.info("Found ${users.users.size} users in file.")
        log.info("About to connect to Keycloak instance at ${arguments.serverUrl} as user ${arguments.adminUser}")
        val keycloak = getKcInstance(arguments)

        val hashParamsId = parseFile<FirebaseHashConfig>(arguments.hashParamsFile)?.let {
            log.info("Adding scrypt hash parameters...")
            createHashParameters(keycloak, arguments.realm, arguments.serverUrl, it.hash_config, arguments.default)
        }

        val size = users.users.size
        var created = 0

        users.users.forEach {
            val realmResource = keycloak.realm(arguments.realm)
            val usersResource = realmResource.users()
            createOneUser(it, usersResource, hashParamsId)?.let { user ->
                addUserRoles(user, realmResource, arguments.clientId, arguments.roles ?: emptyList())
                created++
            }
        }
        log.info("$created of $size users added")
    } else {
        log.warning("No users found in file")
    }
}

private fun setLogLevel(debug: Boolean) {
    val rootLogger: Logger = LogManager.getLogManager().getLogger("")
    val level = if (debug) Level.FINE else Level.INFO
    rootLogger.level = level
    for (h in rootLogger.handlers) {
        h.level = level
    }
}

private fun createOneUser(user: FirebaseUser, usersResource: UsersResource, hashParamsId: String?): UserResource? {
    log.fine("Creating user ${user.email}...")
    val keycloakUser = user.convert()
    return try {
        val response: Response = usersResource.create(keycloakUser)
        val userId = CreatedResponseUtil.getCreatedId(response)
        val resource = usersResource.get(userId)
        addUserCredential(resource, user, hashParamsId)
        log.fine("Created user ID $userId")
        usersResource.get(userId)
    } catch (e: Exception) {
        log.warning("Error creating user: $e")
        if (e.message?.contains("Create method returned status Conflict") == true) {
            duplicateEmail(user, usersResource)
        } else {
            null
        }
    }
}

private fun duplicateEmail(user: FirebaseUser, usersResource: UsersResource): UserResource? {
    log.info("Trying to find existing user with email ${user.email}")
    // The user search has a tendency to hang so ensure client is alive first
    val users = usersResource.search(user.email, true)
    return if (users.size == 1) {
        log.info("Found the user with id ${users[0].id}")
        val phoneNumber = users[0].attributes?.get("phone_number")?.firstOrNull()
        val existing = usersResource.get(users[0].id)
        if (phoneNumber != user.phoneNumber) {
            log.info("User's phone number doesn't match; setting to invalid")
            val representation = existing.toRepresentation()
            representation.singleAttribute("phone_verified", "false")
            existing.update(representation)
        } else {
            log.info("Phone numbers match; nothing to do here")
        }
        existing
    } else {
        log.severe("There were ${users.size} users with this email address. Cannot update.")
        null
    }
}

private fun addUserCredential(userResource: UserResource, user: FirebaseUser, hashParamsId: String?) {
    val credential = CredentialRepresentation()
    val representation = userResource.toRepresentation()
    credential.isTemporary = false
    credential.credentialData = ScryptPasswordCredentialData().jsonData
    credential.secretData = "{\"value\":\"${user.passwordHash}\$$hashParamsId\",\"salt\":\"${user.salt}\"}"
    credential.type = CredentialRepresentation.PASSWORD
    representation.credentials = listOf(credential)
    userResource.update(representation)
}

private fun addUserRoles(userResource: UserResource, realmResource: RealmResource, clientId: String?, roles: List<String>) {

    log.fine("Adding roles for user...")

    // Add realm-level offline_access role to allow refresh token flow
    val role = realmResource.roles()["offline_access"].toRepresentation()
    userResource.roles().realmLevel()
        .add(listOf(role))

    clientId?.let { id ->
        val appClient: ClientRepresentation = realmResource.clients()
            .findByClientId(id)[0]

        log.fine("Found appClient: ${appClient.id}")

        roles.forEach {
            val userClientRole = realmResource.clients()[appClient.id]
                .roles()[it].toRepresentation()

            log.fine("Found userClientRole: ${userClientRole.id}")

            // Assign client level role to user
            userResource.roles()
                .clientLevel(appClient.id).add(listOf(userClientRole))
        }
    }
}

private fun FirebaseUser.convert() : UserRepresentation {
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