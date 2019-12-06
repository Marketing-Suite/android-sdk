package com.cheetahdigital.push

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.android.volley.VolleyError
import experian.mobilesdk.*

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
        Log.d("PRIDCallback: PRID", mPRID)

        // Save PRID to SharedPreferences
        val sharedPref: SharedPreferences = getSharedPreferences(FirebaseIDService.PREF_NAME, FirebaseIDService.PRIVATE_MODE)
        val editor = sharedPref.edit()
        editor.putString(FirebaseIDService.PREF_PRID, mPRID)
        editor.commit()

        // Hide Fields now
        buttonRegister.visibility = View.GONE
        editTextEmail.visibility = View.GONE
        textViewRegistered.visibility = View.VISIBLE

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
                Region.NORTH_AMERICA)

        // Set the PRID callback
        EMSMobileSDK.Default().registerPRIDCallback(this)

        // Find Views
        buttonRegister = findViewById(R.id.btn_register)
        editTextEmail = findViewById(R.id.edt_email)
        textViewRegistered = findViewById(R.id.tv_registered)

        // Check if there is a PRID in SharedPreferences
        val sharedPref: SharedPreferences = getSharedPreferences(FirebaseIDService.PREF_NAME, FirebaseIDService.PRIVATE_MODE)

        mPRID = sharedPref.getString(FirebaseIDService.PREF_PRID, "")
        Log.d("Main Activity: PRID", mPRID)

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
                    var token = sharedPref.getString(FirebaseIDService.PREF_TOKEN, "")
                    Log.d("Main Activity: Token", token)

                    // Send FCM token to CCMP
                    if (!token.isNullOrBlank()) {
                        EMSMobileSDK.Default().setToken(applicationContext, token)
                    } else {
                        Toast.makeText(this, "Push Notification is not enabled", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Email Is Required", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
