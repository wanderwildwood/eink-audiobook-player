package audiobook.app.di

import audiobook.app.features.widget.BaseWidgetProvider
import audiobook.features.widget.WidgetGraph

interface AppGraph : WidgetGraph {

  fun inject(target: App)
  override fun inject(target: BaseWidgetProvider)
}
