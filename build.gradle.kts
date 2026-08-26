plugins {
  alias(libs.plugins.compose.compiler) apply false
  id("audiobook.ktlint")
}

tasks.wrapper {
  distributionType = Wrapper.DistributionType.ALL
}
