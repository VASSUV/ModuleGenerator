package {{ .screen_package }}

import javax.inject.Inject

internal interface {{ .screen_name }}Wiring {
  fun navigateOnBack()
}

internal class {{ .screen_name }}WiringImpl @Inject constructor() : {{ .screen_name }}Wiring {
  override fun navigateOnBack() { }
}
