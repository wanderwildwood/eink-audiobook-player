plugins {
  id("audiobook.library")
  alias(libs.plugins.metro)
}

dependencies {
  implementation(projects.core.remoteconfig.api)
}
