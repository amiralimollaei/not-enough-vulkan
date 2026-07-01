plugins {
    id("java")
    id("net.fabricmc.fabric-loom") version ("1.17.11") apply (false)
}

val MINECRAFT_VERSION by extra { "26.2" }
val NEOFORGE_VERSION by extra { "26.2.0.1-beta" }
val FABRIC_LOADER_VERSION by extra { "0.19.3" }
val FABRIC_API_VERSION by extra { "0.152.1+26.2" }

// https://semver.org/
val MAVEN_GROUP by extra { "io.github.amiralimollaei.mods" }
val ARCHIVE_NAME by extra { "not-enough-vulkan" }
val MOD_VERSION by extra { "1.6.1" }
val VULKANMOD_VERSION by extra { "0.6.9+26.1.2" }
val BOBBY_VERSION by extra { "5.2.14+mc26.2" }

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")
    group = MAVEN_GROUP
    version = createVersionString()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

subprojects {
    val modVersion = createVersionString()

    apply(plugin = "maven-publish")

    repositories {
        maven("https://maven.parchmentmc.org/")
        maven("https://api.modrinth.com/maven")
        maven("https://libraries.minecraft.net")
        maven("https://maven.bawnorton.com/releases")
    }

    base {
        archivesName = "$ARCHIVE_NAME-${project.name}"
    }

    java.toolchain.languageVersion = JavaLanguageVersion.of(25)

    tasks.processResources {
        filesMatching("META-INF/neoforge.mods.toml") {
            expand(mapOf("version" to modVersion))
        }
    }

    version = modVersion
    group = MAVEN_GROUP

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(25)
    }

    tasks.withType<GenerateModuleMetadata>().configureEach {
        enabled = false
    }
}

fun createVersionString(): String {
    val builder = StringBuilder()

    val isReleaseBuild = project.hasProperty("build.release")
    val buildId = System.getenv("GITHUB_RUN_NUMBER")

    if (isReleaseBuild) {
        builder.append(MOD_VERSION)
    } else {
        builder.append(MOD_VERSION.split('-')[0])
        builder.append("-snapshot")
    }

    builder.append("+mc").append(MINECRAFT_VERSION)

    if (!isReleaseBuild) {
        if (buildId != null) {
            builder.append("-build.$buildId")
        } else {
            builder.append("-local")
        }
    }

    return builder.toString()
}
