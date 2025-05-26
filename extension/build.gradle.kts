plugins {
    kotlin("js")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.25"
}

version = "0.0.5" 

repositories {
    mavenCentral()
}

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                devtool = "inline-source-map"
                cssSupport {
                    enabled.set(true)
                }
            }
            
            // Simple distribution without webpack complexity
            distribution {
                outputDirectory.set(project.projectDir.resolve("dist"))
            }
        }
        binaries.executable()
    }

    sourceSets {
        val main by getting {
            dependencies {
                implementation(project(":shared"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

                // For Chrome extension types
                implementation(npm("webextension-polyfill", "0.10.0"))
                
                // React dependencies for Kotlin 1.9.25
                implementation(platform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:"))
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion")
                
                // React NPM dependencies
                implementation(npm("react", "18.2.0"))
                implementation(npm("react-dom", "18.2.0"))
            }
        }
        
        val test by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}