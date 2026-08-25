package voice.navigation

import android.content.Intent
import android.net.Uri
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import voice.core.common.serialization.UriSerializer
import voice.core.data.BookId

sealed interface Destination {

  @Serializable
  data class Playback(val bookId: BookId) : Compose {
    override val trackingName: String get() = "Playback"
  }

  @Serializable
  data class Bookmarks(val bookId: BookId) : Compose {
    override val trackingName: String get() = "Bookmarks"
  }

  data class Website(val url: String) : Destination

  data class Activity(val intent: Intent) : Destination

  @Serializable
  sealed interface Compose :
    Destination,
    NavKey {
    val trackingName: String
  }

  @Serializable
  data object Settings : Compose {
    override val trackingName: String get() = "Settings"
  }

  @Serializable
  data object SupportVoice : Compose {
    override val trackingName: String get() = "SupportVoice"
  }

  @Serializable
  data object DeveloperSettings : Compose {
    override val trackingName: String get() = "DeveloperSettings"
  }

  @Serializable
  data object BookOverview : Compose {
    override val trackingName: String get() = "BookOverview"
  }

  /**
   * One shelf of books, named [name] and holding whatever [shelf] groups by. [name] is null for
   * the books that have no such name - no author folder, or no genre - so which of the two a null
   * means has to be carried alongside it.
   */
  @Serializable
  data class AuthorBooks(
    val name: String?,
    val shelf: Shelf,
  ) : Compose {
    override val trackingName: String get() = "AuthorBooks"

    @Serializable
    enum class Shelf {
      AUTHOR_FOLDER,
      GENRE,
    }
  }

  @Serializable
  data object FolderPicker : Compose {
    override val trackingName: String get() = "FolderPicker"
  }

  @Serializable
  data class SelectFolderType(
    val uri:
    @Serializable(with = UriSerializer::class)
    Uri,
    val origin: Origin,
  ) : Compose {
    override val trackingName: String = "SelectFolderType"
  }

  @Serializable
  data object OnboardingWelcome : Compose {
    override val trackingName: String get() = "OnboardingWelcome"
  }

  @Serializable
  data object OnboardingCompletion : Compose {
    override val trackingName: String get() = "OnboardingCompletion"
  }

  @Serializable
  data object OnboardingExplanation : Compose {
    override val trackingName: String get() = "OnboardingExplanation"
  }

  data object BatteryOptimization : Destination

  @Serializable
  data class AddContent(val origin: Origin) : Compose {
    override val trackingName: String = "AddContent"
  }
}
