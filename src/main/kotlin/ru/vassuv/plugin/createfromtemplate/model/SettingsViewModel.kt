package ru.vassuv.plugin.createfromtemplate.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.project.Project
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ru.vassuv.plugin.createfromtemplate.model.Const.JSON_NAME_FILE
import ru.vassuv.plugin.createfromtemplate.model.entity.Template
import java.io.File

@OptIn(DelicateCoroutinesApi::class)
class SettingsViewModel(
    private val project: Project,
    private val path: String,
) {
    private val templatesModel = TemplatesModel(project)

    private val _targetPath = MutableStateFlow(path)
    val targetPath = _targetPath.asStateFlow()

    private val _templates = MutableStateFlow(listOf<Template>())
    val templates = _templates.asStateFlow()

    private val _selectedTemplate = MutableStateFlow<Template?>(null)
    val selectedTemplate = _selectedTemplate.asStateFlow()

    private val _replaceableStrings = MutableStateFlow<List<Pair<String, String>>>(listOf())
    val replaceableStrings = _replaceableStrings.asStateFlow()


    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }
    private val job = SupervisorJob()
    private val scope = CoroutineScope(GlobalScope.coroutineContext + job + coroutineExceptionHandler)

    init {
        scope.launch {
            _templates.emit(templatesModel.getTemplateTree())
        }
    }

    fun dispose() {
        scope.coroutineContext.cancelChildren()
        job.cancel()
    }

    fun changeSelectedTemplate(selectedValue: Template?) {
        if (selectedValue?.isTemplate == true) {
            _selectedTemplate.tryEmit(selectedValue)
            if (_selectedTemplate.value?.isTemplate == true) {
                updateReplaceableStrings()
            }
        } else {
            _selectedTemplate.tryEmit(null)
        }
    }

    private fun updateReplaceableStrings() {
        scope.launch {
            val gson = Gson()
            val mapType = object : TypeToken<Map<String, String>>() {}.type
            val jsonText = File(selectedTemplate.value?.path + "/$JSON_NAME_FILE.json").readText()
            val rules: Map<String, String> = gson.fromJson<Map<String, String>?>(jsonText, mapType)
            _replaceableStrings.emit(rules.toList())
        }
    }

    fun generate() {
        val projectPath = project.basePath ?: return
        val templatePath = selectedTemplate.value?.path ?: return
        Generator(
            project = project,
            projectPath = projectPath,
            targetPath = targetPath.value,
            templatePath = templatePath,
            properties = _replaceableStrings.value.toMap()
        ).generate()

    }

    fun changeProperty(key: String, newValue: String) {
        _replaceableStrings.tryEmit(_replaceableStrings.value.map { if (it.first == key) Pair(key, newValue) else it })
    }
}