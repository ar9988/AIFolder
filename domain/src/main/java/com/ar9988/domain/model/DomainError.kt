package com.ar9988.domain.model

/**
 * 도메인 계층이 표현하는 실패 원인.
 *
 * 도메인은 사용자에게 보여줄 문구를 만들지 않는다. 표시 문자열은 :app 이
 * [DomainError] 를 문자열 리소스로 옮기면서 만든다. 그래야 번역이 가능하다.
 *
 * Exception 을 상속해서 기존 `Result<T>` 시그니처를 그대로 쓴다.
 */
sealed class DomainError : Exception() {

    /** 이름이 비어 있음. */
    data object NameBlank : DomainError()

    /** 이름에 파일 시스템이 허용하지 않는 문자가 들어 있음. */
    data object InvalidName : DomainError()

    /** 바꾸려는 이름이 기존 이름과 같음. */
    data object SameName : DomainError()

    /** 태그 이름이 너무 짧음. */
    data object TagNameTooShort : DomainError()

    /** 같은 이름의 항목이 이미 있음. */
    data object TargetExists : DomainError()

    /** 대상 파일을 찾을 수 없음. */
    data object NotFound : DomainError()

    /** 접근 권한이 없음. */
    data object PermissionDenied : DomainError()

    /** 그 밖의 입출력 실패. */
    data object Io : DomainError()
}
