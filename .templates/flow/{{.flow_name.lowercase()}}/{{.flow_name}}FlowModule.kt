package {{.flow_package}}

import toothpick.config.Module

internal class {{.flow_name}}FlowModule : Module() {
  init {
    bind({{.flow_name}}Flow.Coordinator::class.java)
      .to({{.flow_name}}FlowCoordinator::class.java)
      .singleton()
  }
}
