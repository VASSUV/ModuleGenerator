package {{.flow_package}}

import com.example.base.ui.mvi.core.routing.Route
import com.example.base.ui.mvi.core.routing.RouteCommand
import com.example.base.ui.mvi.core.routing.Router
import com.example.base.ui.mvi.core.routing.coordinator.BaseFlowCoordinator
import com.example.app.core.ui.routing.AppRoute
import {{.flow_package}}.{{.flow_name}}Flow.Event
import {{.flow_package}}.{{.flow_name}}Flow.Result
import toothpick.Scope
import javax.inject.Inject

internal class {{.flow_name}}FlowCoordinator @Inject constructor(
  scope: Scope,
  private val router: Router,
) : BaseFlowCoordinator<Event, Result>(scope), {{.flow_name}}Flow.Coordinator {

  override fun createInitialRoute(beforePushClearUntil: Route?): RouteCommand {
    return router.push(AppRoute.Dummy(title = "{{.flow_name}}"), createContext())
  }

  override fun createHandleEventCommand(event: Event): RouteCommand? {
    return null
  }
}
