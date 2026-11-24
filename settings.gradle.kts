pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "keystone-llm-sdk"

include(":llm-core")
include(":llm-ktor")
include(":llm-openai")
include(":llm-gemini")
include(":llm-ondevice")
include(":samples:jvm-cli")
include(":samples:android-chat")
