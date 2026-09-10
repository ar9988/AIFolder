package com.ar9988.data.mapper

import com.ar9988.domain.model.Resource
import java.io.File
import java.text.Normalizer

/**
 * 실제 파일에서 Resource 를 만든다.
 *
 * 이름과 경로는 반드시 NFC 로 정규화한다. 안드로이드 파일시스템은 한글을 분해형(NFD)으로
 * 돌려주는 경우가 있고, 스캐너는 정규화한 경로를 키로 기존 항목을 찾는다.
 * 여기서 정규화하지 않으면 눈에 같아 보이는 경로가 다른 문자열이 되어,
 * 다음 스캔이 같은 파일을 새 항목으로 한 번 더 넣는다.
 *
 * [mimeType] 을 비워 두면 카테고리 탐색(이미지/동영상/오디오)에서 이 파일이 보이지 않는다.
 * 그 조회가 mimeType 패턴으로 걸러내기 때문이다.
 */
fun File.toResource(
    parentId: Long?,
    mimeType: String? = null
): Resource {

    return Resource(
        id = 0L,
        name = Normalizer.normalize(name, Normalizer.Form.NFC),
        path = Normalizer.normalize(absolutePath, Normalizer.Form.NFC),
        isDirectory = isDirectory,
        size = if (isFile) length() else 0L,
        fileHash = null,
        lastModified = lastModified(),
        tags = emptyList(),
        parentId = parentId,
        mimeType = mimeType,
        extension = extension.ifBlank { null }
    )
}
