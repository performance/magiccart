plugins {
    kotlin("jvm") version "1.9.25" apply false
    kotlin("multiplatform") version "1.9.25" apply false
    kotlin("js") version "1.9.25" apply false
    kotlin("plugin.spring") version "1.9.25" apply false
    kotlin("plugin.serialization") version "1.9.25" apply false
    id("org.springframework.boot") version "3.4.5" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    group = "com.oboco"
    version = "0.0.1-SNAPSHOT"
}