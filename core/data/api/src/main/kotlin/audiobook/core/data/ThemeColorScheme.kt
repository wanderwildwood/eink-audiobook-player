package audiobook.core.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class ThemeColorScheme {
  @SerialName("Blue")
  Blue,

  @SerialName("Dynamic")
  Dynamic,
}
