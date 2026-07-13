package com.kushagra.reconx.ui.navigation

/**
 * Destinations.kt
 * =================
 * Every navigable route in the app, as typed constants, to avoid
 * stringly-typed route bugs scattered across composables.
 */
object Destinations {
    const val LOGIN = "login"

    // Bottom nav top-level destinations
    const val DASHBOARD = "dashboard"
    const val TOOLS = "tools"
    const val PROJECTS = "projects"
    const val NOTES = "notes"
    const val SETTINGS = "settings"

    // Tools sub-screens

    const val DORK_BUILDER = "dork_builder/{engine}"
    fun dorkBuilder(engine: String) = "dork_builder/$engine"

    const val DOMAIN_INTEL = "domain_intel"
    const val WEBSITE_SECURITY = "website_security"
    const val IP_INTEL = "ip_intel"
    const val HASH_TOOLS = "hash_tools"
    const val PASSWORD_TOOLS = "password_tools"
    const val ENCODING_TOOLS = "encoding_tools"
    const val REGEX_LAB = "regex_lab"
    const val CVE_LOOKUP = "cve_lookup"
    const val CHECKLISTS = "checklists"
    const val SEARCH_HISTORY = "search_history"

    // Projects
    const val PROJECT_DETAIL = "project_detail/{projectId}"
    fun projectDetail(projectId: Long) = "project_detail/$projectId"

    // Notes
    const val NOTE_EDITOR = "note_editor?noteId={noteId}&projectId={projectId}"
    fun noteEditor(noteId: Long? = null, projectId: Long? = null) =
        "note_editor?noteId=${noteId ?: -1}&projectId=${projectId ?: -1}"

    // Reports
    const val REPORTS = "reports"

    // Settings sub-screens
    const val ABOUT_APP = "about_app"
    const val ABOUT_DEVELOPER = "about_developer"
    const val GLOBAL_SEARCH = "global_search"
}
