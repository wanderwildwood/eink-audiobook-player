package audiobook.features.widget

import audiobook.app.features.widget.BaseWidgetProvider

interface WidgetGraph {
  fun inject(target: BaseWidgetProvider)
}
