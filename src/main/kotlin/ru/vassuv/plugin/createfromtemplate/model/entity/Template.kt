package ru.vassuv.plugin.createfromtemplate.model.entity

data class Template(
    val name: String,
    val path: String,
    val level: Int,
    val isTemplate: Boolean
) {

    val prefix : String
        get() = (0 until level).joinToString(separator = "") { "  "}
}