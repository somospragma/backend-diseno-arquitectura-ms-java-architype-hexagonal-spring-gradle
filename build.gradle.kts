import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import info.solidsoft.gradle.pitest.PitestPluginExtension

plugins {
    id("java")
    jacoco
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.pitest) apply false
    alias(libs.plugins.protobuf) apply false
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.owasp.dependencycheck)
}

allprojects {
    group = property("package") as String
    version = "1.0.0"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "jacoco")
    apply(plugin = "info.solidsoft.pitest")
    apply(plugin = "org.owasp.dependencycheck")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    val catalog = rootProject.the<VersionCatalogsExtension>().named("libs")

    dependencies {
        add("compileOnly", catalog.findLibrary("lombok").get())
        add("annotationProcessor", catalog.findLibrary("lombok").get())
        add("annotationProcessor", catalog.findLibrary("mapstruct-processor").get())
        add("testCompileOnly", catalog.findLibrary("lombok").get())
        add("testAnnotationProcessor", catalog.findLibrary("lombok").get())

        // Parches de seguridad (CVE) de transitivas no gestionadas por el BOM de Boot.
        // Un constraint solo fija la version si la libreria ya llega al grafo del modulo.
        constraints {
            listOf("lz4-java", "snappy-java", "guava", "scram-client", "scram-common").forEach {
                add("implementation", catalog.findLibrary(it).get())
            }
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    // MapStruct — suprimir timestamps en el codigo generado
    tasks.withType<JavaCompile>().configureEach {
        options.compilerArgs.add("-Amapstruct.suppressGeneratorTimestamp=true")
    }

    // --- Quality gates (equivalente a main.gradle.kts, aplicado programaticamente) ---

    // JaCoCo — cobertura 70% general / 80% dominio
    extensions.configure<JacocoPluginExtension>("jacoco") {
        toolVersion = "0.8.12"
    }

    tasks.withType<JacocoReport>().configureEach {
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }

    tasks.named("test") {
        finalizedBy(tasks.named("jacocoTestReport"))
    }

    // PIT — Mutation Testing (solo dominio: model + usecase), umbral 60%
    extensions.configure<PitestPluginExtension>("pitest") {
        targetClasses.set(setOf("co.com.pragma.accounts.usecase.*", "co.com.pragma.accounts.model.*"))
        targetTests.set(setOf("co.com.pragma.accounts.usecase.*", "co.com.pragma.accounts.model.*"))
        threads.set(8)
        outputFormats.set(setOf("XML", "HTML"))
        junit5PluginVersion.set(catalog.findVersion("pitest-junit5").get().requiredVersion)
        mutationThreshold.set(60)
        failWhenNoMutations.set(false)
    }

    // OWASP — CVSS >= 7 rompe el build
    extensions.configure<org.owasp.dependencycheck.gradle.extension.DependencyCheckExtension>("dependencyCheck") {
        formats = listOf("HTML", "JSON", "XML")
        failBuildOnCVSS = 7.0f
        scanConfigurations = listOf("runtimeClasspath")
    }
}

// SonarQube — analisis estatico (configurado a nivel root)
sonar {
    properties {
        property("sonar.host.url", System.getenv("SONAR_HOST_URL") ?: "")
        property("sonar.token", System.getenv("SONAR_TOKEN") ?: "")
        property(
            "sonar.coverage.exclusions",
            listOf(
                "**/config/**", "**/model/**", "**/dto/**",
                "**/mapper/**", "**/*MapperImpl.class",
                "**/entity/**", "**/MainApplication.java",
            ).joinToString(","),
        )
    }
}

// Exclusiones sensatas de cobertura: clases sin logica que no tiene sentido
// testear (arranque, DTOs/records, mappers generados, entidades, config/wiring).
val coverageExcludes = listOf(
    "**/MainApplication.*",
    "**/*Application.*",
    "**/config/**",
    "**/*Config.*",
    "**/*Config\$*.*",     // clases internas de wiring/config (p.ej. SmartLifecycle anonimo)
    "**/*Properties.*",
    "**/*Request.*",
    "**/*Response.*",
    "**/*Dto.*",
    "**/dto/**",
    "**/*Item.*",          // DynamoDbBean (POJO de mapeo)
    "**/*Entity.*",
    "**/entity/**",
    "**/*MapperImpl.*",    // generado por MapStruct
    "**/*Mapper.*",        // interfaz de mapeo (impl generado)
    "**/exception/**",     // jerarquia de excepciones (POJOs)
    "**/*Exception.*",
    "**/sensitive/**",     // anotacion + enums de clasificacion (sin logica)
    "**/grpc/v1/**",       // clases generadas por protoc (mensajes/stubs)
    "**/*Grpc.*",          // stubs gRPC generados
    "**/*OrBuilder.*",     // interfaces generadas de protobuf
    "**/*Proto.*"          // outer class generada de protobuf
)

fun filteredClasses(): List<Any> = subprojects.map { sp ->
    sp.sourceSets.main.get().output.asFileTree.matching { exclude(coverageExcludes) }
}

tasks.register<JacocoReport>("jacocoRootReport") {
    description = "Reporte unificado de cobertura JaCoCo (con exclusiones sensatas)"
    group = "verification"
    dependsOn(subprojects.map { it.tasks.named("test") })

    additionalSourceDirs.setFrom(subprojects.map { it.sourceSets.main.get().allSource.srcDirs })
    sourceDirectories.setFrom(subprojects.map { it.sourceSets.main.get().allSource.srcDirs })
    classDirectories.setFrom(filteredClasses())
    executionData.setFrom(subprojects.map {
        it.layout.buildDirectory.file("jacoco/test.exec")
    }.filter { it.get().asFile.exists() })

    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

// Gate de cobertura del artefacto: 90% de linea sobre el codigo con logica real
// (aplicando las mismas exclusiones). Rompe el build si baja del umbral.
tasks.register<JacocoCoverageVerification>("jacocoRootCoverageVerification") {
    group = "verification"
    dependsOn(tasks.named("jacocoRootReport"))

    sourceDirectories.setFrom(subprojects.map { it.sourceSets.main.get().allSource.srcDirs })
    classDirectories.setFrom(filteredClasses())
    executionData.setFrom(subprojects.map {
        it.layout.buildDirectory.file("jacoco/test.exec")
    }.filter { it.get().asFile.exists() })

    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.90".toBigDecimal()
            }
        }
    }
}

// El gate de cobertura del artefacto se engancha a check: `./gradlew check`
// falla si la cobertura de linea (con exclusiones) baja del 90%.
tasks.named("check") {
    dependsOn("jacocoRootCoverageVerification")
}
