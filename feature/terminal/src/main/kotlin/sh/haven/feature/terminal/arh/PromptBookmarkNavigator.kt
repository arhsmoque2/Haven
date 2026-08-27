package sh.haven.feature.terminal.arh

/**
 * Pure navigation helper for semantic prompt landmark jumping and stepping (ADR-003).
 */
object PromptBookmarkNavigator {

    /**
     * Computes the next bookmark index, clamped to the last available index.
     */
    fun nextIndex(currentIndex: Int, totalBookmarks: Int): Int {
        if (totalBookmarks <= 0) return 0
        return (currentIndex + 1).coerceAtMost(totalBookmarks - 1)
    }

    /**
     * Computes the previous bookmark index, clamped to 0.
     */
    fun previousIndex(currentIndex: Int): Int {
        return (currentIndex - 1).coerceAtLeast(0)
    }

    /**
     * Computes the 1-based display index for the ticker badge (e.g. "[1/5]").
     */
    fun displayIndex(currentIndex: Int, totalBookmarks: Int): Int {
        if (totalBookmarks <= 0) return 0
        return currentIndex.coerceIn(0, totalBookmarks - 1) + 1
    }
}
