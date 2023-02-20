package {{ .screen_package }}

import android.content.Context
import com.squareup.contour.ContourLayout
import com.example.disk.core.ui.uikit.component.button.backButton

internal class {{ .screen_name }}Layout(context: Context) : ContourLayout(context) {
  val backButton = backButton { }

  init {
      backButton.layoutBy(
        x = leftTo { parent.left() + 4.xdip }.widthOf { 48.xdip },
        y = topTo { parent.top() }.heightOf { 48.ydip }
      )
  }
}
