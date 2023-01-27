package com.cheetahdigital.push

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import experian.mobilesdk.EMSMobileSDK
import experian.mobilesdk.IEMSPRIDCallback
import experian.mobilesdk.Region
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Main Activity of the Sample App
 * Implements IEMSPRIDCallback
 */
class MainActivity : AppCompatActivity(), IEMSPRIDCallback {

    // Views
    private lateinit var buttonRegister: Button
    private lateinit var editTextEmail: EditText
    private lateinit var textViewRegistered: TextView

    // Variables
    private var mPRID: String? = ""
    private var mEmailAddress: String? = ""

    override fun onPRIDReceived(PRID: String?) {
        // on receive, set the PRID to the variable mPRID
        mPRID = PRID
        mPRID?.let { Log.d("PRIDCallback: PRID", it) }

        // Save PRID to SharedPreferences
        val sharedPref: SharedPreferences =
            getSharedPreferences(FirebaseIDService.PREF_NAME, MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString(FirebaseIDService.PREF_PRID, mPRID)
        editor.apply()

        // Hide Fields now
        buttonRegister.visibility = View.GONE
        editTextEmail.visibility = View.GONE
        textViewRegistered.visibility = View.VISIBLE

        val map = mapOf(
            "s_first_name" to "SDK",
            "s_last_name" to "Sample",
            "s_email_address" to mEmailAddress,
            "s_password" to "123456789",
            "s_phone_number" to "9173827374",
            "s_push_registration_id" to mPRID,
            "s_logged_in" to "N"
        )
        EMSMobileSDK.Default().apiPost(3522, map) {
            if (it == null) {
                Log.d("Register", "Registration Successful")
            } else {
                Log.e("Register failed", it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // Initialize the MS SDK
        EMSMobileSDK.Default().init(
            applicationContext,
            resources.getString(R.string.app_id),
            resources.getInteger(R.integer.customer_id),
            Region.NORTH_AMERICA
        )

        // Set the PRID callback
        EMSMobileSDK.Default().registerPRIDCallback(this)

        // Find Views
        buttonRegister = findViewById(R.id.btn_register)
        editTextEmail = findViewById(R.id.edt_email)
        textViewRegistered = findViewById(R.id.tv_registered)

        // Check if there is a PRID in SharedPreferences
        val sharedPref: SharedPreferences =
            getSharedPreferences(FirebaseIDService.PREF_NAME, MODE_PRIVATE)

        mPRID = sharedPref.getString(FirebaseIDService.PREF_PRID, "")
        mPRID?.let { Log.d("Main Activity: PRID", it) }

        if (!mPRID.isNullOrBlank()) {
            buttonRegister.visibility = View.GONE
            editTextEmail.visibility = View.GONE
            textViewRegistered.visibility = View.VISIBLE
        } else {
            buttonRegister.visibility = View.VISIBLE
            editTextEmail.visibility = View.VISIBLE
            textViewRegistered.visibility = View.GONE

            // Button will Register to Push Notification onClick
            buttonRegister.setOnClickListener {

                // Get Email Address from Field
                mEmailAddress = editTextEmail.text.toString()

                if (mPRID.isNullOrBlank() && !mEmailAddress.isNullOrBlank()) {
                    // Get FCM token from SharedPreferences
                    val token = sharedPref.getString(FirebaseIDService.PREF_TOKEN, "")
                    if (token != null) {
                        Log.d("Main Activity: Token", token)
                    }

                    // Send FCM token to CCMP
                    if (!token.isNullOrBlank()) {
                        EMSMobileSDK.Default().setToken(applicationContext, token)
                    } else {
                        Toast.makeText(this, "Push Notification is not enabled", Toast.LENGTH_LONG)
                            .show()
                    }
                } else {
                    Toast.makeText(this, "Email Is Required", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
