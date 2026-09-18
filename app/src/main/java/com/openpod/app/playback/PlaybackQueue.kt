package com.openpod.app.playback

data class QueueSnapshot(
    val upcoming: List<Long>
)

object PlaybackQueue {
    fun playNow(upcoming: List<Long>, episodeId: Long): QueueSnapshot {
        if (episodeId == 0L) return QueueSnapshot(upcoming)
        return QueueSnapshot(upcoming.filter { it != episodeId })
    }

    fun playNext(nowPlayingId: Long, upcoming: List<Long>, episodeId: Long): QueueSnapshot {
        if (episodeId == 0L || episodeId == nowPlayingId) {
            return QueueSnapshot(upcoming)
        }
        return QueueSnapshot(listOf(episodeId) + upcoming.filter { it != episodeId })
    }

    fun add(nowPlayingId: Long, upcoming: List<Long>, episodeId: Long): QueueSnapshot {
        if (episodeId == 0L || episodeId == nowPlayingId || episodeId in upcoming) {
            return QueueSnapshot(upcoming)
        }
        return QueueSnapshot(upcoming + episodeId)
    }

    fun remove(upcoming: List<Long>, episodeId: Long): QueueSnapshot {
        return QueueSnapshot(upcoming.filter { it != episodeId })
    }

    fun move(upcoming: List<Long>, episodeId: Long, delta: Int): QueueSnapshot {
        val index = upcoming.indexOf(episodeId)
        if (index < 0 || upcoming.isEmpty()) return QueueSnapshot(upcoming)
        val target = (index + delta).coerceIn(0, upcoming.lastIndex)
        if (target == index) return QueueSnapshot(upcoming)
        val mutable = upcoming.toMutableList()
        val item = mutable.removeAt(index)
        mutable.add(target, item)
        return QueueSnapshot(mutable)
    }

    fun clear(): QueueSnapshot = QueueSnapshot(emptyList())

    fun advance(upcoming: List<Long>): Pair<Long, QueueSnapshot> {
        if (upcoming.isEmpty()) return 0L to QueueSnapshot(emptyList())
        return upcoming.first() to QueueSnapshot(upcoming.drop(1))
    }

    fun contains(upcoming: List<Long>, episodeId: Long): Boolean = episodeId != 0L && episodeId in upcoming
}
