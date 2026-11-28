import com.google.protobuf.gradle.id

plugins {
    `java-library`
    alias(libs.plugins.protobuf)
}

apply(plugin = "org.springframework.boot")
apply(plugin = "io.spring.dependency-management")

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") { enabled = false }
tasks.named<Jar>("jar") { enabled = true }

dependencies {
    implementation(project(":domain:model"))
    implementation(project(":domain:ports"))
    implementation(project(":domain:usecases"))

    implementation(platform(libs.grpc.bom))
    implementation(libs.grpc.stub)
    implementation(libs.grpc.protobuf)
    runtimeOnly(libs.grpc.netty.shaded)
    implementation(libs.protobuf.java)
    implementation(libs.boot.starter)
    implementation(libs.boot.autoconfigure)
    // Necesario para compilar el codigo generado (javax.annotation.Generated).
    compileOnly(libs.tomcat.annotations.api)

    testImplementation(platform(libs.junit.bom))
    testImplementation(platform(libs.grpc.bom))
    testImplementation(libs.boot.starter.test)
    testImplementation(libs.grpc.stub)
    testImplementation(libs.grpc.inprocess)
    testRuntimeOnly(libs.junit.platform.launcher)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.36.2"
    }
    plugins {
        // Plugin de gRPC Java (stubs bloqueantes/async estandar).
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.85.1"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                id("grpc")
            }
        }
    }
}
