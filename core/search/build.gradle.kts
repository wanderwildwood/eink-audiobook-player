plugins {
  id("audiobook.library")
  alias(libs.plugins.metro)
}

dependencies {
  implementation(projects.core.data.api)
  testImplementation(projects.core.data.impl)
}
