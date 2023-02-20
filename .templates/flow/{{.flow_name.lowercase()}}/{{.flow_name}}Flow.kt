package {{.flow_package}}

import com.example.base.ui.mvi.core.routing.coordinator.FlowConfig
import com.example.base.ui.mvi.core.routing.coordinator.FlowConstructor
import com.example.base.ui.mvi.core.routing.coordinator.FlowCoordinator

interface {{.flow_name}}Flow {
  companion object : FlowConstructor<Coordinator, Unit, Result>(
    FlowConfig(
      flowId = "{{.flow_name|lower}}_flow",
      flowModules = listOf({{.flow_name}}FlowModule()),
      coordinatorClass = Coordinator::class.java
    )
  )

  interface Coordinator : FlowCoordinator<Event, Result>

  enum class Result {
    Success,
    Dismissed
  }

  sealed class Event
}
