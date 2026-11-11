// domain/usecases — clases Java planas (sin Spring). Depende de model + ports.
plugins {
    `java-library`
}

dependencies {
    api(project(":domain:model"))
    api(project(":domain:ports"))

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}
