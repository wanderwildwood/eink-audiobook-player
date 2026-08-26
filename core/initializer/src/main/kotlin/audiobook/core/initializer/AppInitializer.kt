package audiobook.core.initializer

import android.app.Application

interface AppInitializer {

  fun onAppStart(application: Application)
}
