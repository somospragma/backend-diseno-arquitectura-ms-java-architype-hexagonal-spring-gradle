// domain/ports — solo depende de model. Tipos de retorno planos (Optional/List).
plugins {
    `java-library`
}

dependencies {
    api(project(":domain:model"))
    api(libs.jspecify)
}
