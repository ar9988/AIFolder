package com.ar9988.data.utility

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import javax.inject.Inject

class FileHashExtractor @Inject constructor(){

    fun calculateHash(file: File): String? {
        if (!file.exists() || file.isDirectory) return null

        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(8192)

            FileInputStream(file).use { fis ->
                var bytesRead: Int
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }

            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun partialHash(file: File, chunkSize: Int = 65536): String? {
        if (!file.exists() || file.isDirectory) return null

        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val fileSize = file.length()

            FileInputStream(file).use { fis ->

                val buffer = ByteArray(chunkSize)

                // 앞부분
                val headRead = fis.read(buffer)
                if (headRead > 0) {
                    digest.update(buffer, 0, headRead)
                }

                // 중간
                if (fileSize > chunkSize * 2) {
                    val middlePos = fileSize / 2
                    fis.channel.position(middlePos)

                    val midRead = fis.read(buffer)
                    if (midRead > 0) {
                        digest.update(buffer, 0, midRead)
                    }
                }

                // 끝부분
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

