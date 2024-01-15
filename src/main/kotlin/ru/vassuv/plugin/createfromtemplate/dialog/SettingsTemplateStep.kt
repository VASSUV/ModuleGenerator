package ru.vassuv.plugin.createfromtemplate.dialog

import com.intellij.ui.JBColor
import com.intellij.ui.components.JBTextField
import com.intellij.ui.table.JBTable
import com.intellij.ui.wizard.WizardNavigationState
import com.intellij.ui.wizard.WizardStep
import kotlinx.coroutines.*
import ru.vassuv.plugin.createfromtemplate.model.Const.PROPERTY
import ru.vassuv.plugin.createfromtemplate.model.Const.SETTINGS_TEMPLATE
import ru.vassuv.plugin.createfromtemplate.model.Const.TARGET_PATH
import ru.vassuv.plugin.createfromtemplate.model.Const.VALUE
import ru.vassuv.plugin.createfromtemplate.model.Const.VALUE_COLUMN_CAN_BE_EDITED
import ru.vassuv.plugin.createfromtemplate.model.SettingsViewModel
import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.*
import javax.swing.BorderFactory.createEmptyBorder
import javax.swing.border.StrokeBorder
import javax.swing.event.TableModelEvent.UPDATE
import javax.swing.table.DefaultTableModel


@OptIn(DelicateCoroutinesApi::class)
@Suppress("UnstableApiUsage")
class MyWizardStep2(private val viewModel: SettingsViewModel) : WizardStep<TemplateSettingsWizardModel>() {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable -> throwable.printStackTrace() }
    private val job = SupervisorJob()
    private val scope = CoroutineScope(GlobalScope.coroutineContext + job + coroutineExceptionHandler)

    private val strokeBorder = StrokeBorder(BasicStroke(3f), JBColor(Color.gray, Color.DARK_GRAY))

    private var pathLabel = JLabel(TARGET_PATH)
    private var pathField = JBTextField("")
        .apply {
            maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
            border = createEmptyBorder(4, 0, 4, 0)
            isEnabled = false
        }


    private val tableColumns = arrayOf(PROPERTY, VALUE)
    private val tableModel = object : DefaultTableModel(tableColumns, 0) {}.apply {
        addTableModelListener { event ->
            if (event.type == UPDATE && event.column == 1 && event.lastRow == event.firstRow) {
                val key = viewModel.replaceableStrings.value[event.firstRow].first
                val newValue = dataVector[event.firstRow][event.column].toString()
                viewModel.changeProperty(key, newValue)
            }
        }
    }

    private var replaceableStringsTable = object : JBTable(tableModel) {
        override fun isCellEditable(row: Int, column: Int): Boolean = column != 0
    }
        .apply {
            border = strokeBorder
        }

    private var contentPanel: JPanel = JPanel()
        .apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)

            add(JPanel().apply {
                maximumSize = Dimension(Int.MAX_VALUE, 25)
                layout = FlowLayout(FlowLayout.LEFT)
                add(JLabel(SETTINGS_TEMPLATE))
            })

            add(JPanel().apply {
                maximumSize = Dimension(Int.MAX_VALUE, 25)
                layout = FlowLayout(FlowLayout.LEFT)
                border = createEmptyBorder(4, 0, 4, 0)
                add(pathLabel)
                add(pathField)

                val panel = this
                addComponentListener(object : ComponentAdapter() {
                    override fun componentResized(e: ComponentEvent) {
                        pathField.preferredSize = Dimension(panel.preferredSize.width - pathLabel.preferredSize.width, 25)
                    }
                })
            })

            add(JScrollPane(replaceableStringsTable))

            add(JPanel().apply {
                maximumSize = Dimension(Int.MAX_VALUE, 25)
                layout = FlowLayout(FlowLayout.LEFT)
                add(JLabel(VALUE_COLUMN_CAN_BE_EDITED).apply {
                    border = createEmptyBorder(0, 0, 10, 0)
                })
            })
        }

    override fun prepare(state: WizardNavigationState?): JComponent = contentPanel.apply { onInit() }

    private fun onInit() {
        scope.launch {
            viewModel.targetPath.collect {
                pathField.text = it
                pathField.updateUI()
            }
        }
        scope.launch {
            viewModel.replaceableStrings.collect { strings ->
                val vectorData = strings.map { arrayOf(it.first, it.second) }.toTypedArray()
                tableModel.setDataVector(vectorData, tableColumns)
                replaceableStringsTable.updateUI()
            }
        }
    }

    override fun onFinish(): Boolean {

        if (replaceableStringsTable.isEditing) {
            replaceableStringsTable.cellEditor.stopCellEditing()
        }
        if (replaceableStringsTable.selectedRow >= 0) {
            val key = viewModel.replaceableStrings.value[replaceableStringsTable.selectedRow].first
            val newValue = replaceableStringsTable.getValueAt(replaceableStringsTable.selectedRow, 1).toString()
            viewModel.changeProperty(key, newValue)
        }

        viewModel.generate()
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