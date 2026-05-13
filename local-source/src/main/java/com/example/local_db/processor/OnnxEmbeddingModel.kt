package com.example.local_db.processor

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import com.example.domain.service.EmbeddingModel
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

    private val env: OrtEnvironment = OrtEnvironment.getEnvironment()
    private val mutex = Mutex()

    // 🚀 실제 사용 시점에 초기화하기 위해 Nullable로 선언
    private var session: OrtSession? = null
    private var tokenizer: WordPieceTokenizer? = null

    private suspend fun initializeIfNeeded() = mutex.withLock {
        if (session == null) {
            withContext(Dispatchers.IO) {
                val modelFile = prepareModelFile(context)

                val options = OrtSession.SessionOptions().apply {
                    setIntraOpNumThreads(2)
                }

                session = env.createSession(
                    modelFile.absolutePath,
                    options
                )

                if (tokenizer==null) {
                    tokenizer = WordPieceTokenizer(
                        context = context,
                        vocabFile = "model/vocab.txt"
                    )
                }
            }
        }
    }

    override suspend fun encode(text: String): FloatArray = withContext(Dispatchers.Default) {
        // 🚀 필요할 때만 로드
        initializeIfNeeded()

        val currentSession = session!!

        // 1. 토크나이징
        val (inputIds, attentionMask) =
            tokenizer!!.tokenize(text)
        val ids = inputIds.map { it.toLong() }.toLongArray()
        val mask = attentionMask.map { it.toLong() }.toLongArray()
        val shape = longArrayOf(1, ids.size.toLong())
        val idsBuffer = LongBuffer.wrap(ids)
        val maskBuffer = LongBuffer.wrap(mask)
        // ids와 동일한 크기의 0으로 채워진 배열 생성
        val typeIdsBuffer = LongBuffer.wrap(LongArray(ids.size) { 0L })

        OnnxTensor.createTensor(env, idsBuffer, shape).use { idsTensor ->
            OnnxTensor.createTensor(env, maskBuffer, shape).use { maskTensor ->
                OnnxTensor.createTensor(env, typeIdsBuffer, shape).use { typeIdsTensor ->

                    // 🚀 모델이 요구하는 3개의 입력을 모두 전달
                    val inputs = mapOf(
                        "input_ids" to idsTensor,
                        "attention_mask" to maskTensor,
                        "token_type_ids" to typeIdsTensor
                    )
                    // 3. 모델 추론
                    currentSession.run(inputs).use { output ->
                        // 모델에 따라 출력 이름이 다를 수 있음 (보통 "last_hidden_state" 또는 "output_0")
                        val lastHiddenState = (output[0].value as Array<Array<FloatArray>>)[0]

                        // 4. Mean Pooling: Attention Mask가 1인 토큰들의 평균 계산
                        val embeddingSize = lastHiddenState[0].size
                        val meanEmbedding = FloatArray(embeddingSize)
                        var validTokenCount = 0f

                        lastHiddenState.forEachIndexed { index, tokenVector ->
                            if (mask[index] == 1L) {
                                for (i in 0 until embeddingSize) {
                                    meanEmbedding[i] += tokenVector[i]
                                }
                                validTokenCount += 1f
                            }
                        }

                        if (validTokenCount > 0) {
                            for (i in 0 until embeddingSize) {
                                meanEmbedding[i] /= validTokenCount
                            }
                        }

                        // 5. L2 Normalization (유사도 계산을 위해 정규화)
                        val sumSquared = meanEmbedding.sumOf { (it * it).toDouble() }
                        val norm = sqrt(sumSquared).toFloat()

                        // 결과 반환 (FloatArray 생성)
                        if (norm > 0) {
                            FloatArray(embeddingSize) { i -> meanEmbedding[i] / norm }
                        } else {
                            meanEmbedding
                        }
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

    private fun prepareModelFile(context: Context): File {
        val file = File(context.filesDir, "model_qint8_arm64.onnx")

        if (!file.exists()) {
            context.assets.open("model/model_qint8_arm64.onnx").use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }

        return file
    }
}