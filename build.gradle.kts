import org.gradle.jvm.tasks.Jar

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.60"
}

group = "com.smartmovesystems.keycloak.firebasemigrator"
version = "0.0.4"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {

    val keycloakVersion = "11.0.2"

    testImplementation("junit:junit:4.13")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.keycloak:keycloak-admin-client:$keycloakVersion")
    implementation("com.squareup.moshi:moshi:1.9.3")
    implementation("com.squareup.moshi:moshi-kotlin:1.9.3")
    implementation("org.json:json:20200518")
    compileOnly("org.keycloak:keycloak-core:$keycloakVersion")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

val fatJar = task("fatJar", type = Jar::class) {
    manifest {
        attributes["Main-Class"] = "com.smartmovesystems.keycloak.firebasemigrator.FirebaseKeycloakUserCreator" // fully qualified class name of default main class
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks["jar"] as CopySpec)
}

tasks {
    "build" {
        dependsOn(fatJar)
    }
}