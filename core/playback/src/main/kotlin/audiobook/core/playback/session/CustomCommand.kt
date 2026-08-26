package audiobook.core.playback.session

import android.os.Bundle
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import audiobook.core.playback.misc.Decibel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
internal sealed interface CustomCommand {

  @Serializable
  data object ForceSeekToNext : CustomCommand

  @Serializable
  data object ForceSeekToPrevious : CustomCommand

  @Serializable
  data class SetSkipSilence(val skipSilence: Boolean) : CustomCommand

  @Serializable
  data class SetGain(val gain: Decibel) : CustomCommand

  companion object {

    const val CUSTOM_COMMAND_ACTION = "audiobookCommandAction"
    internal const val CUSTOM_COMMAND_EXTRA = "audiobookCommandExtra"
    internal fun parse(
      command: SessionCommand,
      args: Bundle,
    ): CustomCommand? {
      if (command.customAction != CUSTOM_COMMAND_ACTION) {
        return null
      }
      val json = args.getString(CUSTOM_COMMAND_EXTRA) ?: return null
      return Json.decodeFromString(serializer(), json)
    }
  }
}

internal fun MediaController.sendCustomCommand(command: CustomCommand) {
  val json = Json.encodeToString(CustomCommand.serializer(), command)
  sendCustomCommand(
    SessionCommand(CustomCommand.CUSTOM_COMMAND_ACTION, Bundle.EMPTY),
    Bundle().apply {
      putString(CustomCommand.CUSTOM_COMMAND_EXTRA, json)
    },
  )
}
