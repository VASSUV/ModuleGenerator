package {{ .screen_package }}

import com.example.base.ui.mvi.core.BaseViewIntents

internal object {{ .screen_name }}Screen {
  class ViewIntents : BaseViewIntents() {
    val navigateOnBack = intent(name = "navigateOnBack")
  }

  object ViewState
}
