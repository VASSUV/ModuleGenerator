package ru.vassuv.plugin.createfromtemplate.model

import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.file.PsiDirectoryFactory
import ru.vassuv.plugin.createfromtemplate.model.Const.JSON_NAME_FILE
import java.io.File

private fun String.removeLast(value: Char) = takeIf { it.lastOrNull() == value }?.dropLast(1) ?: this
private fun String.removeFirst(value: Char) = takeIf { it.firstOrNull() == value }?.drop(1) ?: this

class Generator(
    private val project: Project,
    projectPath: String,
    targetPath: String,
    private val templatePath: String,
    private val properties: Map<String, String>,
) {
    private val projectPath = projectPath.removeLast('/')
    private val targetPath = targetPath.removeLast('/').removeFirst('/')

    fun generate() {
        val targetPath = "$projectPath/$targetPath"

        val createDirectoryIfMissing = VfsUtil.createDirectoryIfMissing(targetPath)!!
        PsiDirectoryFactory.getInstance(project).createDirectory(createDirectoryIfMissing)

        File(templatePath).list()?.forEach { fileName ->

            if (fileName != null && fileName != "$JSON_NAME_FILE.json") {
                copyWithReplace(
                    targetPath = targetPath,
                    templatePath = templatePath,
                    fileName = fileName
                )
            }
        }
    }

    private fun copyWithReplace(targetPath: String, templatePath: String, fileName: String) {
        val templateFilePath = "$templatePath/$fileName"
        val file = File(templateFilePath)
        val newFileName = fileName.withReplacedProperties()
        val newTargetPath = "$targetPath/$newFileName"
        if (file.isDirectory) {

            val createDirectoryIfMissing = VfsUtil.createDirectoryIfMissing(newTargetPath)!!
            PsiDirectoryFactory.getInstance(project).createDirectory(createDirectoryIfMissing)

            println("Create new directory: $newTargetPath")
            file.list()?.forEach { name ->
                copyWithReplace(newTargetPath, templateFilePath, name)
            }
        } else {
            val newFile = File(newTargetPath)
            println("Create new file: $newTargetPath")
            newFile.createNewFile()
            val text = file.readText().withReplacedProperties()
            newFile.writeText(text)

            LocalFileSystem.getInstance().refreshAndFindFileByIoFile(newFile)
        }
    }

    private fun String.invokeFunction(function: String): String? {
        return if(function.startsWith("replace")) {
            val params = function.replaceBefore('(', "").split("[,()]".toRegex()).mapNotNull { it.trim().takeIf { it.isNotEmpty() } }
            when {
                params.size != 2-> null
                params.first().isChar() && params.last().isChar() ->
                    replace(params.first()[1], params.last()[1])
                params.first().isString() && params.last().isString() ->
                    replace(
                        params.first().substring(1, params.first().length - 1),
                        params.last().substring(1, params.last().length - 1),
                    )
                else -> null
            }
        } else if(function == "uppercase()" || function == "upper") {
            this.uppercase()
        } else if(function == "lowercase()" || function == "lower") {
            this.lowercase()
        } else {
            null
        }
    }

    private fun String.propertyToValue(): String {

        fun invokeFunction(parametr: String, delimetr: Char): String? {
            if(!parametr.contains(delimetr)) return null
            val dotIndex = parametr.indexOf(delimetr)
            val param = parametr.substring(0, dotIndex)
            val function = parametr.substring(dotIndex + 1, parametr.length)
            return properties[param]?.withReplacedProperties()?.invokeFunction(function)
        }

        return this.trim().takeIf { this.startsWith('.') }?.removePrefix(".")?.let { parametr ->
            properties[parametr]?.withReplacedProperties()     // {{ .param }}
                ?: invokeFunction(parametr, '.')       // {{ .param.function() }}
                ?: invokeFunction(parametr, '|')       // {{ .param|function }}
                ?: "{{$this}}"                                 // {{ ignore }}
        } ?: "{{$this}}"
    }

    private fun String.withReplacedProperties(): String {

        var lastIndex = 0
        var startPropertyIndex = indexOf("{{", 0)
        if(startPropertyIndex < 0) { return this }
        var endPropertyIndex = indexOf("}}", startPropertyIndex)
        if(endPropertyIndex < 0) { return this }
        val builder = StringBuilder()
        while (endPropertyIndex > 0) {
            builder.append(this.substring(lastIndex, startPropertyIndex))
            val value = substring(startPropertyIndex + 2, endPropertyIndex).propertyToValue()
            builder.append(value)
            lastIndex = endPropertyIndex + 2
            startPropertyIndex = indexOf("{{", lastIndex)

            if(startPropertyIndex < 0) {
                builder.append(this.substring(lastIndex, this.length ))
                break
            }
            endPropertyIndex = indexOf("}}", startPropertyIndex)

            if(endPropertyIndex < 0) {
                builder.append(this.substring(lastIndex, this.length ))
                break
            }
        }
        return builder.toString()
    }

    private fun String.isChar(): Boolean = length == 3 && first() == '\'' && last() == '\''

    private fun String.isString(): Boolean = first() == '\"' && last() == '\"'
}
