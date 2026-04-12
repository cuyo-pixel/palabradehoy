package com.palabradeldia.util

import java.time.LocalDate

// Maps each calendar date to a unique dictionary index.
//
// The algorithm runs a seeded Fisher-Yates shuffle over [0, size) using a
// Knuth MMIX LCG. The seed is derived from the year alone, so:
//   - the same date always maps to the same word (deterministic, offline)
//   - no word repeats within a calendar year
//   - each new year starts a fresh permutation
object DailyWordSelector {

    fun indexForDate(date: LocalDate, dictionarySize: Int): Int {
        require(dictionarySize > 0) { "Dictionary must not be empty" }
        val slot = (date.dayOfYear - 1) % dictionarySize
        return shuffleForYear(date.year, dictionarySize)[slot]
    }

    private fun shuffleForYear(year: Int, size: Int): IntArray {
        val array = IntArray(size) { it }
        var state: Long = year.toLong() * 1_000_003L
        for (i in size - 1 downTo 1) {
            state = lcgNext(state)
            val j = (state ushr 33).toInt() % (i + 1)
            val tmp = array[i]; array[i] = array[j]; array[j] = tmp
        }
        return array
    }

    // Knuth MMIX LCG constants.
    private fun lcgNext(state: Long): Long =
        state * 6_364_136_223_846_793_005L + 1_442_695_040_888_963_407L
}
