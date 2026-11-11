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
    implementation(platform(libs.awssdk.bom))
    implementation(libs.awssdk.dynamodb)
    implementation(libs.awssdk.dynamodb.enhanced)
    implementation(libs.boot.starter)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.boot.starter.test)
    testRuntimeOnly(libs.junit.platform.launcher)
}
