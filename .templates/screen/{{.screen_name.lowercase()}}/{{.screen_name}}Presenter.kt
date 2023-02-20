package {{ .screen_package }}

import ru.dimsuz.unicorn.reactivex.Machine
import ru.dimsuz.unicorn.reactivex.machine
import com.example.base.core.util.AppSchedulers
import com.example.base.ui.mvi.core.BasePresenter
import {{ .screen_package }}.{{ .screen_name }}Screen.ViewIntents
import {{ .screen_package }}.{{ .screen_name }}Screen.ViewState
import javax.inject.Inject

internal class {{ .screen_name }}Presenter @Inject constructor(
  schedulers: AppSchedulers,
  private val wiring: {{ .screen_name }}Wiring,
) : BasePresenter<ViewState, ViewIntents, Unit, Unit>(schedulers) {

  override fun createMachine(): Machine<ViewState, Unit> {
    return machine {
      initial = ViewState to null

      onEach(intent(ViewIntents::navigateOnBack)) {
        action { _, _, _ ->
          wiring.navigateOnBack()
        }
      }
    }
  }
}
