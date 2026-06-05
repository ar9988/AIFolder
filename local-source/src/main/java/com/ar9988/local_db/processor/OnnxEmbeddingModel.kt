package com.ar9988.local_db.processor

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import com.ar9988.domain.service.EmbeddingModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.LongBuffer
import javax.inject.Inject
import kotlin.io.use
import kotlin.math.sqrt

class OnnxEmbeddingModel @Inject constructor(
    @ApplicationContext private val context: Context
) : EmbeddingModel {

    private val env: OrtEnvironment =
        OrtEnvironment.getEnvironment()

    private val mutex = Mutex()

    private var session: OrtSession? = null

    private var tokenizer: WordPieceTokenizer? = null

    private suspend fun initializeIfNeeded() =
        mutex.withLock {

            if (session == null) {

                withContext(Dispatchers.IO) {

                    val modelFile =
                        prepareModelFile(context)

                    val options =
                        OrtSession.SessionOptions().apply {
                            setIntraOpNumThreads(2)
                        }

                    session =
                        env.createSession(
                            modelFile.absolutePath,
                            options
                        )

                    if (tokenizer == null) {
                        tokenizer =
                            WordPieceTokenizer(
                                context = context,
                                vocabFile = "model/vocab.txt"
                            )
                    }
                }
            }
        }

    override suspend fun encode(
        text: String
    ): FloatArray = withContext(Dispatchers.Default) {

        initializeIfNeeded()

        val currentSession = session!!

        // 1. tokenize
        val (inputIds, attentionMask) =
            tokenizer!!.tokenize(text)

        val ids =
            inputIds.map { it.toLong() }
                .toLongArray()

        val mask =
            attentionMask.map { it.toLong() }
                .toLongArray()

        val shape =
            longArrayOf(
                1,
                ids.size.toLong()
            )

        val idsBuffer =
            LongBuffer.wrap(ids)

        val maskBuffer =
            LongBuffer.wrap(mask)

        // 2. tensor 생성
        OnnxTensor.createTensor(
            env,
            idsBuffer,
            shape
        ).use { idsTensor ->

            OnnxTensor.createTensor(
                env,
                maskBuffer,
                shape
            ).use { maskTensor ->

                val inputs = mapOf(
                    "input_ids" to idsTensor,
                    "attention_mask" to maskTensor
                )

                // distiluse-base-multilingual-cased-v1
                // => mean pooling 필요
                currentSession.run(inputs).use { output ->

                    val lastHiddenState =
                        (output[0].value as Array<Array<FloatArray>>)[0]

                    val embeddingSize =
                        lastHiddenState[0].size

                    val pooledEmbedding =
                        FloatArray(embeddingSize)

                    var validTokenCount = 0f

                    // Mean Pooling
                    lastHiddenState.forEachIndexed { index, tokenVector ->

                        if (mask[index] == 1L) {

                            for (i in 0 until embeddingSize) {
                                pooledEmbedding[i] += tokenVector[i]
                            }

                            validTokenCount += 1f
                        }
                    }

                    // 평균 계산
                    if (validTokenCount > 0f) {

                        for (i in 0 until embeddingSize) {
                            pooledEmbedding[i] /= validTokenCount
                        }
                    }

                    // L2 Normalize
                    val sumSquared =
                        pooledEmbedding.sumOf {
                            (it * it).toDouble()
                        }

                    val norm =
                        sqrt(sumSquared).toFloat()

                    return@use if (norm > 0f) {

                        FloatArray(pooledEmbedding.size) { i ->
                            pooledEmbedding[i] / norm
                        }

                    } else {
                        pooledEmbedding
                    }
                }
            }
        }
    }

    fun closeSession() {

        session?.close()

        session = null

        tokenizer = null
    }

    private fun prepareModelFile(
        context: Context
    ): File {

        val file =
            File(
                context.filesDir,
                "model_qint8_arm64.onnx"
            )

        if (!file.exists()) {

            context.assets
                .open("model/model_qint8_arm64.onnx")
                .use { input ->

                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
        }

        return file
    }
}