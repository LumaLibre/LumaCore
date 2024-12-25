import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    id("java")
    kotlin("jvm")
    id("com.gradleup.shadow") version("8.3.5")
    id("io.papermc.paperweight.userdev") version("1.7.5")
}

group = "dev.jsinco.luma"
version = "paper-1.21.3"

val jdkVersion: Int = 21
val charset: String = "UTF-8"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    paperweight.paperDevBundle("1.21.3-R0.1-SNAPSHOT")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
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
        version = ""
        enabled = false
    }
}

kotlin {
    jvmToolchain(jdkVersion)
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(jdkVersion)
}