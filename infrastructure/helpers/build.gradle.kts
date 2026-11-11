import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    `java-library`
}

apply(plugin = "org.springframework.boot")
apply(plugin = "io.spring.dependency-management")

tasks.named<BootJar>("bootJar") { enabled = false }
tasks.named<Jar>("jar") { enabled = true }

dependencies {
    api(project(":domain:model"))
    api(project(":domain:ports"))
    implementation(libs.boot.starter.web)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.boot.starter.test)
    testRuntimeOnly(libs.junit.platform.launcher)
}
