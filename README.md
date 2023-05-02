# ModuleGenerator

[Intellij Idea Plugin](https://plugins.jetbrains.com/plugin/21100-segment-generator) for generating modules from templates

### To create a new template:

- open the folder ".template" in the root of the project
- add a new folder with "ModuleGenerator.json" file with properties

**Important**, Existing files will be overwritten with files from the template.

_(example: /.templates/Demo/ModuleGenerator.json)_

- add files and folders for your template to a new folder

_(examples: /.tempalates/Demo/MyProject.txt , /.tempalates/Demo/MyFolder/MyFile.txt )_

### To activate the function:

- right click on target folder
- click on "New"
- click on "Generate from TEMPLATE"
- select template
- set properties

### To change properties in a template:

- replace all necessary substrings with the property name specified in json and wrapped in double curly braces

### Property examples:

- {{ .<b>project_name</b> }}
- {{ .project_name<b>.uppercase()</b> }} or {{ .project_name<b>|upper</b> }}
- {{ .project_name<b>.lowercase()</b> }} or {{ .project_name<b>|lower</b> }}
- {{ .app_package<b>.replace('.', '/')</b> }}
- {{ .app_package<b>.replace("com.example", "com.mycompany"</b>) }}

### ModuleGenerator.json example:
```json
{ 
  "screen_name": "Register",
  "feature_name": "Login",
  "app_package": "com.example.app",
  "screen_package": "{{.app_package}}.{{.feature_name|lower}}.ui.{{.screen_name|lower}}"
}
```

### Template example:

You can see sample templates in the /.templates folder in the repository

**\* You should have your templates in the /.templates folder at the root of your project**

