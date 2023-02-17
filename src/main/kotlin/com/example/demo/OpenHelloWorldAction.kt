package com.example.demo

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class OpenHelloWorldAction : AnAction() {

    override fun actionPerformed(actionEvent: AnActionEvent) {
        val project = actionEvent.project
        val dialog = SettingUiDialog()
        dialog.isVisible = true
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        // TODO - Here we can update our action (for example, disable it)
    }

    override fun beforeActionPerformedUpdate(e: AnActionEvent) {
        super.beforeActionPerformedUpdate(e)
        // TODO - This method calls right before 'actionPerformed'
    }

} /**/