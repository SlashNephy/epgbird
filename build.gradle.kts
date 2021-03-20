plugins {
    kotlin("jvm") version "1.4.30"
    kotlin("plugin.serialization") version "1.4.30"
    id("com.github.johnrengelman.shadow") version "6.1.0"

    id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
    id("com.adarshr.test-logger") version "2.1.1"
    id("net.rdrei.android.buildtimetracker") version "0.11.0"
}

object Versions {
    const val Penicillin = "6.1.0"
    const val Ktor = "1.5.2"
    const val kaml = "0.28.3"

    const val Exposed = "0.29.1"
    const val SQLiteJDBC = "3.30.1"

    const val KotlinLogging = "2.0.4"
    const val Logback = "1.2.3"
    const val jansi = "1.18"

    const val JUnit = "5.7.0"
}

object Libraries {
    const val Penicillin = "blue.starry:penicillin:${Versions.Penicillin}"
    const val KtorClientCIO = "io.ktor:ktor-client-cio:${Versions.Ktor}"
    const val KtorClientSerialization = "io.ktor:ktor-client-serialization:${Versions.Ktor}"
    const val kaml = "com.charleskorn.kaml:kaml:${Versions.kaml}"

    const val ExposedCore = "org.jetbrains.exposed:exposed-core:${Versions.Exposed}"
    const val ExposedJDBC = "org.jetbrains.exposed:exposed-jdbc:${Versions.Exposed}"
    const val ExposedJavaTime = "org.jetbrains.exposed:exposed-java-time:${Versions.Exposed}"
    const val SqliteJDBC = "org.xerial:sqlite-jdbc:${Versions.SQLiteJDBC}"

    const val KotlinLogging = "io.github.microutils:kotlin-logging:${Versions.KotlinLogging}"
    const val LogbackCore = "ch.qos.logback:logback-core:${Versions.Logback}"
    const val LogbackClassic = "ch.qos.logback:logback-classic:${Versions.Logback}"
    const val Jansi = "org.fusesource.jansi:jansi:${Versions.jansi}"
    const val JUnitJupiter = "org.junit.jupiter:junit-jupiter:${Versions.JUnit}"

    val ExperimentalAnnotations = setOf(
        "kotlinx.coroutines.ExperimentalCoroutinesApi",
        "kotlin.io.path.ExperimentalPathApi",
        "kotlin.time.ExperimentalTime",
        "kotlin.ExperimentalStdlibApi",
        "kotlinx.coroutines.FlowPreview"
    )
}

repositories {
    mavenCentral()

    // for exposed
    jcenter()
    // TODO: It should be removed by May 1, 2021. It is potentially used by kotlinx-datetime.
    maven(url = "https://kotlin.bintray.com/kotlinx")
}

dependencies {
    implementation(Libraries.Penicillin)
    implementation(Libraries.KtorClientCIO)
    implementation(Libraries.KtorClientSerialization)
    implementation(Libraries.kaml)

    implementation(Libraries.ExposedCore)
    implementation(Libraries.ExposedJDBC)
    implementation(Libraries.ExposedJavaTime)
    implementation(Libraries.SqliteJDBC)

    implementation(Libraries.KotlinLogging)
    implementation(Libraries.LogbackCore)
    implementation(Libraries.LogbackClassic)
    implementation(Libraries.Jansi)

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
    testImplementation(Libraries.JUnitJupiter)
}

kotlin {
    target {
        compilations.all {
            kotlinOptions {
                jvmTarget = JavaVersion.VERSION_1_8.toString()
                apiVersion = "1.4"
                languageVersion = "1.4"
                allWarningsAsErrors = true
                verbose = true
            }
        }
    }

    sourceSets.all {
        languageSettings.progressiveMode = true

        Libraries.ExperimentalAnnotations.forEach {
            languageSettings.useExperimentalAnnotation(it)
        }
    }
}

/*
 * Tests
 */

ktlint {
    verbose.set(true)
    outputToConsole.set(true)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
    ignoreFailures.set(true)
}

buildtimetracker {
    reporters {
        register("summary") {
            options["ordered"] = "true"
            options["barstyle"] = "ascii"
            options["shortenTaskNames"] = "false"
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()

    testLogging {
        showStandardStreams = true
        events("passed", "failed")
    }

    testlogger {
        theme = com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA_PARALLEL
    }
}

task<JavaExec>("run") {
    dependsOn("build")

    group = "application"
    main = "blue.starry.epgbird.MainKt"
    classpath(configurations.runtimeClasspath, tasks.jar)
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest {
        attributes("Main-Class" to "blue.starry.epgbird.MainKt")
    }
}
