import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.gradle.jvm.tasks.Jar
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
//    id("com.github.johnrengelman.shadow") version "8.1.1"
}

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting
        
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation("io.ktor:ktor-client-core:2.3.5")
            implementation("io.ktor:ktor-client-cio:2.3.5")
            implementation("io.ktor:ktor-client-websockets:2.3.5")
            implementation("de.drick.compose:hotpreview:0.1.6")
            implementation("cafe.adriel.voyager:voyager-navigator:1.0.1")
            implementation("cafe.adriel.voyager:voyager-core:1.0.1")
            implementation("io.github.ismai117:kottie:2.0.1")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}


compose.desktop {
    application {
        mainClass = "org.tetra.adas.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ADAS"
            packageVersion = "1.0.0"
            linux {
                iconFile.set(project.file("src/desktopMain/composeResources/drawable/logo.png"))
            }
            windows {
                iconFile.set(project.file("src/desktopMain/composeResources/drawable/logo.png"))
            }
        }

        buildTypes.release.proguard {
            configurationFiles.from(project.file("compose-desktop.pro"))
        }
    }

}



//        tasks.register<Jar>("fatJar") {
//            group = "build"
//            manifest {
//                attributes["Main-Class"] = "your.package.MainKt" // <-- Replace this
//            }
//
//            // Use the correct configuration name for the JVM target
//            val runtimeClasspath = configurations.getByName("jvmRuntimeClasspath")
//
//            from(runtimeClasspath.map { if (it.isDirectory) it else zipTree(it) })
//
//            val jvmJar = tasks.named<Jar>("jvmJar").get()
//            from(jvmJar.archiveFile.map { zipTree(it) })
//
//            archiveClassifier.set("fat")
//        }
