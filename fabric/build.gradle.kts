plugins {
    id("java")
    id("idea")
    id("net.fabricmc.fabric-loom") version ("1.17.11")
}

val MINECRAFT_VERSION: String by rootProject.extra
val PARCHMENT_VERSION: String? by rootProject.extra
val FABRIC_LOADER_VERSION: String by rootProject.extra
val FABRIC_API_VERSION: String by rootProject.extra
val MOD_VERSION: String by rootProject.extra

val VULKANMOD_VERSION: String by rootProject.extra
val BOBBY_VERSION: String by rootProject.extra
val ARCHIVE_NAME: String by rootProject.extra

base {
    archivesName.set("$ARCHIVE_NAME-fabric")
}

dependencies {
    minecraft("com.mojang:minecraft:${MINECRAFT_VERSION}")
    compileOnly("net.fabricmc:fabric-loader:$FABRIC_LOADER_VERSION")
    runtimeOnly("net.fabricmc:fabric-loader:$FABRIC_LOADER_VERSION")
    testCompileOnly("net.fabricmc:fabric-loader:$FABRIC_LOADER_VERSION")

    include("com.github.bawnorton.mixinsquared:mixinsquared-fabric:0.3.7-beta.2")
    implementation("com.github.bawnorton.mixinsquared:mixinsquared-fabric:0.3.7-beta.2")
    annotationProcessor("com.github.bawnorton.mixinsquared:mixinsquared-fabric:0.3.7-beta.2")

    fun addEmbeddedFabricModule(name: String) {
        val module = fabricApi.module(name, FABRIC_API_VERSION)
        implementation(module)
    }

    // Fabric API modules
    addEmbeddedFabricModule("fabric-api-base")
    addEmbeddedFabricModule("fabric-block-getter-api-v2")
    addEmbeddedFabricModule("fabric-rendering-v1")

    compileOnly(project(":common"))
    //implementation("maven.modrinth:vulkanmod:$VULKANMOD_VERSION")
    implementation(files("run/mods/VulkanMod-0.6.9-dev.0+26.2.jar"))
    implementation("maven.modrinth:bobby:$BOBBY_VERSION")
}

tasks.test {
    failOnNoDiscoveredTests = false
}

loom {
    accessWidenerPath.set(project(":common").file("src/main/resources/sodium-extra.accesswidener"))

    runs {
        named("client") {
            client()
            displayName.set("Fabric Client")
            generateRunConfig.set(true)
            runDirectory.set(layout.projectDirectory.dir("run"))
        }
        named("server") {
            server()
            displayName.set("Fabric Server")
            generateRunConfig.set(true)
            runDirectory.set(layout.projectDirectory.dir("run"))
        }
    }
}

val modVersion = project.version.toString()

tasks {
    withType<JavaCompile> {
        source(project(":common").sourceSets.main.get().allSource)
    }

    javadoc { source(project(":common").sourceSets.main.get().allJava) }

    processResources {
        from(project(":common").sourceSets.main.get().resources)

        inputs.property("version", modVersion)

        filesMatching("fabric.mod.json") {
            expand(mapOf("version" to modVersion))
        }
    }

    jar {
        from(rootDir.resolve("LICENSE.txt"))
    }
}

tasks.named("validateAccessWidener").configure {
    dependsOn(":common:genSourcesWithVineflower")
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
}
*/