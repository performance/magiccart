plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    js(IR) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
            webpackTask {
                mainOutputFileName = "content.js"
            }
            testTask {
                enabled = false
            }
        }
    }
    
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
                
                // Browser dependencies only for now
                
                // Browser API
                implementation(npm("@types/chrome", "0.0.246"))
            }
        }
        
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}