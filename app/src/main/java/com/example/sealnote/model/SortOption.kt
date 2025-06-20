// path: app/src/main/java/com/example/sealnote/util/SortOption.kt
package com.example.sealnote.util

enum class SortOption(val displayName: String) {
    BY_DATE_DESC("Sort by Date (Newest)"),
    BY_DATE_ASC("Sort by Date (Oldest)"),
    BY_TITLE_ASC("Sort by Title (A-Z)"),
    BY_TITLE_DESC("Sort by Title (Z-A)")
}