pluginManagement {
    val shadowVersion: String by settings
    val runPaperVersion: String by settings
    plugins {
        id("com.gradleup.shadow") version shadowVersion
        id("xyz.jpenilla.run-paper") version runPaperVersion
    }
}

rootProject.name = "Unhacked"