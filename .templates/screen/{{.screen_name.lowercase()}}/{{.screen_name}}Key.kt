package {{ .screen_package }}

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import com.example.base.ui.core.util.ToothpickScreenBindings
import com.example.base.ui.mvi.core.model.ComponentConfig
import com.example.base.ui.mvi.core.model.ScreenKey

@Parcelize
object {{ .screen_name }}Key : ScreenKey() {
  // See NOTE_IGNORED_ON_PARCEL_AND_OBJECT
  @Suppress("INAPPLICABLE_IGNORED_ON_PARCEL")
  @IgnoredOnParcel
  override val componentConfig = ComponentConfig(
    presenterClass = {{ .screen_name }}Presenter::class.java,
    controllerClass = {{ .screen_name }}Controller::class.java,
    screenBindings = ToothpickScreenBindings {
      bind({{ .screen_name }}Wiring::class.java)
        .to({{ .screen_name }}WiringImpl::class.java)
        .singleton()
    }
  )
}
