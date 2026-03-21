package com.palabradeldia.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.palabradeldia.data.db.AppDatabase
import com.palabradeldia.data.db.FavouriteEntity
import com.palabradeldia.domain.model.DailyWord
import com.palabradeldia.domain.model.Definition
import com.palabradeldia.domain.model.ThemeMode
import com.palabradeldia.domain.model.Word
import com.palabradeldia.domain.repository.WordRepository
import com.palabradeldia.util.DailyWordSelector
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordRepositoryImpl @Inject constructor(
    private val loader: DictionaryLoader,
    private val db: AppDatabase,
    private val dataStore: DataStore<Preferences>
) : WordRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getDictionarySize(): Int = loader.size()

    override suspend fun getWordForDate(date: LocalDate): DailyWord {
        val words = loader.getAll()
        val index = DailyWordSelector.indexForDate(date, words.size)
        return DailyWord(date = date, word = words[index])
    }

    override suspend fun getTodayWord(): DailyWord = getWordForDate(LocalDate.now())

    override fun getFavourites(): Flow<List<Word>> =
        db.favouritesDao().observeAll().map { it.map { e -> e.toDomain() } }

    override suspend fun addFavourite(word: Word) =
        db.favouritesDao().insert(word.toEntity())

    override suspend fun removeFavourite(word: Word) =
        db.favouritesDao().deleteById(word.id)

    override suspend fun isFavourite(wordId: Int): Boolean =
        db.favouritesDao().countById(wordId) > 0

    override suspend fun exportFavourites(): String {
        val words = db.favouritesDao().getAll().map { it.toDomain() }
        return json.encodeToString(words.map { w ->
            ExportedWord(w.id, w.word, w.pos, w.gender, w.etymology,
                w.definitions.map { DefinitionDto(it.number, it.text, it.example) })
        })
    }

    override suspend fun importFavourites(payload: String): Int {
        return try {
            val imported = json.decodeFromString<List<ExportedWord>>(payload)
            imported.forEach { ew ->
                val word = Word(ew.id, ew.word, ew.pos, ew.gender, ew.etymology,
                    ew.definitions.map { Definition(it.number, it.text, it.example) })
                if (!isFavourite(word.id)) addFavourite(word)
            }
            imported.size
        } catch (e: Exception) {
            -1
        }
    }

    override fun getThemeMode(): Flow<ThemeMode> =
        dataStore.data.map { ThemeMode.valueOf(it[PREF_THEME] ?: ThemeMode.SYSTEM.name) }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[PREF_THEME] = mode.name }
    }

    private fun Word.toEntity() = FavouriteEntity(
        wordId          = id,
        word            = word,
        pos             = pos,
        gender          = gender,
        etymology       = etymology,
        definitionsJson = json.encodeToString(
            definitions.map { DefinitionDto(it.number, it.text, it.example) }
        )
    )

    private fun FavouriteEntity.toDomain(): Word {
        val defs = json.decodeFromString<List<DefinitionDto>>(definitionsJson)
        return Word(wordId, word, pos, gender, etymology,
            defs.map { Definition(it.number, it.text, it.example) })
    }

    @Serializable
    private data class ExportedWord(
        val id: Int, val word: String, val pos: String,
        val gender: String?, val etymology: String?,
        val definitions: List<DefinitionDto>
    )

    private companion object {
        val PREF_THEME = stringPreferencesKey("theme_mode")
    }
}
