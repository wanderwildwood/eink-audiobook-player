package audiobook.core.sleeptimer

interface ShakeChime {

  /**
   * Plays a short confirmation tone. Returns immediately; the tone plays in the background.
   */
  fun play()
}
