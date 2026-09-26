plugins {
    id("java")
    id("idea")
    id("dev.architectury.loom-no-remap")
    id("architectury-plugin")
}

val MINECRAFT_VERSION = rootProject.extra["MINECRAFT_VERSION"] as String
val FABRIC_LOADER_VERSION = rootProject.extra["FABRIC_LOADER_VERSION"] as String
val FABRIC_API_VERSION = rootProject.extra["FABRIC_API_VERSION"] as String

val VULKANMOD_VERSION = rootProject.extra["VULKANMOD_VERSION"]  as String
val BOBBY_VERSION = rootProject.extra["BOBBY_VERSION"] as String
val GREENLIGHT_VERSION = rootProject.extra["GREENLIGHT_VERSION"] as String

architectury {
    compileOnly()
    common("fabric", "neoforge")
    injectInjectables = false
}

// This trick hides common tasks in the IDEA list.
tasks.configureEach {
    group = null
}


loom {
    accessWidenerPath = file("src/main/resources/sodium-extra.accesswidener")
}

dependencies {
    minecraft("net.minecraft:minecraft:$MINECRAFT_VERSION")

    compileOnly("io.github.llamalad7:mixinextras-common:0.5.4")
    annotationProcessor("io.github.llamalad7:mixinextras-common:0.5.4")
    compileOnly("net.fabricmc:sponge-mixin:0.17.3+mixin.0.8.7")
    compileOnly("net.fabricmc:fabric-loader:$FABRIC_LOADER_VERSION")

    fun addDependentFabricModule(name: String) {
        val module = fabricApi.module(name, FABRIC_API_VERSION)
        compileOnly(module)
    }

    addDependentFabricModule("fabric-api-base")
    addDependentFabricModule("fabric-block-getter-api-v2")
    addDependentFabricModule("fabric-rendering-v1")

    compileOnly("maven.modrinth:vulkanmod:$VULKANMOD_VERSION")
    compileOnly("me.flashyreese.mods:greenlight-api:$GREENLIGHT_VERSION")

    compileOnly("com.github.bawnorton.mixinsquared:mixinsquared-fabric:0.3.7-beta.2")
    annotationProcessor("com.github.bawnorton.mixinsquared:mixinsquared-fabric:0.3.7-beta.2")
    compileOnly("org.jspecify:jspecify:1.0.0")
    compileOnly("org.apache.commons:commons-lang3:3.18.0")
    compileOnly("net.java.dev.jna:jna-platform:5.17.0")
    compileOnly("maven.modrinth:bobby:$BOBBY_VERSION")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = base.archivesName.get()
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "FlashyReeseReleases"
            url = uri("https://maven.flashyreese.me/releases")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
        maven {
            name = "FlashyReeseSnapshots"
            url = uri("https://maven.flashyreese.me/snapshots")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }
}
