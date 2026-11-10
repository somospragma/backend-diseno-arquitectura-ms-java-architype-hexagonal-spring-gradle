rootProject.name = "accounts-service"

include(
    ":domain:model",
    ":domain:ports",
    ":domain:usecases",
    ":infrastructure:driven-adapters:persistence",
    ":infrastructure:driven-adapters:external-service-client-api",
    ":infrastructure:driven-adapters:dynamodb-persistence",
    ":infrastructure:driven-adapters:redis-cache",
    ":infrastructure:driven-adapters:caffeine-cache",
    ":infrastructure:driven-adapters:event-publisher",
    ":infrastructure:driven-adapters:sns-publisher",
    ":infrastructure:driven-adapters:kafka-messaging",
    ":infrastructure:driven-adapters:sqs-consumer",
    ":infrastructure:entry-points:rest",
    ":infrastructure:entry-points:grpc",
    ":infrastructure:helpers",
    ":application:app-service",
)
