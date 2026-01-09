import org.gradle.jvm.tasks.Jar

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.2.21"
}

group = "com.smartmovesystems.keycloak.firebasemigrator"
version = "1.1.0"

repositories {
    mavenCentral()
    jcenter()
}

val mainClassName = "com.smartmovesystems.keycloak.firebasemigrator.FirebaseKeycloakUserCreator"

dependencies {

    val keycloakVersion = "26.4.6" //"11.0.2"
    val keycloakClientVersion = "26.0.7" //"11.0.2"
    val jUnitVersion = "5.10.0"
    val moshiVersion = "1.15.2"
    val jsonVersion = "20250517"

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.keycloak:keycloak-admin-client:$keycloakClientVersion")
    implementation("com.squareup.moshi:moshi:$moshiVersion")
    implementation("com.squareup.moshi:moshi-kotlin:$moshiVersion")
    implementation("org.apache.httpcomponents:httpcore-nio:4.4.16")
    implementation("org.apache.httpcomponents:httpasyncclient:4.1.5")
    implementation("org.json:json:$jsonVersion")
//    compileOnly("org.keycloak:keycloak-core:$keycloakVersion")

    // JUnit
    testImplementation("org.junit.jupiter:junit-jupiter-api:${jUnitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${jUnitVersion}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) // Set to Java 17
    }
}

val fatJar = tasks.register<Jar>("fatJar") {
    manifest {
        attributes["Main-Class"] = "${mainClassName}" //""com.smartmovesystems.keycloak.firebasemigrator.FirebaseKeycloakUserCreator" // fully qualified class name of default main class
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks["jar"] as CopySpec)
}

tasks {
    "build" {
        dependsOn(fatJar)
    }
}


//##################################### run - JavaExec #######################################
tasks.register<JavaExec>("runImportUsers") {
    group = "smartmove"
    classpath = sourceSets["main"].runtimeClasspath

    mainClass.set("${mainClassName}")

    // arguments to pass to the application
    val USERS_FILE="example_users.json"
    val HASH_PARAMS_FILE="example_hash_config.json"
    val ADMIN_USER="admin@my-keycloak.nowhere"
    val ADMIN_PASSWORD="test1234"
    val REALM="myrealm"
    val SERVER_URL="http://localhost:8080/auth"
    val DEFAULT=true

    args("--usersFile", "${USERS_FILE}",
        "--hashParamsFile", "${HASH_PARAMS_FILE}",
        "--adminUser", "${ADMIN_USER}",
        "--adminPassword", "${ADMIN_PASSWORD}",
        "--realm", "${REALM}",
        "--serverUrl", "${SERVER_URL}",
        "--default", "${DEFAULT}")

    debugOptions {
        enabled = true
        port = 5005
        server = true
        suspend = true
    }
}