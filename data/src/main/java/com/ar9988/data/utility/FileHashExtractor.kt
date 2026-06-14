package com.ar9988.data.utility

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import javax.inject.Inject

class FileHashExtractor @Inject constructor(){

    fun calculatePartialHash(file: File, chunkSize: Int = 65536): String? {
        if (!file.exists() || file.isDirectory) return null

        return try {
            val digest = MessageDigest.getInstance("MD5")
            val fileSize = file.length()

            FileInputStream(file).use { fis ->
                val buffer = ByteArray(chunkSize)

                val headRead = fis.read(buffer)
                if (headRead > 0) {
                    digest.update(buffer, 0, headRead)
                }

                if (fileSize > chunkSize * 2) {
                    val middlePos = fileSize / 2
                    fis.channel.position(middlePos)

                    val midRead = fis.read(buffer)
                    if (midRead > 0) {
                        digest.update(buffer, 0, midRead)
                    }
                }

                if (fileSize > chunkSize) {
                    val tailPos = fileSize - chunkSize
                    fis.channel.position(tailPos)

                    val tailRead = fis.read(buffer)
                    if (tailRead > 0) {
                        digest.update(buffer, 0, tailRead)
                    }
                }
            }

            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            null
        }
    }

}

