include(":app")
include(":features:alarm-api")
include(":features:alarm")
include(":features:task")
include(":features:category-api")
include(":features:category")
include(":features:preference")
include(":features:tracker")
include(":features:search")
include(":features:glance")
include(":data:repository")
include(":data:local")
include(":data:datastore")
include(":libraries:core")
include(":libraries:coroutines")
include(":libraries:test")
include(":libraries:splitInstall")
include(":libraries:designsystem")
include(":libraries:navigation")

include(":domain")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        includeBuild("plugins")
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
