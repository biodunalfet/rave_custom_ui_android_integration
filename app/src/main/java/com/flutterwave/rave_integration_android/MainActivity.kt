package com.flutterwave.rave_integration_android

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.flutterwave.raveandroid.RaveConstants
import com.flutterwave.raveandroid.RavePayActivity
import com.flutterwave.raveandroid.RavePayInitializer
import com.flutterwave.raveandroid.RavePayManager
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var ravePayInitializer : RavePayInitializer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.startBtn).setOnClickListener {
            launchSdk()
        }
    }

    private fun launchSdk() {

        ravePayInitializer = RavePayManager(this).setAmount(50.0)
                .setCountry("NG")
                .setCurrency("NGN")
                .setEmail("test@mailinator.com")
                .setfName("Tester")
                .setlName("Lastname")
                .setNarration("")
                .setPublicKey("YOUR-PUBLIC-KEY")
                .setSecretKey("YOUR-SECRET-KEY")
                .setTxRef(UUID.randomUUID().toString())
                .acceptAccountPayments(false)
                .acceptCardPayments(true)
                .acceptMpesaPayments(false)
                .acceptGHMobileMoneyPayments(false)
                .onStagingEnv(true)
                .allowSaveCardFeature(false)
                .createRavePayInitializer()

        if (ravePayInitializer.isStaging) {
            RavePayActivity.BASE_URL = RaveConstants.STAGING_URL
        } else {
            RavePayActivity.BASE_URL = RaveConstants.LIVE_URL
        }

        supportFragmentManager.beginTransaction().replace(R.id.container, TokenizeFragment()).addToBackStack(null).commit()

    }
}
