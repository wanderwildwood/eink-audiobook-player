package audiobook.core.data

import kotlinx.serialization.Serializable

@Serializable
public enum class LibraryOrganization {
  // Declaration order is the order the picker offers them in. Safe to reorder: the setting is
  // serialised by constant name, not by position.
  AUTHOR_FOLDERS,
  GENRE,
  BY_STATUS,
}
