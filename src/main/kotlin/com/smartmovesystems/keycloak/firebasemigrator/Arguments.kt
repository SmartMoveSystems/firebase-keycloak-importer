package com.smartmovesystems.keycloak.firebasemigrator

import com.smartmovesystems.keycloak.firebasemigrator.Arguments.Companion.REQUIRED_ARGS
import java.lang.Exception

data class Arguments(
    val fileName: String,
    val hashParamsFile: String,
    val realm: String,
    val adminUser: String,
    val adminPassword: String,
    val serverUrl: String,
    val clientId: String?,
    val roles: List<String>?,
    val clientSecret: String?,
    val default: Boolean,
    val debug: Boolean
) {

    companion object {
        val REQUIRED_ARGS = arrayOf(
            "--usersFile",
            "--hashParamsFile",
            "--realm",
            "--adminUser",
            "--adminPassword",
            "--serverUrl"
        )
        val OPTIONAL_ARGS = arrayOf( "--clientId", "--roles", "--clientSecret", "--default", "--debug")
    }
}

@Throws(IllegalArgumentException::class)
fun fromStringArray(args: Array<String>): Arguments {
    if (args.size < REQUIRED_ARGS.size * 2) {
        throw IllegalArgumentException("Not enough arguments (passed ${args.size/2}, required ${REQUIRED_ARGS.size}")
    }
    val argMap = hashMapOf<String, String>()
    args.forEachIndexed { index, arg ->
        if (index % 2 == 0) {
            argMap[arg] = args[index + 1]
        }
    }

    REQUIRED_ARGS.forEach {
        if (!argMap.containsKey(it)) {
            throw IllegalArgumentException("Missing required argument: $it")
        }
    }

    try {
        return Arguments(
            argMap[REQUIRED_ARGS[0]]!!,
            argMap[REQUIRED_ARGS[1]]!!,
            argMap[REQUIRED_ARGS[2]]!!,
            argMap[REQUIRED_ARGS[3]]!!,
            argMap[REQUIRED_ARGS[4]]!!,
            argMap[REQUIRED_ARGS[5]]!!,
            argMap[Arguments.OPTIONAL_ARGS[0]],
            argMap[Arguments.OPTIONAL_ARGS[1]]?.split(","),
            argMap[Arguments.OPTIONAL_ARGS[2]],
            argMap[Arguments.OPTIONAL_ARGS[3]]?.toBoolean() ?: false,
            argMap[Arguments.OPTIONAL_ARGS[4]]?.toBoolean() ?: false
        )
    } catch (e: Exception) {
        throw IllegalArgumentException("Badly formatted argument", e)
    }
}