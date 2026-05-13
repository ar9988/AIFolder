package com.example.local_db.processor

import android.content.Context

class WordPieceTokenizer(context: Context, vocabFile: String = "model/vocab.txt") {
    private val vocab: Map<String, Int> = context.assets.open(vocabFile)
        .bufferedReader()
        .readLines()
        .mapIndexed { idx, token -> token to idx }
        .toMap()
    private val unkToken = "[UNK]"
    private val maxInputCharsPerWord = 100
    private val maxSeqLen = 128

    fun tokenize(text: String): Pair<IntArray, IntArray> {
        val tokens = mutableListOf("[CLS]")

        text.trim()
            .split(Regex("\\s+"))
            .forEach { word ->
                tokens.addAll(wordPiece(word))
            }

        tokens.add("[SEP]")

        val inputIds = IntArray(maxSeqLen)
        val attentionMask = IntArray(maxSeqLen)

        tokens.take(maxSeqLen).forEachIndexed { i, token ->
            inputIds[i] = vocab[token] ?: vocab[unkToken] ?: 0
            attentionMask[i] = 1
        }

        return Pair(inputIds, attentionMask)
    }

    private fun wordPiece(word: String): List<String> {
        if (word.length > maxInputCharsPerWord) return listOf(unkToken)
        val result = mutableListOf<String>()
        var start = 0
        while (start < word.length) {
            var end = word.length
            var found: String? = null
            while (start < end) {
                val substr = (if (start > 0) "##" else "") + word.substring(start, end)
                if (vocab.containsKey(substr)) { found = substr; break }
                end--
            }
            if (found == null) return listOf(unkToken)
            result.add(found)
            start = end
        }
        return result
    }
}