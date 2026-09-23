plugins {
    id("idea")
    id("net.neoforged.moddev") version "2.0.141"
    id("java-library")
}

val NEOFORGE_VERSION: String by rootProject.extra
val MOD_VERSION: String by rootProject.extra

val FABRIC_LOADER_VERSION: String by rootProject.extra
val VULKANMOD_VERSION: String by rootProject.extra
val GREENLIGHT_VERSION: String by rootProject.extra
val BOBBY_VERSION: String by rootProject.extra
val ARCHIVE_NAME: String by rootProject.extra

// Fabric Loom resolves the Fabric VulkanMod artifact into its compile classpath.
// Reuse that resolved jar for NeoForge compilation because VulkanMod 26.2 is not
// published as a normal NeoForge Maven dependency.
val fabricVulkanModClasspath = project(":fabric").configurations.named("compileClasspath").map { classpath ->
    classpath.filter { file -> file.name == "vulkanmod-$VULKANMOD_VERSION.jar" }
}

base {
    archivesName = "$ARCHIVE_NAME-neoforge"
}

repositories {
    maven("https://maven.fabricmc.net/")
    maven("https://maven.su5ed.dev/releases")
    maven("https://maven.neoforged.net/releases/")
    maven("https://maven.caffeinemc.net/releases")
    maven("https://maven.caffeinemc.net/snapshots")

    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = uri("https://api.modrinth.com/maven")
                metadataSources {
                    mavenPom()
                    artifact()
                }
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }
}

tasks.jar {
    from(rootDir.resolve("LICENSE.txt"))

    filesMatching("neoforge.mods.toml") {
        expand(mapOf("version" to MOD_VERSION))
    }
}

neoForge {
    // Specify the version of NeoForge to use.
    version = NEOFORGE_VERSION

    runs {
        create("client") {
            client()
            ideName = "NeoForge/Client"
        }
    }

    mods {
        create(project.name) {
            sourceSet(sourceSets.main.get())
        }
    }
}

dependencies {
    compileOnly(project(":common"))

    // Common sources use Fabric/VulkanMod APIs. These are compile-time only for
    // NeoForge: the Fabric VulkanMod jar must not be bundled or treated as a
    // NeoForge runtime dependency.
    compileOnly("net.fabricmc:fabric-loader:$FABRIC_LOADER_VERSION")
    compileOnly(files(fabricVulkanModClasspath))
    compileOnly("io.github.llamalad7:mixinextras-common:0.5.4")
    annotationProcessor("io.github.llamalad7:mixinextras-common:0.5.4")
    compileOnly("net.fabricmc:sponge-mixin:0.17.3+mixin.0.8.7")
    compileOnly("com.github.bawnorton.mixinsquared:mixinsquared-fabric:0.3.7-beta.2")
    annotationProcessor("com.github.bawnorton.mixinsquared:mixinsquared-fabric:0.3.7-beta.2")
    compileOnly("org.jspecify:jspecify:1.0.0")
    compileOnly("org.apache.commons:commons-lang3:3.18.0")
    compileOnly("maven.modrinth:bobby:$BOBBY_VERSION")

    implementation("me.flashyreese.mods:greenlight-api:$GREENLIGHT_VERSION")
    jarJar("me.flashyreese.mods:greenlight-api:$GREENLIGHT_VERSION")
}

tasks.named<JavaCompile>("compileJava") {
    source(project(":common").sourceSets.main.get().allSource)
}

tasks.named<Javadoc>("javadoc") {
    source(project(":common").sourceSets.main.get().allJava)
}

tasks.named<ProcessResources>("processResources") {
    from(project(":common").sourceSets.main.get().resources)
}

java.toolchain.languageVersion = JavaLanguageVersion.of(25)

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
