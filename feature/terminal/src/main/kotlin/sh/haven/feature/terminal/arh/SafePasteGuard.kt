package sh.haven.feature.terminal.arh

/**
 * Safe Multi-Line Paste Guard (ADR-003).
 * Intercepts multi-line clipboard pastes containing newlines to avoid accidental shell execution
 * and routes them into the editable Floating Text Input Dialog.
 */
object SafePasteGuard {

    /**
     * Determines whether clipboard text contains multiple lines that should be intercepted
     * into the floating draft dialog rather than executed directly on the PTY stream.
     */
    fun shouldIntercept(text: CharSequence?, enabled: Boolean): Boolean {
        if (!enabled || text.isNullOrEmpty()) return false
        val str = text.toString()
        return str.contains("\n") && str.trim().lines().size > 1
    }
}
