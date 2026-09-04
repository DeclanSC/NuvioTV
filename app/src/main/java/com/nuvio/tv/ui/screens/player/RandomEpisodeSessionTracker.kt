package com.nuvio.tv.core.player

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RandomEpisodeSessionTracker @Inject constructor() {

    private var sessionContentId: String? = null
    private val watchedEpisodes = mutableSetOf<Pair<Int, Int>>()

    @Synchronized
    fun record(contentId: String, season: Int, episode: Int) {
        if (sessionContentId != contentId) {
            sessionContentId = contentId
            watchedEpisodes.clear()
        }
        watchedEpisodes.add(season to episode)
    }

    @Synchronized
    fun watchedPairs(contentId: String): Set<Pair<Int, Int>> {
        if (sessionContentId != contentId) return emptySet()
        return watchedEpisodes.toSet()
    }

    @Synchronized
    fun clear() {
        sessionContentId = null
        watchedEpisodes.clear()
    }
}

@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface RandomEpisodeSessionEntryPoint {
    fun randomEpisodeSessionTracker(): RandomEpisodeSessionTracker
}