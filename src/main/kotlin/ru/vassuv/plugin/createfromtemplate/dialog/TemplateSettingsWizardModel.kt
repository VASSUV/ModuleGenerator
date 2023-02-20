package ru.vassuv.plugin.createfromtemplate.dialog

import ru.vassuv.plugin.createfromtemplate.model.SettingsViewModel
import com.intellij.openapi.project.Project
import com.intellij.ui.wizard.WizardModel
import ru.vassuv.plugin.createfromtemplate.model.Const.CREATE_FROM_TEMPLATES


class TemplateSettingsWizardModel(project: Project, path: String) : WizardModel(CREATE_FROM_TEMPLATES) {

    private val viewModel: SettingsViewModel = SettingsViewModel(project, path,)
    init {
        this.add(MyWizardStep1(viewModel))
        this.add(MyWizardStep2(viewModel))
    }

    override fun isDone(): Boolean {
        viewModel.dispose()
        return super.isDone()
    }
}
