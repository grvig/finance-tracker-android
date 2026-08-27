package com.grvig.financetracker

import android.content.Context

/**
 * Per-user, on-device notification settings. Kept local rather than in
 * Firestore: the polling that drives these runs on this device, so no other
 * device needs to read them, and keeping them off the shared profile avoids a
 * rules change.
 *
 * The follow set and master switch are keyed by the signed-in user's uid so
 * that signing a different account in on the same device does not inherit the
 * previous user's choices.
 */
object NotificationPreferences {

    private const val PREFS_NAME = "finance_tracker_notifications"
    private const val KEY_ENABLED_PREFIX = "enabled_"
    private const val KEY_FOLLOWS_PREFIX = "follows_"
    private const val KEY_LAST_SEEN = "last_seen_created_at"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isEnabled(context: Context, uid: String): Boolean {
        if (uid.isBlank()) return false
        return prefs(context).getBoolean(KEY_ENABLED_PREFIX + uid, false)
    }

    fun setEnabled(context: Context, uid: String, enabled: Boolean) {
        if (uid.isBlank()) return
        prefs(context).edit().putBoolean(KEY_ENABLED_PREFIX + uid, enabled).apply()
    }

    fun followedUsers(context: Context, uid: String): Set<String> {
        if (uid.isBlank()) return emptySet()
        val stored = prefs(context).getString(KEY_FOLLOWS_PREFIX + uid, "") ?: ""
        return stored.split("\n").filter { it.isNotBlank() }.toSet()
    }

    fun setFollowedUsers(context: Context, uid: String, followed: Set<String>) {
        if (uid.isBlank()) return
        prefs(context)
            .edit()
            .putString(KEY_FOLLOWS_PREFIX + uid, followed.joinToString("\n"))
            .apply()
    }

    fun setFollowing(
        context: Context,
        uid: String,
        target: String,
        follow: Boolean
    ) {
        val current = followedUsers(context, uid).toMutableSet()
        if (follow) current.add(target) else current.remove(target)
        setFollowedUsers(context, uid, current)
    }

    /**
     * Highest expense createdAt already considered, so a poll never re-notifies
     * for the same row. A single high-water mark rather than per-uid: it only
     * needs to move forward.
     */
    fun lastSeenCreatedAt(context: Context): Long {
        return prefs(context).getLong(KEY_LAST_SEEN, 0L)
    }

    fun setLastSeenCreatedAt(context: Context, value: Long) {
        prefs(context).edit().putLong(KEY_LAST_SEEN, value).apply()
    }
}
