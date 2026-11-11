import org.springframework.boot.gradle.tasks.bundling.BootJar

apply(plugin = "java-library")
apply(plugin = "org.springframework.boot")
apply(plugin = "io.spring.dependency-management")

tasks.named<BootJar>("bootJar") { enabled = false }
tasks.named<Jar>("jar") { enabled = true }

dependencies {
    implementation(project(":domain:model"))
    implementation(project(":domain:ports"))
    implementation(project(":infrastructure:helpers"))
    implementation(libs.boot.starter.web)
    implementation(libs.mapstruct)
    implementation(libs.resilience4j.spring.boot3)
    implementation(libs.resilience4j.circuitbreaker)
    implementation(libs.resilience4j.retry)
    implementation(libs.resilience4j.bulkhead)
    implementation(libs.resilience4j.timelimiter)
    implementation(libs.resilience4j.micrometer)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.boot.starter.test)
    testImplementation(libs.boot.starter.restclient.test)
    testRuntimeOnly(libs.junit.platform.launcher)
}
