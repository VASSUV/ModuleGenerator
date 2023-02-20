package ru.vassuv.plugin.createfromtemplate.dialog

import com.intellij.ui.JBColor
import com.intellij.ui.SingleSelectionModel
import com.intellij.ui.components.JBList
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.LCFlags
import com.intellij.ui.layout.panel
import com.intellij.ui.wizard.WizardNavigationState
import com.intellij.ui.wizard.WizardStep
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.swing.Swing
import ru.vassuv.plugin.createfromtemplate.model.Const.CHOOSE_TEMPLATE
import ru.vassuv.plugin.createfromtemplate.model.Const.TEMPLATES
import ru.vassuv.plugin.createfromtemplate.model.SettingsViewModel
import ru.vassuv.plugin.createfromtemplate.model.entity.Template
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import javax.swing.*
import javax.swing.border.StrokeBorder


class MyWizardStep1(private val viewModel: SettingsViewModel) : WizardStep<TemplateSettingsWizardModel>() {
    private var templatesLabel = JLabel(TEMPLATES)

    @Suppress("UnstableApiUsage")
    private var templatesTree = JBList<Template>()
        .apply {
            border = StrokeBorder(BasicStroke(3f), JBColor(Color.gray, Color.DARK_GRAY))
            layoutOrientation = JList.VERTICAL
            selectionMode = ListSelectionModel.SINGLE_SELECTION
            selectionModel = SingleSelectionModel()
            installCellRenderer {
                JLabel().apply {
                    val icon = if(it.isTemplate) "\uD83D\uDCC4" else "\uD83D\uDCC1"
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

    private var contentPanel: JPanel = panel(LCFlags.fill, title = CHOOSE_TEMPLATE) {
        row(templatesLabel) {
            JScrollPane(templatesTree)(CCFlags.grow)
        }
    }
    private val coroutineExceptionHandler = CoroutineExceptionHandler{_, throwable ->
        throwable.printStackTrace()
    }
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Swing + job + coroutineExceptionHandler)

    override fun prepare(state: WizardNavigationState?): JComponent {
        return contentPanel.apply { onInit(state) }
    }

    private fun onInit(state: WizardNavigationState?) {
        scope.launch {
            viewModel.templates.collect {
                templatesTree.setListData(it.toTypedArray())
                templatesTree.updateUI()
            }
        }
        scope.launch {
            viewModel.selectedTemplate.collect {
                state?.NEXT?.isEnabled = it != null
                templatesTree.clearSelection()
                templatesTree.updateUI()
            }
        }
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
        return if(viewModel.selectedTemplate.value == null) {
            this
        } else {
            super.onNext(model)
        }
    }
}