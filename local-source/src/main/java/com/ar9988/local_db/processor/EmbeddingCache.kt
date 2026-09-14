package com.ar9988.local_db.processor

import kotlinx.coroutines.CompletableDeferred

/** Bounded, process-local cache. Keys preserve the cased model's exact input. */
internal class EmbeddingCache(
    private val maxEntries: Int = 256,
    private val maxKeyLength: Int = 256,
) {
    init {
        require(maxEntries > 0)
        require(maxKeyLength > 0)
    }

    private val lock = Any()
    private val entries = LinkedHashMap<String, FloatArray>(maxEntries, 0.75f, true)
    private val inFlight = mutableMapOf<String, CompletableDeferred<FloatArray>>()

    suspend fun getOrCompute(text: String, compute: suspend () -> FloatArray): FloatArray {
        // Long inputs still use the original model, but do not retain large keys.
        if (text.length > maxKeyLength) return compute()

        var owner = false
        val pending = synchronized(lock) {
            entries[text]?.let { return it.copyOf() }
            inFlight[text] ?: CompletableDeferred<FloatArray>().also {
                inFlight[text] = it
                owner = true
            }
        }
        if (!owner) return pending.await().copyOf()

        try {
            val value = compute().copyOf()
            synchronized(lock) {
                // A clear during inference must not repopulate the old cache.
                if (inFlight[text] === pending) {
                    entries[text] = value
                    if (entries.size > maxEntries) {
                        val oldest = entries.entries.iterator()
                        oldest.next()
                        oldest.remove()
                    }
                    inFlight.remove(text)
                }
            }
            pending.complete(value)
            return value.copyOf()
        } catch (error: Throwable) {
            synchronized(lock) {
                if (inFlight[text] === pending) inFlight.remove(text)
            }
            pending.completeExceptionally(error)
            throw error
        }
    }

    fun clear() = synchronized(lock) {
        entries.clear()
        inFlight.clear()
    }
}
