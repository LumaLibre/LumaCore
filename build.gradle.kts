import org.apache.commons.io.output.ByteArrayOutputStream
import java.nio.charset.Charset

plugins {
    id("java")
    id("maven-publish")
    id("io.freefair.lombok") version "9.2.0"
    id("com.gradleup.shadow") version("8.3.5")
    id("de.eldoria.plugin-yml.bukkit") version("0.7.1")
    kotlin("jvm")
}

group = "dev.lumas.core"
version = commitHashShort()

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
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
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }
    jar {
        archiveVersion = null
        enabled = false
    }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
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

            artifact(tasks.named("sourcesJar").get())
        }
    }
}


bukkit {
    name = "LumaCore"
    main = "dev.lumas.core.LumaCore"
    version = project.version.toString()
    apiVersion = "1.21"
    author = "Jsinco"
    softDepend = listOf("PlaceholderAPI")
    foliaSupported = true
}


fun commitHashShort(): String = ByteArrayOutputStream().use { stream ->
    var branch = "none"
    project.exec {
        commandLine = listOf("git", "log", "-1", "--format=%h")
        standardOutput = stream
    }
    if (stream.size() > 0) branch = stream.toString(Charset.defaultCharset().name()).trim()
    return branch
}