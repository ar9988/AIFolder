package com.ar9988.domain.usecase.files

import com.ar9988.domain.manager.FileOpener
import javax.inject.Inject

class OpenFileUseCase @Inject constructor(
    private val fileOpener: FileOpener
) {
    operator fun invoke(path: String) {
        fileOpener.openFile(path)
    }
}