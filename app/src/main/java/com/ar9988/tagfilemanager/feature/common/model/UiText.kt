package com.ar9988.tagfilemanager.feature.common.model

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * 아직 문자열이 되지 않은 문구.
 *
 * ViewModel 과 상태는 Context 를 갖지 않으므로 표시 문자열을 직접 만들 수 없다.
 * 대신 "무엇을 보여줄지"만 들고 다니다가, 화면에 그리는 순간 [asString] 으로 해석한다.
 * 이렇게 해야 문구가 res/values-* 를 따라 번역된다.
 */
sealed interface UiText {

    data class Res(
        @param:StringRes val id: Int,
        val args: List<Any> = emptyList()
    ) : UiText

    data class Plural(
        @param:PluralsRes val id: Int,
        val count: Int,
        val args: List<Any> = emptyList()
    ) : UiText

    /** 파일명처럼 이미 확정된 문자열. */
    data class Raw(val value: String) : UiText

    companion object {
        fun res(@StringRes id: Int, vararg args: Any): UiText = Res(id, args.toList())

        fun plural(@PluralsRes id: Int, count: Int, vararg args: Any): UiText =
            Plural(id, count, if (args.isEmpty()) listOf(count) else args.toList())
    }
}

fun UiText.resolve(context: Context): String = when (this) {
    is UiText.Res -> context.getString(id, *args.resolved(context))
    is UiText.Plural -> context.resources.getQuantityString(id, count, *args.resolved(context))
    is UiText.Raw -> value
}

/** 인자로 UiText 를 다시 넘길 수 있게 한다. "복사 실패: <사유>" 처럼 문장 안에 문장이 들어갈 때 쓴다. */
private fun List<Any>.resolved(context: Context): Array<Any> =
    map { if (it is UiText) it.resolve(context) else it }.toTypedArray()

@Composable
fun UiText.asString(): String = resolve(LocalContext.current)
