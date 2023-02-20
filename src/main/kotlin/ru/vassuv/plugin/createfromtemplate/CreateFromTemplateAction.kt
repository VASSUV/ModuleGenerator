package ru.vassuv.plugin.createfromtemplate

import ru.vassuv.plugin.createfromtemplate.dialog.TemplateSettingsDialog
import ru.vassuv.plugin.createfromtemplate.dialog.TemplateSettingsWizardModel
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.psi.PsiFileSystemItem

class CreateFromTemplateAction : AnAction() {

    override fun actionPerformed(actionEvent: AnActionEvent) {
        val project = actionEvent.project!!
        val projectPath = project.basePath!!
        val psiFileItem = actionEvent.dataContext.getData(DataKey.create<PsiFileSystemItem>("psi.Element")) ?: return
        if (!psiFileItem.isPhysical) {
            return
        }
        val path = if (psiFileItem.isDirectory) {
            psiFileItem.virtualFile.path.drop(projectPath.length) + "/"
        } else {
            psiFileItem.virtualFile.path.drop(projectPath.length).replaceAfterLast("/", "")
        }
        val dialog = TemplateSettingsDialog(TemplateSettingsWizardModel(project, path)) {

        }
        dialog.setSize(700, 500)
        dialog.pack()
        dialog.show()
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        // TODO - Here we can update our action (for example, disable it)
    }

    override fun beforeActionPerformedUpdate(e: AnActionEvent) {
        super.beforeActionPerformedUpdate(e)
        // TODO - This method calls right before 'actionPerformed'
    }
}