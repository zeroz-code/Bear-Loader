// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

// Convenience CI task to run common checks
tasks.register("ciChecks") {
    group = "verification"
    description = "Run lint, tests and assembleDebug for CI validation"
    dependsOn(":app:lint", 
              ":app:test", 
              ":app:assembleDebug")
}