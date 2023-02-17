package com.example.demo

import com.intellij.ui.wizard.WizardNavigationState
import com.intellij.ui.wizard.WizardStep
import javax.swing.JComponent
import javax.swing.JPanel

class MyWizardStep1: WizardStep<MyWizardModel>() {

    private lateinit var contentPanel: JComponent

    override fun prepare(state: WizardNavigationState?): JComponent {
        contentPanel = JPanel()
        return contentPanel
    }
}