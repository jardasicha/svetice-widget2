package cz.svetice.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class TrainWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_REFRESH -> {
                // Manuální refresh - zatím jen překresli, ve fázi 3 se sem napojí volání API
                updateAll(context)
            }
            ACTION_TOGGLE -> {
                // Přepni stav v SharedPreferences a překresli
                val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                val current = prefs.getBoolean(KEY_ENABLED, true)
                prefs.edit().putBoolean(KEY_ENABLED, !current).apply()
                updateAll(context)
            }
        }
    }

    companion object {
        const val ACTION_REFRESH = "cz.svetice.widget.ACTION_REFRESH"
        const val ACTION_TOGGLE = "cz.svetice.widget.ACTION_TOGGLE"
        const val PREFS = "widget_prefs"
        const val KEY_ENABLED = "enabled"

        fun updateAll(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, TrainWidgetProvider::class.java))
            for (id in ids) updateWidget(context, mgr, id)
        }

        private fun updateWidget(context: Context, mgr: AppWidgetManager, widgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_train)

            // Statická data (zatím)
            views.setTextViewText(R.id.t1_planned, "06:12")
            views.setTextViewText(R.id.t1_eta, "3 min")

            views.setTextViewText(R.id.t2_planned, "07:42")
            views.setTextViewText(R.id.t2_delay, "+4")
            views.setTextViewText(R.id.t2_new, "07:46")
            views.setTextViewText(R.id.t2_eta, "93 min")

            views.setTextViewText(R.id.t3_planned, "08:54")
            views.setTextViewText(R.id.t3_delay, "+12")
            views.setTextViewText(R.id.t3_new, "09:06")
            views.setTextViewText(R.id.t3_eta, "165 min")

            // Toggle ikona podle stavu v prefs
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val enabled = prefs.getBoolean(KEY_ENABLED, true)
            views.setImageViewResource(
                R.id.btn_toggle,
                if (enabled) R.drawable.ic_power_on else R.drawable.ic_power_off
            )

            // Klik na ozubené kolečko → SettingsActivity
            val settingsIntent = Intent(context, SettingsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val settingsPi = PendingIntent.getActivity(
                context, 0, settingsIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.btn_settings, settingsPi)

            // Klik na refresh → broadcast ACTION_REFRESH
            val refreshIntent = Intent(context, TrainWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
            }
            val refreshPi = PendingIntent.getBroadcast(
                context, 1, refreshIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.btn_refresh, refreshPi)

            // Klik na toggle → broadcast ACTION_TOGGLE
            val toggleIntent = Intent(context, TrainWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE
            }
            val togglePi = PendingIntent.getBroadcast(
                context, 2, toggleIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.btn_toggle, togglePi)

            mgr.updateAppWidget(widgetId, views)
        }
    }
}
