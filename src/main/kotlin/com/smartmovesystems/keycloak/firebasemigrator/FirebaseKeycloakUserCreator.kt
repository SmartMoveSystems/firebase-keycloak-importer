package com.smartmovesystems.keycloak.firebasemigrator

class FirebaseKeycloakUserCreator {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val arguments = fromStringArray(args)
            createUsers(arguments)
        }
    }
}