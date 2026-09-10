package com.ar9988.domain.model

/** 앱 테마 선택. SYSTEM 이면 기기 설정을 따른다. */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        fun fromName(name: String?): ThemeMode =
            entries.firstOrNull { it.name == name } ?: SYSTEM
    }
}
