package financial.atomic.molecular

import androidx.appcompat.app.AppCompatActivity
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import financial.atomic.molecular.databinding.ActivityMainBinding
import financial.atomic.transact.Config
import financial.atomic.transact.Transact
import financial.atomic.transact.receiver.TransactBroadcastReceiver
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private val token = ""
    private val config = Config(
        publicToken = token,
        product = Config.Product.deposit,
        environment = Config.Environment.SANDBOX,
        theme = Config.Theme(
            brandColor = "#5535FF",
            overlayColor = "#5535FF"
        ),

        /* See the Config class/docs for all possible options */

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
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // There are multiple ways to interact with transact - only one is needed

        // Recommended - also possible using Transact.present()
        // 1. Using the TransactBroadcastReceiver class to receive all events
        Transact.registerReceiver(this,
            object: TransactBroadcastReceiver() {
                override fun onClose(reason: String) {
                    Log.d("APP", "RECEIVER close $reason")
                }
                override fun onFinish(taskId: String) {
                    Log.d("APP", "RECEIVER finish $taskId")
                }
                override fun onInteraction(name: String, value: JSONObject) {
                    Log.d("APP", "RECEIVER interaction $name $value")
                }
                override fun onOpenURL(url: Uri, context: Context?) {
                    Log.d("APP", "RECEIVER open_url $url")
                }
            }
        )

        // 2. Using a custom BroadcastReceiver to receive all events
        registerReceiver(
            object: BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val extras = intent?.extras
                    val type = Transact.Event.valueOf(extras?.getString("type")!!)
                    Log.d("APP", "RECEIVER $type")
                }
            },
            IntentFilter(Transact.ACTION_EVENT)
        )

        // 3. Using StartActivityForResult to get close and finish events
        val safr = ActivityResultContracts.StartActivityForResult()
        val transactResult = registerForActivityResult(safr) {
            when (it.resultCode) {
                Activity.RESULT_CANCELED -> {
                    val reason = it.data?.extras?.getString("reason")
                    Toast.makeText(this, "Closed: $reason", Toast.LENGTH_LONG).show()
                }
                Activity.RESULT_OK -> {
                    val taskId = it.data?.extras?.getString("taskId")
                    Toast.makeText(this, "Completed: $taskId", Toast.LENGTH_LONG).show()
                }
            }

        }

        binding.dummyButton.setOnClickListener {
            Transact.present(this, config) // can pass the receiver as an argument here too
            /*
            // If using StartActivityForResult
            transactResult.launch(
                safr.createIntent(this, Intent(this, TransactActivity::class.java)),
                ActivityOptionsCompat.makeCustomAnimation(this,
                    android.R.anim.fade_in, android.R.anim.fade_out)
            )
             */
        }
    }
}