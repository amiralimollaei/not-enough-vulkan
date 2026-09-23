plugins {
    id("java")
    id("idea")
    id("net.fabricmc.fabric-loom")
}

val MINECRAFT_VERSION: String by rootProject.extra
val FABRIC_LOADER_VERSION: String by rootProject.extra
val FABRIC_API_VERSION: String by rootProject.extra

val VULKANMOD_VERSION: String by rootProject.extra
val BOBBY_VERSION: String by rootProject.extra
val GREENLIGHT_VERSION: String by rootProject.extra

dependencies {
    minecraft("com.mojang:minecraft:$MINECRAFT_VERSION")
    compileOnly("net.fabricmc:fabric-loader:$FABRIC_LOADER_VERSION")
    runtimeOnly("net.fabricmc:fabric-loader:$FABRIC_LOADER_VERSION")

    fun addDependentFabricModule(name: String) {
        val module = fabricApi.module(name, FABRIC_API_VERSION)
        implementation(module)
    }

    // Fabric API modules
    addDependentFabricModule("fabric-api-base")
    addDependentFabricModule("fabric-block-getter-api-v2")
    addDependentFabricModule("fabric-rendering-v1")
    compileOnly(project(":common"))
    implementation("com.github.bawnorton.mixinsquared:mixinsquared-fabric:0.3.7-beta.2")
    include("com.github.bawnorton.mixinsquared:mixinsquared-fabric:0.3.7-beta.2")
    annotationProcessor("com.github.bawnorton.mixinsquared:mixinsquared-fabric:0.3.7-beta.2")
    compileOnly("org.jspecify:jspecify:1.0.0")
    compileOnly("org.apache.commons:commons-lang3:3.18.0")
    compileOnly("maven.modrinth:bobby:$BOBBY_VERSION")
    implementation("maven.modrinth:vulkanmod:$VULKANMOD_VERSION")
    implementation("me.flashyreese.mods:greenlight-api:$GREENLIGHT_VERSION")
    include("me.flashyreese.mods:greenlight-api:$GREENLIGHT_VERSION")
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
    named<JavaCompile>("compileJava") {
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
