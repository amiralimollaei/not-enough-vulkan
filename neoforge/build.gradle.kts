import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("idea")
    id("java-library")
    id("dev.architectury.loom-no-remap")
    id("architectury-plugin")
    id("com.gradleup.shadow")
}

val MINECRAFT_VERSION = rootProject.extra["MINECRAFT_VERSION"] as String
val NEOFORGE_VERSION = rootProject.extra["NEOFORGE_VERSION"] as String
val VULKANMOD_VERSION = rootProject.extra["VULKANMOD_VERSION"] as String
val GREENLIGHT_VERSION = rootProject.extra["GREENLIGHT_VERSION"] as String
val BOBBY_VERSION = rootProject.extra["BOBBY_VERSION"] as String

base {
    archivesName.set("${rootProject.name}-neoforge")
}

architectury {
    compileOnly()
    platformSetupLoomIde()
    neoForge()
}

repositories {
    maven("https://maven.neoforged.net/releases/")
}

loom {
    neoForge {
        accessTransformer("src/main/resources/META-INF/accesstransformer.cfg")
    }

    mods {
        named("main") {
            sourceSet(sourceSets.main.get())
            sourceSet(project(":common").sourceSets.main.get())
        }
    }

    runs {
        named("client") {
            client()
            displayName.set("NeoForge Client")
            runDirectory.set(layout.projectDirectory.dir("run"))
        }
    }
}

val common = configurations.create("common") {
    isCanBeResolved = true
    isCanBeConsumed = false
}

val shadowBundle = configurations.create("shadowBundle") {
    isCanBeResolved = true
    isCanBeConsumed = false
}

configurations.named("compileClasspath") {
    extendsFrom(common)
}

configurations.named("runtimeClasspath") {
    extendsFrom(common)
}

dependencies {
    minecraft("net.minecraft:minecraft:$MINECRAFT_VERSION")
    // Specify the version of NeoForge to use.
    add("neoForge", "net.neoforged:neoforge:$NEOFORGE_VERSION")

    compileOnly("io.github.llamalad7:mixinextras-common:0.5.4")
    annotationProcessor("io.github.llamalad7:mixinextras-common:0.5.4")
    compileOnly("net.fabricmc:sponge-mixin:0.17.3+mixin.0.8.7")
    compileOnly("com.github.bawnorton.mixinsquared:mixinsquared-fabric:0.3.7-beta.2")
    annotationProcessor("com.github.bawnorton.mixinsquared:mixinsquared-fabric:0.3.7-beta.2")
    compileOnly("org.jspecify:jspecify:1.0.0")
    compileOnly("org.apache.commons:commons-lang3:3.18.0")
    compileOnly("maven.modrinth:bobby:$BOBBY_VERSION")

    implementation("me.flashyreese.mods:greenlight-api:$GREENLIGHT_VERSION")
    include("me.flashyreese.mods:greenlight-api:$GREENLIGHT_VERSION")
    add("common", project(":common")) {
        isTransitive = false
    }
    add("shadowBundle", project(path = ":common", configuration = "runtimeElements")) {
        isTransitive = false
    }
}

tasks.named("compileTestJava").configure {
    enabled = false
}

tasks.test {
    failOnNoDiscoveredTests = false
}

tasks {
    processResources {
        inputs.property("version", project.version)
        inputs.property("minecraft_version", MINECRAFT_VERSION)
        inputs.property("vulkanmod_version", VULKANMOD_VERSION)

        filesMatching("META-INF/neoforge.mods.toml") {
            expand(mapOf(
                "version" to project.version,
                "minecraft_version" to MINECRAFT_VERSION,
                "vulkanmod_version" to VULKANMOD_VERSION
            ))
        }
    }

    jar {
        archiveClassifier.set("dev")
        from(rootDir.resolve("LICENSE.txt"))
    }
}

tasks.named<ShadowJar>("shadowJar") {
    configurations = listOf(shadowBundle)
    archiveClassifier.set("")
    from(rootDir.resolve("LICENSE.txt"))
}

loom.nestJars(tasks.named<ShadowJar>("shadowJar"), configurations.named("include"))

configurations.named("apiElements") {
    outgoing.artifacts.clear()
}

configurations.named("runtimeElements") {
    outgoing.artifacts.clear()
}

artifacts {
    add("apiElements", tasks.named("shadowJar"))
    add("runtimeElements", tasks.named("shadowJar"))
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
