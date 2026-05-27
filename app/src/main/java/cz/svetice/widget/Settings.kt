package cz.svetice.widget

import android.content.Context

object Settings {
    private const val PREFS = "widget_prefs"
    private const val KEY_DAYS = "active_days"
    private const val KEY_FROM = "from"
    private const val KEY_TO = "to"
    private const val KEY_OVERRIDE = "override_enabled"

    // Default: Po-Pá, 6:00-9:00
    private const val DEFAULT_DAYS = "1111100"
    private const val DEFAULT_FROM = "06:00"
    private const val DEFAULT_TO = "09:00"

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getActiveDays(ctx: Context): BooleanArray {
        val s = prefs(ctx).getString(KEY_DAYS, DEFAULT_DAYS) ?: DEFAULT_DAYS
        return BooleanArray(7) { i -> s.getOrNull(i) == '1' }
    }

    fun setActiveDays(ctx: Context, days: BooleanArray) {
        val s = days.joinToString("") { if (it) "1" else "0" }
        prefs(ctx).edit().putString(KEY_DAYS, s).apply()
    }

    fun getFrom(ctx: Context): String = prefs(ctx).getString(KEY_FROM, DEFAULT_FROM) ?: DEFAULT_FROM
    fun setFrom(ctx: Context, v: String) { prefs(ctx).edit().putString(KEY_FROM, v).apply() }

    fun getTo(ctx: Context): String = prefs(ctx).getString(KEY_TO, DEFAULT_TO) ?: DEFAULT_TO
    fun setTo(ctx: Context, v: String) { prefs(ctx).edit().putString(KEY_TO, v).apply() }

    // Override: true = uživatel chce force ON (mimo okno), false = force OFF (v okně), null = sleduj okno
    fun getOverride(ctx: Context): Boolean? {
        val p = prefs(ctx)
        return if (!p.contains(KEY_OVERRIDE)) null else p.getBoolean(KEY_OVERRIDE, true)
    }
    fun setOverride(ctx: Context, v: Boolean?) {
        val e = prefs(ctx).edit()
        if (v == null) e.remove(KEY_OVERRIDE) else e.putBoolean(KEY_OVERRIDE, v)
        e.apply()
    }

    fun isInWindow(ctx: Context): Boolean {
        val cal = java.util.Calendar.getInstance()
        // Calendar.DAY_OF_WEEK: 1=Ne..7=So → převedeme na 0=Po..6=Ne
        val dow = (cal.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7
        if (!getActiveDays(ctx)[dow]) return false
        val curMin = cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE)
        return curMin in toMin(getFrom(ctx))..toMin(getTo(ctx))
    }

    private fun toMin(hhmm: String): Int {
        val p = hhmm.split(":")
        return (p.getOrNull(0)?.toIntOrNull() ?: 0) * 60 + (p.getOrNull(1)?.toIntOrNull() ?: 0)
    }

    /**
     * Efektivní stav: má widget refreshovat data nyní?
     * Override (pokud uživatel klikl na toggle) má přednost před oknem,
     * ale jen do nejbližší hrany okna - viz logika v RefreshScheduler.
     */
    fun isActive(ctx: Context): Boolean {
        val override = getOverride(ctx)
        val inWindow = isInWindow(ctx)
        return when (override) {
            null -> inWindow
            true -> true
            false -> false
        }
    }
}
