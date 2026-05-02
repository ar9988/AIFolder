package com.example.domain.usecase.files

import com.example.domain.manager.FileOpener
import javax.inject.Inject

class OpenFileUseCase @Inject constructor(
    private val fileOpener: FileOpener
) {
    operator fun invoke(path: String) {
        fileOpener.openFile(path)
    }
}