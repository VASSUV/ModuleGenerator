package ru.vassuv.plugin.createfromtemplate.model

import ru.vassuv.plugin.createfromtemplate.model.entity.Template
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import ru.vassuv.plugin.createfromtemplate.model.Const.JSON_NAME_FILE
import java.io.File

class TemplatesModel(
    private val project: Project,
) {
    class TemplateTreeNode (
        val value: Template,
        val children: List<TemplateTreeNode>
    )

    private val templates: List<TemplateTreeNode>

    init {
        this.templates = loadTemplateTreeNodes()

    }

    private fun loadTemplateTreeNodes(): List<TemplateTreeNode> {
        val basePath = project.basePath ?: return emptyList()
        val rootFolder = LocalFileSystem.getInstance().findFileByPath(basePath)
            ?: return listOf()
        val templatesFolder = rootFolder.children.firstOrNull { it.isDirectory && it.name == ".templates" } ?: run {
            addDemo(rootFolder.path + "/.templates")
            rootFolder.refresh(false, true)
            rootFolder.children.firstOrNull { it.isDirectory && it.name == ".templates" }
        } ?: return emptyList()
        var templates = templatesFolder.children.mapToTreeNode(0).optimize()
        if (templates.isEmpty()) {
            addDemo(templatesFolder.path)
            templatesFolder.refresh(false, true)
            templates = templatesFolder.children.mapToTreeNode(0).optimize()
        }
        return templates
    }

    private fun addDemo(path: String) {
        val demoPath = "$path/demo_module"
        File(demoPath).mkdirs()

        val readme = File("$demoPath/{{.module_name}}.md")
        readme.createNewFile()
        readme.writeText(Const.Demo.README_FILE)

        val jsonFile = File("$demoPath/ModuleGenerator.json")
        jsonFile.createNewFile()
        jsonFile.writeText(Const.Demo.JSON_FILE)

        val modulePath = "$demoPath/{{.module_name.lowercase()}}"
        File(modulePath).mkdir()

        val gradleFile = File("$modulePath/build.gradle")
        gradleFile.createNewFile()
        gradleFile.writeText(Const.Demo.GRADLE_FILE)

        val gitIgnore = File("$modulePath/.gitignore")
        gitIgnore.createNewFile()
        gitIgnore.writeText(Const.Demo.GIT_IGNORE_FILE)
        val manifestFolderPath = "$modulePath/src/main"
        File(manifestFolderPath).mkdirs()
        val androidManifest = File("$manifestFolderPath/AndroidManifest.xml")
        androidManifest.writeText(Const.Demo.ANDROID_MANIFEST_FILE)

        val sourceFolderPath = "$manifestFolderPath/kotlin/{{.module_path}}"
        File(sourceFolderPath).mkdirs()
        val repository = File("$sourceFolderPath/{{.module_name}}Repository.kt")
        repository.writeText(Const.Demo.REPOSITORY_FILE)

        val entityFolderPath = "$sourceFolderPath/entity"
        File(entityFolderPath).mkdir()
        val entity = File("$entityFolderPath/{{.module_name}}.kt")
        entity.writeText(Const.Demo.REPOSITORY_ENTITY_FILE)

        val resFolderPath = "$manifestFolderPath/res"
        File(resFolderPath).mkdir()
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