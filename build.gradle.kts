import org.apache.commons.io.output.ByteArrayOutputStream
import org.apache.tools.ant.filters.ReplaceTokens
import java.nio.charset.Charset

plugins {
    id("java")
    id("maven-publish")
    kotlin("jvm")
    id("com.gradleup.shadow") version("8.3.5")
}

group = "dev.lumas.lumacore"
version = getGitCommitHashShort()

val jdkVersion: Int = 21
val charset: String = "UTF-8"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

    compileOnly("me.clip:placeholderapi:2.11.6")
    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.0")

}

tasks {
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        dependencies {
            include(dependency("org.jetbrains.kotlin:kotlin-stdlib"))
        }
        archiveVersion.set("")
        archiveClassifier.set("")
    }
    processResources {
        outputs.upToDateWhen { false }
        filter<ReplaceTokens>(mapOf(
            "tokens" to mapOf("version" to project.version.toString().replace("/", "")),
            "beginToken" to "\${",
            "endToken" to "}"
        )).filteringCharset = charset
    }
    withType<JavaCompile>().configureEach {
        options.encoding = charset
    }
    jar {
        archiveVersion = null
        enabled = false
    }
}

kotlin {
    jvmToolchain(jdkVersion)
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(jdkVersion)
    withSourcesJar()
}

publishing {
    repositories {
        maven {
            name = "jsinco-repo"
            url = uri("https://repo.jsinco.dev/releases")
            credentials(PasswordCredentials::class) {
                // get from environment
                username = System.getenv("REPO_USERNAME")
                password = System.getenv("REPO_PASSWORD")
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            artifact(tasks.shadowJar.get().archiveFile) {
                builtBy(tasks.shadowJar)
            }
        }
    }
}

fun getGitCommitHashShort(): String = ByteArrayOutputStream().use { stream ->
    var branch = "none"
    project.exec {
        commandLine = listOf("git", "log", "-1", "--format=%h")
        standardOutput = stream
    }
    if (stream.size() > 0) branch = stream.toString(Charset.defaultCharset().name()).trim()
    return branch
}