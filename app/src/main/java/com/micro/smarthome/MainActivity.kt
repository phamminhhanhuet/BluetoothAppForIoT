package com.micro.smarthome

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPref = getSharedPreferences("com.micro.smarthome.PREFERENCE", Context.MODE_PRIVATE) ?: return
        val name = sharedPref.getString(USER_NAME, "Default Value")
        val phone = sharedPref.getString(USER_PHONE_NUMBER, "84367212156")
        val email = sharedPref.getString(USER_EMAIL, "phamminhhanhk62@gmail.com")
        val password = sharedPref.getString(USER_PASSWORD, "*******")
        val isPhone = sharedPref.getBoolean(USER_IS_PHONE, true)
        val isMes = sharedPref.getBoolean(USER_IS_MESSAGE, true)
        val isEmail = sharedPref.getBoolean(USER_IS_EMAIL, true)

        var userData : DataSource? = DataSource.getData()
        userData?.fullname = name
        userData?.phoneNumber = phone
        userData?.email = email
        userData?.password = password
        userData?.isConnectPhone = isPhone
        userData?.isConnectMail = isEmail
        userData?.isConnectMes = isMes

        sharedPref.edit().putString(USER_NAME, userData?.fullname)
        sharedPref.edit().putString(USER_IS_PHONE, userData?.phoneNumber)
        sharedPref.edit().putString(USER_EMAIL, userData?.email)
        sharedPref.edit().putString(USER_PASSWORD, userData?.password)
        sharedPref.edit().putBoolean(USER_IS_MESSAGE, userData?.isConnectMes!!)
        sharedPref.edit().putBoolean(USER_IS_EMAIL, userData?.isConnectMail!!)
        sharedPref.edit().putBoolean(USER_IS_PHONE, userData?.isConnectPhone!!)
        sharedPref.edit().apply()


    }
}