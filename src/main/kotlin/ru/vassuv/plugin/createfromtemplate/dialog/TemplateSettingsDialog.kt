package ru.vassuv.plugin.createfromtemplate.dialog

import com.intellij.ui.wizard.WizardDialog

class TemplateSettingsDialog(
    model: TemplateSettingsWizardModel,
    private val onFinishButtonClickedListener: (TemplateSettingsWizardModel) -> Unit
): WizardDialog<TemplateSettingsWizardModel>(true, true, model) {

    override fun onWizardGoalAchieved() {
        super.onWizardGoalAchieved()
        onFinishButtonClickedListener.invoke(myModel)
    }
} 