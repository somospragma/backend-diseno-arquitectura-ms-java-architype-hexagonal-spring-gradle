apply(plugin = "org.springframework.boot")
apply(plugin = "io.spring.dependency-management")

dependencies {
    implementation(project(":domain:model"))
    implementation(project(":domain:ports"))
    implementation(project(":domain:usecases"))
    implementation(project(":infrastructure:driven-adapters:persistence"))
    implementation(project(":infrastructure:driven-adapters:external-service-client-api"))
    implementation(project(":infrastructure:driven-adapters:dynamodb-persistence"))
    implementation(project(":infrastructure:driven-adapters:redis-cache"))
    implementation(project(":infrastructure:driven-adapters:caffeine-cache"))
    implementation(project(":infrastructure:driven-adapters:event-publisher"))
    implementation(project(":infrastructure:driven-adapters:sns-publisher"))
    implementation(project(":infrastructure:driven-adapters:kafka-messaging"))
    implementation(project(":infrastructure:driven-adapters:sqs-consumer"))
    implementation(project(":infrastructure:entry-points:rest"))
    implementation(project(":infrastructure:entry-points:grpc"))
    implementation(project(":infrastructure:helpers"))

    implementation(libs.boot.starter.web)
    implementation(libs.boot.starter.jpa)
    implementation(libs.boot.starter.actuator)
    implementation(libs.boot.starter.validation)
    implementation(libs.springdoc.webmvc)
    implementation(libs.logstash.encoder)
    implementation(libs.micrometer.registry.prometheus)
    runtimeOnly(libs.postgresql)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.boot.starter.test)
    testImplementation(libs.boot.starter.webmvc.test)
    testImplementation(libs.archunit.junit5)
    testRuntimeOnly(libs.h2)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.register<Test>("architectureTest") {
    useJUnitPlatform()
    include("**/HexagonalArchitectureTest.class")
}

tasks.named("build") {
    dependsOn("architectureTest")
}
