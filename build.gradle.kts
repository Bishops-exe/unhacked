plugins {
    java
    id("com.gradleup.shadow")
    id("xyz.jpenilla.run-paper")
}


group = property("group") as String
version = property("version") as String

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(property("javaVersion").toString().toInt())
    }
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://jitpack.io")
    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://repo.opencollab.dev/maven-releases/")
    maven("https://repo.opencollab.dev/main/")
}

dependencies {
    compileOnly("org.projectlombok:lombok:${property("lombokVersion")}")
    annotationProcessor("org.projectlombok:lombok:${property("lombokVersion")}")

    compileOnly("io.papermc.paper:paper-api:${property("paperApiVersion")}")
    compileOnly("com.github.retrooper:packetevents-spigot:${property("packetEventsVersion")}")
    compileOnly("com.github.BitByLogics:PacketBlocks:${property("packetBlocksVersion")}")
    compileOnly("org.geysermc.geyser:api:2.11.2-SNAPSHOT")
}

tasks {
    processResources {
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(
                "version" to project.version,
                "name" to project.name
            )

        }
    }

    shadowJar {
        archiveBaseName = project.name
        archiveClassifier = ""
        archiveVersion = project.version.toString()
        from(rootProject.file("LICENSE"))
    }

    build {
        dependsOn(shadowJar)
    }

    runServer {
        minecraftVersion(project.property("minecraftVersion") as String)
    }
}