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
    compileOnly("org.keycloak:keycloak-core:$keycloakVersion")

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
        attributes["Main-Class"] = "com.smartmovesystems.keycloak.firebasemigrator.FirebaseKeycloakUserCreator" // fully qualified class name of default main class
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