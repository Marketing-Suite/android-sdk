package com.cheetahdigital.push

import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

/**
 *
 */
class FirebaseIDService : FirebaseInstanceIdService() {

    private var TAG = "FirebaseIDService"

    /**
     * Keys for the SharedPreferences
     */
    companion object {
        const val PRIVATE_MODE = 0
        const val PREF_NAME = "ems_preferences"
        const val PREF_TOKEN = "fcm_token"
        const val PREF_PRID = "ems_prid"
    }

    /**
     * Callback method for
     */
    override fun onTokenRefresh() {
        // Get the Firebase Device Token
        var refreshedToken = FirebaseInstanceId.getInstance().token
        Log.d("Service: token", refreshedToken)

        // Save the Refreshed token to SharedPreferences
        val sharedPref : SharedPreferences = getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        val editor = sharedPref.edit()
        editor.putString(PREF_TOKEN, refreshedToken)
        editor.remove(PREF_PRID)
        editor.commit()
    }

}