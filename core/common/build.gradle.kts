plugins {
  id("audiobook.library")
}

dependencies {
  implementation(libs.serialization.json)
  implementation(libs.androidxCore)

  testImplementation(libs.bundles.testing.jvm)
}
