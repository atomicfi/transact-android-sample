package financial.atomic.molecular

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import financial.atomic.transact.Config
import financial.atomic.transact.Transact

// Implementing your own TransactActivity class is optional but gives more control
class TransactActivity : AppCompatActivity() {
    private val localToken = "4ce75223-a47f-410e-b3d8-f75967d14035"
    private val token = localToken
    private val config = Config(
        publicToken = token,
        product = Config.Product.deposit,
        environment = Config.Environment.LOCAL,
        theme = Config.Theme(
            brandColor = "#5535FF",
            overlayColor = "#5535FF"
        ),
        //distribution = Config.Distribution(
        //    type = Config.Distribution.Type.total,
        //    action = Config.Distribution.Action.create,
        //    amount = 1.0
        //),
        //language = Config.Language.en,
        //deeplink = Config.Deeplink(
        //    step = Config.Deeplink.Step.SEARCH_COMPANY,
        //)
    )
    private val transact by lazy { Transact(context = this, config) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
        setContentView(R.layout.activity_transact)

        val layout = findViewById<FrameLayout>(R.id.TransactLayout)
        layout.addView(transact.view())

        // Using transact directly to receive events
        transact.on(Transact.Event.CLOSE) { event ->
            setResult(Activity.RESULT_CANCELED, Intent().apply {
                putExtra("reason", event.data?.getString("reason"))
            })
            finish()
        }
        transact.on(Transact.Event.FINISH) { event ->
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra("taskId", event.data?.getString("taskId"))
            })
            finish()
        }
    }

    override fun onDestroy() {
        transact.destroy()
        super.onDestroy()
    }
}