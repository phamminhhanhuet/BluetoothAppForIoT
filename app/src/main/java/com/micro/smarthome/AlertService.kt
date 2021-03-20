package com.micro.smarthome

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.telephony.SmsManager

class AlertBoundService : Service(){
    private val binder = LocalBinder()
    private val userData : DataSource? = DataSource.getData()
    private var context : Context? =  null
    override fun onBind(intent: Intent?): IBinder? {
        return  binder
    }

    val sharedPref = context?.getSharedPreferences("com.micro.smarthome.PREFERENCE", Context.MODE_PRIVATE)
    val phone = sharedPref?.getString(USER_PHONE_NUMBER, "0123456789")

    fun setContext(ct : Context) { context = ct }

    fun sendRecord(subject : String?){
        val smsManager = SmsManager.getDefault() as SmsManager
        smsManager.sendTextMessage(phone, null, "$subject", null, null)

    }

    fun composeEmail(
        addresses: String?,
        subject: String?) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:") // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        if (context?.packageManager?.let { intent.resolveActivity(it) } != null) {
            startActivity(intent)
        }
    }


    fun call(){
        val intent = Intent(Intent.ACTION_CALL);
        intent.data = Uri.parse("tel:${userData?.phoneNumber}")
        startActivity(intent)
    }

    inner  class LocalBinder : Binder(){
        fun getService() : AlertBoundService {
            return this@AlertBoundService
        }
    }
}