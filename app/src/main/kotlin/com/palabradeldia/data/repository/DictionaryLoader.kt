package com.palabradeldia.data.repository

import android.content.Context
import com.palabradeldia.domain.model.Definition
import com.palabradeldia.domain.model.Word
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

// Loads assets/dictionary.json once and keeps it in memory.
// Concurrent callers block on the mutex until the first load completes.
@Singleton
class DictionaryLoader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    private val mutex = Mutex()

    @Volatile private var cache: List<Word>? = null

    suspend fun getAll(): List<Word> = cache ?: mutex.withLock {
        cache ?: load().also { cache = it }
    }

    suspend fun size(): Int = getAll().size

    private suspend fun load(): List<Word> = withContext(Dispatchers.IO) {
        context.assets.open("dictionary.json").use { stream ->
            val dtos: List<WordDto> = json.decodeFromString(
                stream.bufferedReader(Charsets.UTF_8).readText()
            )
            dtos.map { it.toDomain() }
        }
    }

    private fun WordDto.toDomain() = Word(
        id          = id,
        word        = word,
        pos         = pos,
        gender      = gender,
        etymology   = etymology,
        definitions = definitions.map { Definition(it.number, it.text, it.example) }
    )
}
