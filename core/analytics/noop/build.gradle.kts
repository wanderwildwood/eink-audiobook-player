plugins {
  id("audiobook.library")
  alias(libs.plugins.metro)
}

dependencies {
  api(projects.core.analytics.api)
}
