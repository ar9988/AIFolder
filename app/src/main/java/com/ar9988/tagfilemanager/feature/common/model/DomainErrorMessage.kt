package com.ar9988.tagfilemanager.feature.common.model

import com.ar9988.domain.model.DomainError
import com.ar9988.tagfilemanager.R

/**
 * 도메인이 돌려준 실패 원인을 화면에 보여줄 문구로 옮긴다.
 *
 * 이 매핑이 :app 에 있어야 문구가 번역되고, :domain 은 언어를 모르는 채로 남는다.
 */
fun Throwable?.toUiText(): UiText = when (this) {
    is DomainError.NameBlank -> UiText.res(R.string.error_name_blank)
    is DomainError.InvalidName -> UiText.res(R.string.error_invalid_name)
    is DomainError.SameName -> UiText.res(R.string.error_same_name)
    is DomainError.TagNameTooShort -> UiText.res(R.string.error_tag_name_too_short)
    is DomainError.TargetExists -> UiText.res(R.string.error_target_exists)
    is DomainError.NotFound -> UiText.res(R.string.error_not_found)
    is DomainError.PermissionDenied -> UiText.res(R.string.error_permission_denied)
    is DomainError.Io -> UiText.res(R.string.error_io)
    else -> UiText.res(R.string.error_unknown)
}
