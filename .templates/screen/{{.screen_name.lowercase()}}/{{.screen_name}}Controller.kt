package {{ .screen_package }}

import com.badoo.mvicore.modelWatcher
import com.example.base.ui.mvi.core.BaseController
import com.example.base.ui.mvi.core.bindClicks
import com.example.base.ui.mvi.core.util.requireActivity
import {{ .screen_package }}.{{ .screen_name }}Screen.ViewIntents
import {{ .screen_package }}.{{ .screen_name }}Screen.ViewState

internal class {{ .screen_name }}Controller :
  BaseController<{{ .screen_name }}Layout, ViewState, ViewIntents, Unit>() {

  override fun createConfig(): Config<{{ .screen_name }}Layout, ViewIntents> {
    return object : Config<{{ .screen_name }}Layout, ViewIntents> {
      override val rootViewConstructor = ::{{ .screen_name }}Layout
      override val intentsConstructor = ::ViewIntents
    }
  }

  override fun initializeView() {
    rootView.backButton.bindClicks { handleBack() }
  }

  override fun handleBack(): Boolean {
    intents.navigateOnBack()
    return true
  }

  override fun configureModelWatcher() = modelWatcher<ViewState> {
  }
}
