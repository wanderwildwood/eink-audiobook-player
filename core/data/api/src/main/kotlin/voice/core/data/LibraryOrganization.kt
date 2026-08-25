package voice.core.data

import kotlinx.serialization.Serializable

@Serializable
public enum class LibraryOrganization {
  AUTHOR_FOLDERS,

  /**
   * No longer offered: one undivided list of every book was not worth a view of its own, and the
   * same listing lives under [BY_STATUS] with headings on it. Kept only so a stored setting still
   * reads back - the store's serializer has no fallback, and an unknown value would throw on read
   * rather than quietly reset. Treated as [BY_STATUS] wherever it is used.
   */
  FLAT_LIST,

  BY_STATUS,
}
