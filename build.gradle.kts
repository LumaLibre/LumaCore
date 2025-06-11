import org.apache.commons.io.output.ByteArrayOutputStream
import org.apache.tools.ant.filters.ReplaceTokens
import java.nio.charset.Charset

plugins {
    id("java")
    id("maven-publish")
    kotlin("jvm")
    id("com.gradleup.shadow") version("8.3.5")
    id("io.papermc.paperweight.userdev") version("1.7.5")
}

group = "dev.jsinco.luma.lumacore"
version = getGitCommitHashShort()

val jdkVersion: Int = 21
val charset: String = "UTF-8"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    paperweight.paperDevBundle("1.21.3-R0.1-SNAPSHOT")

    compileOnly("me.clip:placeholderapi:2.11.6")
    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.0")
    implementation("fr.skytasul:glowingentities:1.4.4")

}

tasks {
    test {
        useJUnitPlatform()
    }
    build {
        dependsOn(shadowJar)
    }
    assemble {
        dependsOn(reobfJar)
    }
    reobfJar {
        outputJar.set(
            layout.buildDirectory.file(
                "${projectDir}${File.separator}build${File.separator}libs${File.separator}${project.name}.jar"
            )
        )


    }
    shadowJar {
        relocate("fr.skytasul.glowingentities", "dev.jsinco.luma.lumacore.glowingentities")
        dependencies {
            include(dependency("org.jetbrains.kotlin:kotlin-stdlib"))
        }
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
                username = System.getenv("repo_username")
                password = System.getenv("repo_secret")
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