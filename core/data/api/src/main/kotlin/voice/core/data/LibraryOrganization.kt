package voice.core.data

import kotlinx.serialization.Serializable

@Serializable
public enum class LibraryOrganization {
  AUTHOR_FOLDERS,
  FLAT_LIST,
  BY_STATUS,
}
