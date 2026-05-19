package com.example.siheunggagae.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object FavoritesCache {
    private val _ids = MutableStateFlow<Set<Int>>(emptySet())
    val ids: StateFlow<Set<Int>> = _ids.asStateFlow()
    var isLoaded = false

    fun init(set: Set<Int>) {
        _ids.value = set
        isLoaded = true
    }

    fun add(id: Int) { _ids.value = _ids.value + id }
    fun remove(id: Int) { _ids.value = _ids.value - id }
    fun contains(id: Int) = id in _ids.value
    fun reset() { _ids.value = emptySet(); isLoaded = false }
}
