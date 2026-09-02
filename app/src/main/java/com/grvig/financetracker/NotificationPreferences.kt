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
    private const val KEY_LAST_SEEN_LEGACY = "last_seen_created_at"
    private const val KEY_LAST_SEEN_PREFIX = "last_seen_created_at_"

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
     * for the same row. Keyed by uid like the settings above: two accounts on
     * one phone follow different people, so one marker for both would let the
     * account that polls first hide expenses from the other.
     */
    fun lastSeenCreatedAt(context: Context, uid: String): Long {
        if (uid.isBlank()) return 0L

        val stored = prefs(context)
        val key = KEY_LAST_SEEN_PREFIX + uid

        // Upgrades from the single shared marker inherit it, so the first poll
        // after updating does not alert for a backlog already seen.
        return if (stored.contains(key)) {
            stored.getLong(key, 0L)
        } else {
            stored.getLong(KEY_LAST_SEEN_LEGACY, 0L)
        }
    }

    fun setLastSeenCreatedAt(context: Context, uid: String, value: Long) {
        if (uid.isBlank()) return
        prefs(context).edit().putLong(KEY_LAST_SEEN_PREFIX + uid, value).apply()
    }
}
