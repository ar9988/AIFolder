package com.ar9988.local_db.processor

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test

class EmbeddingCacheTest {
    @Test
    fun `repeated input reuses inference without changing the vector`() = runBlocking<Unit> {
        val cache = EmbeddingCache()
        var calls = 0
        repeat(10) {
            val actual = cache.getOrCompute("영수증") {
                calls++
                floatArrayOf(0.1f, 0.2f, 0.3f)
            }
            assertArrayEquals(floatArrayOf(0.1f, 0.2f, 0.3f), actual, 0f)
        }
        assertEquals(1, calls)
    }

    @Test
    fun `default capacity evicts the least recently used of 256 entries`() = runBlocking<Unit> {
        val cache = EmbeddingCache()
        val calls = mutableMapOf<String, Int>()
        suspend fun load(key: String) = cache.getOrCompute(key) {
            calls[key] = (calls[key] ?: 0) + 1
            floatArrayOf(1f)
        }
        repeat(256) { load("tag$it") }
        load("tag0") // A cache hit refreshes recency.
        load("tag256")
        load("tag0")
        load("tag1")
        assertEquals(1, calls["tag0"])
        assertEquals(2, calls["tag1"])
    }

    @Test
    fun `case and compound inputs remain distinct`() = runBlocking<Unit> {
        val cache = EmbeddingCache()
        var calls = 0
        val keys = listOf("출장", "영수증", "출장 영수증", "Receipt", "receipt")
        repeat(2) {
            keys.forEach { key -> cache.getOrCompute(key) { calls++; floatArrayOf(1f) } }
        }
        assertEquals(keys.size, calls)
    }

    @Test
    fun `neither the producer nor consumers can mutate cached vectors`() = runBlocking<Unit> {
        val cache = EmbeddingCache()
        val original = floatArrayOf(1f, 2f)
        val first = cache.getOrCompute("receipt") { original }
        original[0] = 99f
        first[1] = 99f
        val second = cache.getOrCompute("receipt") { error("Unexpected inference") }
        assertArrayEquals(floatArrayOf(1f, 2f), second, 0f)
        second[0] = 88f
        assertArrayEquals(floatArrayOf(1f, 2f), cache.getOrCompute("receipt") {
            error("Unexpected inference")
        }, 0f)
        assertNotSame(first, second)
    }

    @Test
    fun `concurrent requests for the same input share one inference`() = runBlocking<Unit> {
        val cache = EmbeddingCache()
        val release = CompletableDeferred<Unit>()
        var calls = 0
        val requests = List(20) {
            async(start = CoroutineStart.UNDISPATCHED) {
                cache.getOrCompute("receipt") {
                    calls++
                    release.await()
                    floatArrayOf(1f)
                }
            }
        }
        assertEquals(1, calls)
        release.complete(Unit)
        val results = requests.awaitAll()
        results.forEach { assertArrayEquals(floatArrayOf(1f), it, 0f) }
        assertNotSame(results[0], results[1])
    }

    @Test
    fun `another word and cache hits are not blocked by a pending inference`() = runBlocking<Unit> {
        val cache = EmbeddingCache()
        cache.getOrCompute("cached") { floatArrayOf(1f) }
        val release = CompletableDeferred<Unit>()
        val pending = async(start = CoroutineStart.UNDISPATCHED) {
            cache.getOrCompute("slow") { release.await(); floatArrayOf(2f) }
        }
        assertArrayEquals(floatArrayOf(1f), cache.getOrCompute("cached") { error("Cache miss") }, 0f)
        assertArrayEquals(floatArrayOf(3f), cache.getOrCompute("other") { floatArrayOf(3f) }, 0f)
        release.complete(Unit)
        pending.await()
    }

    @Test
    fun `failed inference reaches waiters and can be retried`() = runBlocking<Unit> {
        val cache = EmbeddingCache()
        val release = CompletableDeferred<Unit>()
        val requests = List(2) {
            async(start = CoroutineStart.UNDISPATCHED) {
                runCatching {
                    cache.getOrCompute("receipt") { release.await(); error("Inference failed") }
                }
            }
        }
        release.complete(Unit)
        requests.awaitAll().forEach { assertEquals("Inference failed", it.exceptionOrNull()?.message) }
        assertArrayEquals(floatArrayOf(4f), cache.getOrCompute("receipt") { floatArrayOf(4f) }, 0f)
    }

    @Test
    fun `cancelled inference does not poison a later request`() = runBlocking<Unit> {
        val cache = EmbeddingCache()
        val pending = async(start = CoroutineStart.UNDISPATCHED) {
            cache.getOrCompute("receipt") { CompletableDeferred<Unit>().await(); floatArrayOf(1f) }
        }
        pending.cancel()
        pending.join()
        assertTrue(pending.isCancelled)
        assertArrayEquals(floatArrayOf(2f), cache.getOrCompute("receipt") { floatArrayOf(2f) }, 0f)
    }

    @Test
    fun `clear discards entries and prevents old inference from replacing new data`() = runBlocking<Unit> {
        val cache = EmbeddingCache()
        cache.getOrCompute("cached") { floatArrayOf(1f) }
        val release = CompletableDeferred<Unit>()
        val old = async(start = CoroutineStart.UNDISPATCHED) {
            cache.getOrCompute("receipt") { release.await(); floatArrayOf(2f) }
        }
        cache.clear()
        assertArrayEquals(floatArrayOf(3f), cache.getOrCompute("cached") { floatArrayOf(3f) }, 0f)
        cache.getOrCompute("receipt") { floatArrayOf(4f) }
        release.complete(Unit)
        old.await()
        assertArrayEquals(floatArrayOf(4f), cache.getOrCompute("receipt") { error("Unexpected inference") }, 0f)
    }

    @Test
    fun `long input is computed normally but not retained`() = runBlocking<Unit> {
        val cache = EmbeddingCache()
        var calls = 0
        repeat(2) {
            cache.getOrCompute("a".repeat(256)) { calls++; floatArrayOf(1f) }
            cache.getOrCompute("a".repeat(257)) { calls++; floatArrayOf(2f) }
        }
        assertEquals(3, calls)
    }
}
