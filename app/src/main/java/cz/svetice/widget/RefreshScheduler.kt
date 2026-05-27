package cz.svetice.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object RefreshScheduler {
    private const val INTERVAL_MS = 30_000L

    /**
     * Pokud jsme aktivní → naplánuje překreslení za 30s.
     * Pokud nejsme aktivní → naplánuje překreslení k nejbližšímu okraji okna,
     * aby se widget probudil přesně ve chvíli, kdy má začít.
     */
    fun schedule(ctx: Context) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(ctx, TrainWidgetProvider::class.java).apply {
            action = TrainWidgetProvider.ACTION_TICK
        }
        val pi = PendingIntent.getBroadcast(
            ctx, 100, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val nextMs = when {
            Settings.isActive(ctx) -> System.currentTimeMillis() + INTERVAL_MS
            else -> nextWindowEdgeMs(ctx)
        }

        // setExact není potřeba — máme rezervu, nepotřebujeme přesnost na sekundu
        am.set(AlarmManager.RTC, nextMs, pi)
    }

    fun cancel(ctx: Context) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(ctx, TrainWidgetProvider::class.java).apply {
            action = TrainWidgetProvider.ACTION_TICK
        }
        val pi = PendingIntent.getBroadcast(
            ctx, 100, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        am.cancel(pi)
    }

    /**
     * Najde nejbližší okamžik v budoucnu, kdy aktuální stav okna změní hodnotu.
     * Když jsme mimo okno → najde nejbližší start okna v aktivním dni.
     * Když jsme v okně → najde konec dnešního okna.
     * Hledá max 8 dní dopředu jako pojistka.
     */
    private fun nextWindowEdgeMs(ctx: Context): Long {
        val days = Settings.getActiveDays(ctx)
        val fromMin = toMinutes(Settings.getFrom(ctx))
        val toMin = toMinutes(Settings.getTo(ctx))

        val cal = Calendar.getInstance()

        // Když jsme v okně → vrať konec okna
        if (Settings.isInWindow(ctx)) {
            val end = (cal.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, toMin / 60)
                set(Calendar.MINUTE, toMin % 60)
                set(Calendar.SECOND, 0)
            }
            return end.timeInMillis + 60_000L // minutu po konci aby se přepnulo
        }

        // Mimo okno → hledej další start
        for (offset in 0..7) {
            val test = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, offset) }
            val dow = (test.get(Calendar.DAY_OF_WEEK) + 5) % 7
            if (!days[dow]) continue
            test.set(Calendar.HOUR_OF_DAY, fromMin / 60)
            test.set(Calendar.MINUTE, fromMin % 60)
            test.set(Calendar.SECOND, 0)
            if (test.timeInMillis > cal.timeInMillis) {
                return test.timeInMillis
            }
        }
        // fallback: 1h
        return cal.timeInMillis + 3_600_000L
    }

    private fun toMinutes(hhmm: String): Int {
        val p = hhmm.split(":")
        return (p.getOrNull(0)?.toIntOrNull() ?: 0) * 60 + (p.getOrNull(1)?.toIntOrNull() ?: 0)
    }
}
