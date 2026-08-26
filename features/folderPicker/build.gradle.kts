plugins {
  id("audiobook.library")
  id("audiobook.compose")
  alias(libs.plugins.metro)
}

android {
  androidResources {
    enable = true
  }
}

dependencies {
  implementation(projects.core.ui)
  implementation(projects.core.common)
  implementation(projects.core.strings)
  implementation(projects.core.playback)
  implementation(projects.core.data.api)
  implementation(projects.core.documentfile)
  implementation(projects.navigation)
  implementation(projects.core.featureflag)
  implementation(projects.core.scanner)

  implementation(libs.androidxCore)
  implementation(libs.documentFile)

  testImplementation(libs.molecule)
}
