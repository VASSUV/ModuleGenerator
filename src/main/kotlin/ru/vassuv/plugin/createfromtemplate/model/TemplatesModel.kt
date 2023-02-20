package ru.vassuv.plugin.createfromtemplate.model

import ru.vassuv.plugin.createfromtemplate.model.entity.Template
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import ru.vassuv.plugin.createfromtemplate.model.Const.JSON_NAME_FILE

class TemplatesModel(
    private val project: Project,
) {
    class TemplateTreeNode (
        val value: Template,
        val children: List<TemplateTreeNode>
    )

    private val templates: List<TemplateTreeNode>

    init {
        val rootFolder = project.workspaceFile!!.parent.parent
        val templatesFolder = rootFolder.children.firstOrNull { it.isDirectory && it.name == ".templates"  }
        templates = (templatesFolder?.children?.mapToTreeNode(0) ?: arrayListOf()).optimize()
    }

    fun getTemplateTree(): List<Template> {
        return templates.map { it.mapToTreeList() }.flatten()
    }



    private fun Array<VirtualFile>.mapToTreeNode(level: Int): List<TemplateTreeNode> {
        return map { file ->
            val template = Template(file.name, file.path, level, file.children.isNotEmpty())
            TemplateTreeNode(template, file.children.mapToTreeNode(level + 1))
        }
    }

    private fun TemplateTreeNode.mapToTreeList(): List<Template> {
        return arrayListOf(this.value) + this.children.map { it.mapToTreeList() }.flatten()
    }

    private fun List<TemplateTreeNode>.optimize(): List<TemplateTreeNode> {
        return this.mapNotNull { it.optimizedTreeNode() }
    }

    private fun TemplateTreeNode.optimizedTreeNode(): TemplateTreeNode? {
        if (children.firstOrNull { it.value.name == "$JSON_NAME_FILE.json" } == null) {
            val newChildren = children.optimize()
            if (newChildren.isEmpty()) {
                return null
            }
            return TemplateTreeNode(value.copy(isTemplate = false), newChildren)
        } else {
            return TemplateTreeNode(value.copy(isTemplate = true), arrayListOf())
        }
    }

}