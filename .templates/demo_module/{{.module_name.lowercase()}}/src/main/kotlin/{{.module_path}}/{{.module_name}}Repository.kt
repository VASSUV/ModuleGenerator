package {{.module_package}}

import {{.module_package}}.entity.{{.module_name}}

interface {{.module_name}}Repository {
  fun get{{.module_name}}(): {{.module_name}} 
}
