package ru.vassuv.plugin.createfromtemplate.model

object Const {

    const val JSON_NAME_FILE = "ModuleGenerator"

    const val CREATE_FROM_TEMPLATES = "Generate from templates"
    const val CHOOSE_TEMPLATE = "Choose template"
    const val TARGET_PATH = "Target path"
    const val SETTINGS_TEMPLATE = "Template Settings"
    const val VALUE_COLUMN_CAN_BE_EDITED = "* Value column can be edited"
    const val PROPERTY = "Property"
    const val VALUE = "Value"

    object Demo {
        val JSON_FILE: String get() = """
            {
               "module_name": "Sample",
               "app_package": "com.example.app",
               "module_package": "{{.app_package}}.{{.module_name.lowercase()}}",
               "module_path": "{{.module_package.replace('.', '/')}}"
            }
        """.trimIndent()
        val GRADLE_FILE: String get() = """
            apply plugin: 'com.android.library'
            apply plugin: 'kotlin-android'
            apply plugin: 'kotlin-kapt'
            
            android {
              namespace = "{{.module_package}}"
            }
            
            dependencies {
            }
        """.trimIndent()
        val GIT_IGNORE_FILE: String get() = """
            /build
        """.trimIndent()
        val README_FILE: String get() = """
            There will be information about the module "{{.module_name}}"
        """.trimIndent()
        val ANDROID_MANIFEST_FILE: String get() = """
            <manifest />
        """.trimIndent()
        val REPOSITORY_FILE: String get() = """
            package {{.module_package}}
    
            import {{.module_package}}.entity.{{.module_name}}
    
            interface {{.module_name}}Repository {
              fun get{{.module_name}}(): {{.module_name}} 
            }

        """.trimIndent()
        val REPOSITORY_ENTITY_FILE: String get() = """
            package {{.module_package}}.entity
            
            class {{.module_name}} {
               val id: String
            }
        """.trimIndent()
    }
}