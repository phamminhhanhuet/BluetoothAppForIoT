package com.micro.smarthome
val USER_NAME : String = "USER_NAME_PREFERNCE"
val USER_PHONE_NUMBER : String = "USER_PHONE_NUMBER_PREFERENCE"
val USER_EMAIL : String = "USER_EMAIL_PREFERENCE"
val USER_PASSWORD : String = "USER_PASSWORD_PREFERENCE"
val USER_IS_PHONE : String = "USER_IS_PHONE_PREFERENCE"
val USER_IS_EMAIL :String = "USER_IS_EMAIL_PREFERENCE"
val USER_IS_MESSAGE : String = "USER_IS_MESSAGE_PREFERENCE"
object DataSource {
    var fullname : String? = "Pham Hanh"
        get() = field?.capitalize().let {
            "Not yet had an account"
        }
        set(value) { field = value }
    var phoneNumber : String? = "84367212156"
        get() = field?.let {
            "Not yet had a phone"
        }
        set(value) {field = value}
    var email : String? = "phamminhhanhk62@gmail.com"
        get() = field?.let {
            "Not yet had a gmail"
        }
        set(value) {field = value}
    var password : String? = "*******"
        get() = field?.let {
            "Not yet set up a password"
        }
        set(value) { field = value}

    var isConnectPhone : Boolean = true
    var isConnectMail : Boolean = true
    var isConnectMes : Boolean = true
    fun getData() = this
}