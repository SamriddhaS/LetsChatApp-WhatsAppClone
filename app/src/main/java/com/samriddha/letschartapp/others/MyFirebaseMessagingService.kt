package com.samriddha.letschartapp.others

import android.annotation.SuppressLint
import android.content.Context
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.samriddha.letschartapp.others.Constants.KEY_DEVICE_TOKEN
import com.samriddha.letschartapp.others.Constants.SHARED_PREF_NAME

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)

        getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_DEVICE_TOKEN,p0)
            .apply()
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
    }

    fun getToken(context: Context):String{
        return context.getSharedPreferences(SHARED_PREF_NAME,MODE_PRIVATE).getString(KEY_DEVICE_TOKEN,"").toString()
    }

}