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
        for (id in appWidgetIds) updateWidget(context, appWidgetManager, id)
        RefreshScheduler.schedule(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        RefreshScheduler.schedule(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        RefreshScheduler.cancel(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_REFRESH -> {
                // Manuální refresh — překresli widget (později i API volání)
                updateAll(context)
            }
            ACTION_TICK -> {
                // Plánovaný tick z AlarmManageru — překresli a naplánuj další
                updateAll(context)
                RefreshScheduler.schedule(context)
            }
            ACTION_TOGGLE -> {
                // Přepneme override podle aktuálního stavu
                val current = Settings.isActive(context)
                Settings.setOverride(context, !current)
                updateAll(context)
                RefreshScheduler.schedule(context)
            }
            ACTION_REFRESH_FROM_SETTINGS -> {
                // Po uložení nastavení smaž override (vrať se k oknu) a naplánuj
                Settings.setOverride(context, null)
                updateAll(context)
                RefreshScheduler.schedule(context)
            }
        }
    }

    companion object {
        const val ACTION_REFRESH = "cz.svetice.widget.ACTION_REFRESH"
        const val ACTION_TOGGLE = "cz.svetice.widget.ACTION_TOGGLE"
        const val ACTION_TICK = "cz.svetice.widget.ACTION_TICK"
        const val ACTION_REFRESH_FROM_SETTINGS = "cz.svetice.widget.ACTION_REFRESH_FROM_SETTINGS"

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

            // Toggle ikona reflektuje SKUTEČNÝ stav (okno AND override)
            views.setImageViewResource(
                R.id.btn_toggle,
                if (Settings.isActive(context)) R.drawable.ic_power_on else R.drawable.ic_power_off
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

            // Refresh
            val refreshIntent = Intent(context, TrainWidgetProvider::class.java)
                .apply { action = ACTION_REFRESH }
            val refreshPi = PendingIntent.getBroadcast(
                context, 1, refreshIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.btn_refresh, refreshPi)

            // Toggle
            val toggleIntent = Intent(context, TrainWidgetProvider::class.java)
                .apply { action = ACTION_TOGGLE }
            val togglePi = PendingIntent.getBroadcast(
                context, 2, toggleIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.btn_toggle, togglePi)

            mgr.updateAppWidget(widgetId, views)
        }
    }
}
