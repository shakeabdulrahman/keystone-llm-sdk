plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.binary.compatibility.validator)
}

allprojects {
    group = providers.gradleProperty("GROUP").get()
    version = providers.gradleProperty("VERSION_NAME").get()
}

// Public API dumps (`./gradlew apiDump`, checked by `./gradlew apiCheck` in CI).
apiValidation {
    ignoredProjects += listOf("jvm-cli", "android-chat")
    @OptIn(kotlinx.validation.ExperimentalBCVApi::class)
    klib { enabled = true }
    nonPublicMarkers += "io.github.shakeabdulrahman.keystone.llm.InternalKeystoneLlmApi"
}

// One API reference site for all library modules: `./gradlew dokkaGenerate`.
dependencies {
    dokka(project(":llm-core"))
    dokka(project(":llm-ktor"))
    dokka(project(":llm-openai"))
    dokka(project(":llm-gemini"))
    dokka(project(":llm-ondevice"))
}
