package ru.vassuv.plugin.createfromtemplate.dialog

import com.intellij.ui.JBColor
import com.intellij.ui.SingleSelectionModel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.wizard.WizardNavigationState
import com.intellij.ui.wizard.WizardStep
import kotlinx.coroutines.*
import ru.vassuv.plugin.createfromtemplate.model.Const
import ru.vassuv.plugin.createfromtemplate.model.SettingsViewModel
import ru.vassuv.plugin.createfromtemplate.model.entity.Template
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import javax.swing.*
import javax.swing.border.StrokeBorder


@OptIn(DelicateCoroutinesApi::class)
class MyWizardStep1(private val viewModel: SettingsViewModel) : WizardStep<TemplateSettingsWizardModel>() {

    private var templatesTree = JBList<Template>()
        .apply {
            border = StrokeBorder(BasicStroke(3f), JBColor(Color.gray, Color.DARK_GRAY))
            layoutOrientation = JList.VERTICAL
            selectionMode = ListSelectionModel.SINGLE_SELECTION
            selectionModel = SingleSelectionModel()
            installCellRenderer {
                JLabel().apply {
                    val icon = if (it.isTemplate) "\uD83D\uDCC4" else "\uD83D\uDCC1"
                    text = "${it.prefix}$icon ${it.name}"
                    font = Font(Font.MONOSPACED, Font.PLAIN, 14)
                    isEnabled = it.isTemplate
                }
            }
            addListSelectionListener {
                viewModel.changeSelectedTemplate(selectedValue)
                setSelectedValue(viewModel.selectedTemplate.value, true)
                updateUI()
            }
        }

    private val scrollPane = JBScrollPane(templatesTree)

    private var contentPanel: JPanel = JPanel()
        .apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(JLabel(Const.CHOOSE_TEMPLATE).apply {
                alignmentX = 0.5f
                border = BorderFactory.createEmptyBorder(10, 0, 10, 0)
            })
            add(scrollPane)
        }

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }
    private val job = SupervisorJob()
    private val scope = CoroutineScope(GlobalScope.coroutineContext + job + coroutineExceptionHandler)

    override fun prepare(state: WizardNavigationState?): JComponent {
        return contentPanel.apply { onInit(state) }
    }

    private fun onInit(state: WizardNavigationState?) {
        scope.launch {
            viewModel.templates.collect {
                templatesTree.setListData(it.toTypedArray())
                updateUi()
            }
        }
        scope.launch {
            viewModel.selectedTemplate.collect {
                state?.NEXT?.isEnabled = it != null
                templatesTree.clearSelection()
                updateUi()
            }
        }
    }

    private fun updateUi() {
        templatesTree.updateUI()
    }

    override fun onFinish(): Boolean {
        scope.coroutineContext.cancelChildren()
        job.cancel()
        return super.onFinish()
    }

    override fun onCancel(): Boolean {
        scope.coroutineContext.cancelChildren()
        job.cancel()
        return super.onCancel()
    }

    override fun onNext(model: TemplateSettingsWizardModel?): WizardStep<*> {
        return if (viewModel.selectedTemplate.value == null) {
            this
        } else {
            super.onNext(model)
        }
    }
}