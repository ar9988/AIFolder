package com.ar9988.tagfilemanager.feature.file.model

/**
 * 선택한 파일들에 대해 한 태그가 걸린 정도.
 *
 * ALL  = 선택한 파일 전부에 걸려 있음
 * SOME = 일부에만 걸려 있음
 * NONE = 아무 파일에도 걸려 있지 않음(= 떼겠다는 뜻)
 */
enum class TagSelectionState { ALL, SOME, NONE }
