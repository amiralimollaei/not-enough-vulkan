import net.fabricmc.loom.task.AbstractRemapJarTask

plugins {
    id("java")
    id("idea")
    id("net.fabricmc.fabric-loom") version "1.15.4"
}

val MINECRAFT_VERSION: String by rootProject.extra
val PARCHMENT_VERSION: String? by rootProject.extra
val FABRIC_LOADER_VERSION: String by rootProject.extra
val FABRIC_API_VERSION: String by rootProject.extra

dependencies {
    minecraft("com.mojang:minecraft:$MINECRAFT_VERSION")
    compileOnly("io.github.llamalad7:mixinextras-common:0.5.0")
    annotationProcessor("io.github.llamalad7:mixinextras-common:0.5.0")
    compileOnly("net.fabricmc:sponge-mixin:0.13.2+mixin.0.8.5")

    fun addDependentFabricModule(name: String) {
        val module = fabricApi.module(name, FABRIC_API_VERSION)
        implementation(module)
    }

    addDependentFabricModule("fabric-api-base")
    addDependentFabricModule("fabric-block-getter-api-v2")
    addDependentFabricModule("fabric-rendering-v1")
}

tasks.withType<AbstractRemapJarTask>().forEach {
    it.targetNamespace = "named"
}

tasks.named("compileJava") {
    mustRunAfter("genSourcesWithVineflower")
}

loom {
    mixin {
        useLegacyMixinAp = false
        //defaultRefmapName = "${rootProject.name}.refmap.json"
    }

    accessWidenerPath = file("src/main/resources/sodium-extra.accesswidener")
}

/*publishing {
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
}*/