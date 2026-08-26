package audiobook.core.data.store

import androidx.datastore.core.Serializer
import audiobook.core.logging.api.Logger
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.InputStream
import java.io.OutputStream

internal class KotlinxDataStoreSerializer<T>(
  override val defaultValue: T,
  private val json: Json,
  private val serializer: KSerializer<T>,
) : Serializer<T> {

  /**
   * Falls back to [defaultValue] rather than throwing. A stored value the current build no longer
   * understands - a removed enum constant, a renamed field - otherwise propagates out of every
   * read of this store and takes the screen collecting it down with it. A setting that quietly
   * returns to its default is the better failure.
   */
  @OptIn(ExperimentalSerializationApi::class)
  override suspend fun readFrom(input: InputStream): T {
    return try {
      json.decodeFromStream(serializer, input)
    } catch (e: SerializationException) {
      Logger.w(e, "Could not read stored value, falling back to the default")
      defaultValue
    }
  }

  @OptIn(ExperimentalSerializationApi::class)
  override suspend fun writeTo(
    t: T,
    output: OutputStream,
  ) {
    json.encodeToStream(serializer, t, output)
  }
}
