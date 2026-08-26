@file:Suppress("UnstableApiUsage")

import com.android.build.api.dsl.ManagedVirtualDevice
import java.util.Properties

plugins {
  id("audiobook.app")
  id("audiobook.compose")
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.metro)
}

android {

  namespace = "audiobook.app"

  dependenciesInfo {
    // disable the dependencies info in apks to allow reproducible builds
    // see https://github.com/PaulWoitaschek/Voice/discussions/2862#discussioncomment-13622836
    includeInApk = false
  }

  defaultConfig {
    applicationId = "com.wanderwildwood.einkaudiobookplayer"

    // CI passes both of these from the tag it is building. Without them, this is a local build.
    versionName = providers.gradleProperty("audiobook.versionName").orNull ?: localVersionName()

    // Deliberately 1, and deliberately not Int.MAX_VALUE. A local build used to install as the
    // highest version code there is, which then outranked every real release forever: the next
    // install of a published APK failed with INSTALL_FAILED_VERSION_DOWNGRADE, and the ways past
    // it were `-d` or an uninstall that would take the reading positions and bookmarks with it.
    // At 1, a local build can never block a release. Installing a local build *over* a release is
    // what now needs `adb install -r -d`, which is the direction worth being inconvenienced in -
    // that is a developer at a terminal, not someone trying to update their audiobook player.
    versionCode = providers.gradleProperty("audiobook.versionCode").orNull?.toInt() ?: 1

    testInstrumentationRunner = "audiobook.app.AppJUnitRunner"
  }

  val distributionFlavor = "distribution"
  flavorDimensions += distributionFlavor
  productFlavors {
    register("free") {
      dimension = distributionFlavor
    }
  }

  val signingPropertiesFile = layout.projectDirectory.file("../signing/signing.properties").asFile
  val signingKeystoreFile = layout.projectDirectory.file("../signing/signing.keystore").asFile
  val appSigningConfig = if (signingPropertiesFile.isFile) {
    val signingProperties = Properties().apply {
      signingPropertiesFile.inputStream().use(::load)
    }
    signingConfigs.create("signing") {
      storeFile = signingKeystoreFile
      storePassword = signingProperties.getProperty("STORE_PASSWORD")
      keyAlias = signingProperties.getProperty("KEY_ALIAS")
      keyPassword = signingProperties.getProperty("KEY_PASSWORD")
    }
  } else {
    null
  }

  // A fixed debug keystore checked into the repo, so debug builds are signed identically on
  // every machine and CI run - lets a downloaded debug APK be upgraded in place by a later one,
  // instead of each environment's own auto-generated ~/.android/debug.keystore producing a
  // different signature every time.
  // A fixed keystore checked into the repo (no real security value - not Play Store
  // distributed), reused for BOTH debug and release build types. Every prior published release
  // (v1.0.0 through v1.1.2) was signed with this same keystore via the debug build type, so
  // release must keep using it too - a different keystore would force everyone who's already
  // installed the app to uninstall (and lose their library data) before they could take a new
  // update.
  signingConfigs.getByName("debug") {
    storeFile = layout.projectDirectory.file("debug.keystore").asFile
    storePassword = "android"
    keyAlias = "androiddebugkey"
    keyPassword = "android"
  }

  buildTypes {
    getByName("release") {
      isMinifyEnabled = true
      isShrinkResources = true
      signingConfig = signingConfigs.getByName("debug")
    }
    getByName("debug") {
      isMinifyEnabled = false
    }
    all {
      if (appSigningConfig != null) {
        signingConfig = appSigningConfig
      }
      setProguardFiles(
        listOf(
          getDefaultProguardFile("proguard-android-optimize.txt"),
          "proguard.pro",
        ),
      )
    }
  }

  testOptions {
    unitTests {
      isReturnDefaultValues = true
      isIncludeAndroidResources = true
    }
    animationsDisabled = true
    execution = "ANDROIDX_TEST_ORCHESTRATOR"
    managedDevices {
      allDevices.create("audiobookDevice", ManagedVirtualDevice::class.java) {
        device = "Pixel 9"
        apiLevel = 33
      }
    }
  }

  lint {
    checkDependencies = true
    ignoreTestSources = true
    checkReleaseBuilds = false
    warningsAsErrors = providers.gradleProperty("audiobook.warningsAsErrors").get().toBooleanStrict()
  }

  packaging {
    with(resources.pickFirsts) {
      add("META-INF/atomicfu.kotlin_module")
      add("META-INF/core.kotlin_module")
    }
  }

  buildFeatures {
    buildConfig = true
  }
}

dependencies {
  implementation(projects.core.strings)
  implementation(projects.core.ui)
  implementation(projects.core.common)
  implementation(projects.core.data.api)
  implementation(projects.core.data.impl)
  implementation(projects.core.playback)
  implementation(projects.core.scanner)
  implementation(projects.core.featureflag)
  implementation(projects.core.initializer)
  implementation(projects.features.playbackScreen)
  implementation(projects.navigation)
  implementation(projects.core.sleeptimer.api)
  implementation(projects.core.sleeptimer.impl)
  implementation(projects.features.settings)
  implementation(projects.features.folderPicker)
  implementation(projects.features.bookOverview)
  implementation(projects.core.search)
  implementation(projects.core.documentfile)
  implementation(projects.features.bookmark)
  implementation(projects.features.widget)

  implementation(libs.appCompat)
  implementation(libs.lifecycle.compose)
  implementation(libs.datastore)

  implementation(libs.navigation3.ui)

  implementation(libs.serialization.json)



  implementation(projects.core.remoteconfig.api)
  add("freeImplementation", projects.core.remoteconfig.noop)

  implementation(projects.core.analytics.api)
  add("freeImplementation", projects.core.analytics.noop)

  debugImplementation(projects.core.logging.debug)

  implementation(libs.androidxCore)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)

  implementation(libs.media3.exoplayer)
  implementation(libs.media3.session)

  testImplementation(libs.androidX.test.runner)
  testImplementation(libs.androidX.test.junit)
  testImplementation(libs.androidX.test.core)
  testImplementation(libs.robolectric)
  testImplementation(libs.coroutines.test)
  testImplementation(kotlin("reflect"))

  debugImplementation(libs.compose.ui.testManifest)

  androidTestImplementation(platform(libs.compose.bom))
  androidTestImplementation(libs.androidX.test.runner)
  androidTestImplementation(libs.androidX.test.rules)
  androidTestImplementation(libs.androidX.test.junit)
  androidTestImplementation(libs.media3.testUtils.core)
  androidTestImplementation(libs.kotlin.testJunit)
  androidTestImplementation(libs.androidX.test.services)
  androidTestImplementation(libs.compose.ui.testJunit)
  androidTestImplementation(libs.coroutines.test)
  androidTestUtil(libs.androidX.test.orchestrator)
}

/**
 * What the working tree actually is, for builds CI did not stamp. Beats reporting "1.0.0", which
 * looked like a real version in the app's own About row and told you nothing about what was
 * installed. Falls back to a plain marker if git cannot answer.
 */
fun localVersionName(): String {
  val described = runCatching {
    providers.exec {
      commandLine("git", "describe", "--tags", "--always", "--dirty")
    }.standardOutput.asText.get().trim()
  }.getOrNull()
  return if (described.isNullOrBlank()) "local" else "$described-local"
}
