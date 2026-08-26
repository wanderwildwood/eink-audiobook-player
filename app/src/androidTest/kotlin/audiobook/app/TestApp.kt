package audiobook.app

import audiobook.app.di.App
import audiobook.app.di.AppGraph
import dev.zacsweers.metro.createGraphFactory

class TestApp : App() {

  override fun createGraph(): AppGraph {
    return createGraphFactory<TestGraph.Factory>()
      .create(this)
  }
}
