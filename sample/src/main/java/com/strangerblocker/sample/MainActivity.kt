package com.strangerblocker.sample

import android.app.Activity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.strangerblocker.engine.CallDecision
import com.strangerblocker.engine.EngineConfig
import com.strangerblocker.engine.NotificationConfig
import com.strangerblocker.engine.PatternLearner
import com.strangerblocker.engine.SbEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Minimal host-app demo: constructs [SbEngine] and exercises its decision and
 * pattern-learning API. No framework services are registered here — wire those
 * as shown in sb-engine-android/README.md.
 */
class MainActivity : Activity() {

    private val output = TextView(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val engine = SbEngine(
            this,
            EngineConfig(prefsName = "sb_sample_prefs"),
            NotificationConfig(
                smallIconRes = android.R.drawable.ic_menu_info_details,
                channelId = "sample_alerts",
            ),
        )

        val scroll = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(
                TextView(this@MainActivity).apply {
                    text = "Stranger Blocker Engine — sample host"
                    textSize = 18f
                },
            )
            addView(output)
        }
        scroll.addView(layout)
        setContentView(scroll)

        CoroutineScope(Dispatchers.IO).launch {
            val report = buildString {
                appendLine("Call decisions:")
                listOf(
                    null to "private/unknown",
                    "+6285592679948" to "unknown mobile",
                    "+622130179723" to "unknown landline",
                    "WhatsApp Call" to "VoIP handle (no number)",
                ).forEach { (number, label) ->
                    val d = engine.shouldBlockCall(number)
                    appendLine("  $label → $d")
                }
                appendLine()
                appendLine("SMS decisions (no whitelist/contacts on this device):")
                appendLine("  \"promo offer 50%\" → ${engine.smsBlockReason("6281200000001", "promo offer 50%")}")
                appendLine()
                appendLine("Pattern learning from blocked numbers:")
                val patterns = PatternLearner.learn(
                    listOf(
                        "+6285592679948",
                        "+6285592679125",
                        "+6285592679231",
                    ),
                )
                patterns.forEach { p -> appendLine("  ${p.prefix} (${p.count} numbers)") }
            }
            withContext(Dispatchers.Main) {
                output.text = report
            }
        }
    }
}
