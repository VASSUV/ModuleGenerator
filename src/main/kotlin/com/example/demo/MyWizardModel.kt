package com.example.demo

import com.intellij.ui.wizard.WizardModel

class MyWizardModel: WizardModel("Title for my wizard") {

    init {
        this.add(MyWizardStep1())
        this.add(MyWizardStep1())
        this.add(MyWizardStep1())
    }

}