package ru.vassuv.plugin.createfromtemplate.dialog

import com.intellij.ui.JBColor
import com.intellij.ui.components.fields.valueEditors.TextFieldValueEditor
import com.intellij.ui.components.fields.valueEditors.ValueEditor
import com.intellij.ui.layout.CCFlags
import ru.vassuv.plugin.createfromtemplate.model.SettingsViewModel
import com.intellij.ui.layout.LCFlags
import com.intellij.ui.layout.listCellRenderer
import com.intellij.ui.layout.panel
import com.intellij.ui.table.JBTable
import com.intellij.ui.wizard.WizardNavigationState
import com.intellij.ui.wizard.WizardStep
import com.intellij.util.ui.ListTableModel
import com.intellij.util.ui.TableViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.swing.Swing
import ru.vassuv.plugin.createfromtemplate.model.Const.PROPERTY
import ru.vassuv.plugin.createfromtemplate.model.Const.SETTINGS_TEMPLATE
import ru.vassuv.plugin.createfromtemplate.model.Const.TARGET_PATH
import ru.vassuv.plugin.createfromtemplate.model.Const.VALUE
import ru.vassuv.plugin.createfromtemplate.model.Const.VALUE_COLUMN_CAN_BE_EDITED
import java.awt.BasicStroke
import java.awt.Color
import java.util.*
import javax.swing.*
import javax.swing.border.StrokeBorder
import javax.swing.event.TableModelEvent
import javax.swing.event.TableModelEvent.UPDATE
import javax.swing.event.TableModelListener
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellEditor


class MyWizardStep2(private val viewModel: SettingsViewModel) : WizardStep<TemplateSettingsWizardModel>() {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable -> throwable.printStackTrace() }
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Swing + job + coroutineExceptionHandler)

    private val strokeBorder = StrokeBorder(BasicStroke(3f), JBColor(Color.gray, Color.DARK_GRAY))

    private var pathLabel = JLabel(TARGET_PATH)
    private var pathField = JTextField("")
        .apply { isEnabled = false }


    private val tableColumns = arrayOf(PROPERTY, VALUE)
    private val tableModel = object: DefaultTableModel(tableColumns, 0) { }.apply {
        addTableModelListener { event ->
            if(event.type == UPDATE && event.column == 1 && event.lastRow == event.firstRow) {
                val key = viewModel.replaceableStrings.value[event.firstRow].first
                val newValue = dataVector[event.firstRow][event.column].toString()
                viewModel.changeProperty(key, newValue)
            }
        }
    }

    private var replaceableStringsTable = object : JBTable(tableModel) {
        override fun isCellEditable(row: Int, column: Int): Boolean = column != 0
    }
        .apply { border = strokeBorder }

    private var contentPanel: JPanel = panel(LCFlags.fill, title = SETTINGS_TEMPLATE) {
        row(pathLabel) {
            pathField.invoke(CCFlags.growX)
        }
        row {
            JScrollPane(replaceableStringsTable).invoke(CCFlags.grow, comment = VALUE_COLUMN_CAN_BE_EDITED)
        }
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
        if(replaceableStringsTable.selectedRow >= 0) {
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