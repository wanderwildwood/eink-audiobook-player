plugins {
  `kotlin-dsl`
}

dependencies {
  implementation(libs.androidPluginForGradle)
  implementation(libs.kotlin.pluginForGradle)
  implementation(libs.kotlin.powerAssert)
  implementation(libs.compose.compiler.gradle.plugin)
  implementation(libs.ktlint.gradlePlugin)
}

gradlePlugin {
  plugins {
    create("library") {
      id = "audiobook.library"
      implementationClass = "LibraryPlugin"
    }
    create("app") {
      id = "audiobook.app"
      implementationClass = "AppPlugin"
    }
    create("compose") {
      id = "audiobook.compose"
      implementationClass = "ComposePlugin"
    }
    create("ktlint") {
      id = "audiobook.ktlint"
      implementationClass = "KtlintPlugin"
    }
  }
}

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(libs.versions.jvm.toolchain.get().toInt()))
  }
}
