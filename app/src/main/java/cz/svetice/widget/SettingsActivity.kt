package cz.svetice.widget

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private val dayLabels = arrayOf("Po", "Út", "St", "Čt", "Pá", "So", "Ne")
    private lateinit var days: BooleanArray
    private var fromHHmm = "06:00"
    private var toHHmm = "09:00"
    private lateinit var fromBtn: Button
    private lateinit var toBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        days = Settings.getActiveDays(this)
        fromHHmm = Settings.getFrom(this)
        toHHmm = Settings.getTo(this)

        val daysRow = findViewById<LinearLayout>(R.id.days_row)
        for (i in 0..6) {
           val b = Button(this).apply {
    text = dayLabels[i]
    textSize = 14f
    isAllCaps = false
    minWidth = 0
    minimumWidth = 0
    setPadding(0, 16, 0, 16)
    layoutParams = LinearLayout.LayoutParams(
        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
    ).apply { setMargins(4, 0, 4, 0) }
    setOnClickListener {
        days[i] = !days[i]
        refreshDayButtons(daysRow)
    }
}
            daysRow.addView(b)
        }
        refreshDayButtons(daysRow)

        fromBtn = findViewById(R.id.from_btn)
        toBtn = findViewById(R.id.to_btn)
        fromBtn.text = fromHHmm
        toBtn.text = toHHmm

        fromBtn.setOnClickListener {
            pickTime(fromHHmm) { hh, mm ->
                fromHHmm = "%02d:%02d".format(hh, mm)
                fromBtn.text = fromHHmm
            }
        }
        toBtn.setOnClickListener {
            pickTime(toHHmm) { hh, mm ->
                toHHmm = "%02d:%02d".format(hh, mm)
                toBtn.text = toHHmm
            }
        }

        findViewById<Button>(R.id.save_btn).setOnClickListener {
            Settings.setActiveDays(this, days)
            Settings.setFrom(this, fromHHmm)
            Settings.setTo(this, toHHmm)
            // Pošli widgetu broadcast, ať se přepočítá (a smaže override)
            sendBroadcast(
                Intent(this, TrainWidgetProvider::class.java).apply {
                    action = TrainWidgetProvider.ACTION_REFRESH_FROM_SETTINGS
                }
            )
            finish()
        }
    }

    private fun refreshDayButtons(row: LinearLayout) {
        for (i in 0..6) {
            val b = row.getChildAt(i) as Button
            if (days[i]) {
                b.setBackgroundColor(0xFF5DCAA5.toInt())
                b.setTextColor(0xFF000000.toInt())
            } else {
                b.setBackgroundColor(0xFF2A2A2A.toInt())
                b.setTextColor(0xFFBBBBBB.toInt())
            }
        }
    }

    private fun pickTime(current: String, cb: (Int, Int) -> Unit) {
        val p = current.split(":")
        val h = p.getOrNull(0)?.toIntOrNull() ?: 6
        val m = p.getOrNull(1)?.toIntOrNull() ?: 0
        TimePickerDialog(this, { _, hh, mm -> cb(hh, mm) }, h, m, true).show()
    }
}
