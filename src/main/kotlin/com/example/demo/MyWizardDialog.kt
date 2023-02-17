package com.example.demo

import com.intellij.ui.wizard.WizardDialog

class MyWizardDialog(
    model: MyWizardModel,
    private val onFinishButtonClickedListener: (MyWizardModel) -> Unit 
): WizardDialog<MyWizardModel>(true, true, model) {

    override fun onWizardGoalAchieved() {
        super.onWizardGoalAchieved()
        onFinishButtonClickedListener.invoke(myModel)
    }
} 