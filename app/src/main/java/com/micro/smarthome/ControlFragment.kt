package com.micro.smarthome

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.ColorStateList
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.telephony.SmsManager
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_control.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ControlFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ControlFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private val REQUEST_ENABLE_BLUETOOTH = 1
    private val REQUEST_DISCOVER_BT = 0

    private var navController : NavController? = null
    private var userData : DataSource? = DataSource.getData()
    private val DEVICE_ADDRESS : String? = "00:20:10:09:10:F2" //MAC Address of Bluetooth Module
    //private val DEVICE_ADDRESS : String? = "41:42:00:00:0E:E7"
    private val PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
    private var command: String? = ""

    private var blueAdapter : BluetoothAdapter? = null
    private lateinit var targetedDevice : BluetoothDevice
    private var socket : BluetoothSocket? = null
    private var outputStream : OutputStream? = null
    private var inputStream: InputStream? = null
    private var connected : Boolean = false


    var name  = ""
    var phone  = ""
    var email = ""
    var password = ""
    var isPhone = true
    var isMes = true
    var isEmail = true

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val intent  = Intent(context, AlertBoundService::class.java)
        context?.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        navController = container?.let { Navigation.findNavController(it) }
        setHasOptionsMenu(true)
        blueAdapter = BluetoothAdapter.getDefaultAdapter()

        val sharedPref = activity?.getSharedPreferences("com.micro.smarthome.PREFERENCE", Context.MODE_PRIVATE)
        name = sharedPref!!.getString(USER_NAME, "Pham Hanh").toString()
        phone = sharedPref!!.getString(USER_PHONE_NUMBER, "01234567").toString()
        email = sharedPref!!.getString(USER_EMAIL, "yourmail@gmail.com").toString()
        password = sharedPref!!.getString(USER_PASSWORD, "*******").toString()
        isPhone = sharedPref!!.getBoolean(USER_IS_PHONE, true)
        isMes = sharedPref!!.getBoolean(USER_IS_MESSAGE, true)
        isEmail = sharedPref!!.getBoolean(USER_IS_EMAIL, true)

        return inflater.inflate(R.layout.fragment_control, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        toolbarControl.inflateMenu(R.menu.menu_connected)
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_connected, menu)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_on -> {
                BluetoothStart()
            }
            R.id.menu_off -> {
                BluetoothEnd()
            }
            R.id.menu_discover -> {
                BluetoothDiscover()
            }
            R.id.menu_paired_device -> {
                BluetoothPairedDevice()
            }
            R.id.menu_moreInfor ->
                navController?.navigate(R.id.action_controlFragment_to_InformationFragment)
        }
        return super.onOptionsItemSelected(item)
    }

    private var lightOneStatus = false
    private var lightTwoStatus = false
    private var refrigeratorStatus = false
    private var entryOut = "EntryOut"
    private var entryIn = "EntryIn"
    private var securityStatus = true
    private var numOut : Int = 0
    private var numIn : Int = 0
    private var idDone = true
    private var alerService : AlertBoundService? = null
    private var isBound = false
    private val serviceConnection = object  : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AlertBoundService.LocalBinder
            alerService = binder.getService()
            isBound = true
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        if(connected == true) {
            val available = inputStream?.available()
            val bytes = available?.let { ByteArray(it) }
            Log.i("server", "Reading")
            available?.let { inputStream!!.read(bytes, 0, it) }
            val text = bytes?.let { String(it) }
            text?.let { Log.i("server", it) }
            if(text?.contains(entryOut)!!){
                val indexStart = text?.indexOf(entryOut)
                val indexEnd = text?.indexOf(":");
                val trimOut =  text?.substring(indexStart + 8, indexEnd -1)
                numOut = trimOut.toInt()
            }
            if(text!!.contains(entryIn)!!){
                val indexStart = text?.indexOf(entryIn)
                val indexEnd = text?.indexOf("_")
                val trimIn = text?.substring(indexStart+ 7, indexEnd -1 )
                numIn = trimIn.toInt()
            }
            if(text!!.contains("Security")!!){
                securityStatus = false
            }
            statusIRout.text = "OUNT: $numOut"
            statusIRin.text = "IN: $numIn"
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            val formatted = current.format(formatter)
            statusIRecord.text = "Record until $formatted"

            lightOne.setOnClickListener {
                showToast("Light 1 is click!")
                command = when(lightOneStatus){
                    false -> {
                        statusLightOne.text = "ON"
                        ImageViewCompat.setImageTintList(imgLightOne, ColorStateList.valueOf(resources.getColor(R.color.green_100)))
                        "1"
                    }
                    true -> {
                        statusLightOne.text = "OFF"
                        ImageViewCompat.setImageTintList(imgLightOne, ColorStateList.valueOf(resources.getColor(R.color.colorAccent)))
                        "2"
                    }

                }
                lightOneStatus = !lightOneStatus
                try {
                    outputStream!!.write(command!!.toByteArray())
                    outputStream!!.flush()
                } catch ( e : IOException){
                    e.printStackTrace()
                }
                Log.d("TagOutputstream", command!!)

            }
            lightTwo.setOnClickListener{
                command = when(lightTwoStatus){
                    false -> {
                        statusLightTwo.text = "ON"
                        ImageViewCompat.setImageTintList(imgLightTwo, ColorStateList.valueOf(resources.getColor(R.color.green_100)))
                        "3"
                    }
                    true -> {
                        statusLightTwo.text = "OFF"
                        ImageViewCompat.setImageTintList(imgLightTwo, ColorStateList.valueOf(resources.getColor(R.color.colorAccent)))
                        "4"
                    }
                }
                lightTwoStatus = !lightTwoStatus
                try {
                    outputStream!!.write(command!!.toByteArray())
                    outputStream!!.flush()
                }catch (e : IOException){
                    e.printStackTrace()
                }
                Log.d("TagOutputstream", command!!)
            }

            refrigerator.setOnClickListener {
                command = when(refrigeratorStatus){
                    false -> {
                        statusRefrigerator.text = "ON"
                        ImageViewCompat.setImageTintList(imgLightOne, ColorStateList.valueOf(resources.getColor(R.color.teal_200)))
                        "5"
                    }
                    true -> {
                        statusRefrigerator.text = "OFF"
                        ImageViewCompat.setImageTintList(imgLightOne, ColorStateList.valueOf(resources.getColor(R.color.colorAccent)))
                        "6"
                    }
                }
                refrigeratorStatus = !refrigeratorStatus
                try {
                    outputStream!!.write(command!!.toByteArray())
                    outputStream!!.flush()
                } catch (e : IOException){
                    e.printStackTrace()
                }
                Log.d("TagOutputstream", command!!)
            }
            if(!securityStatus)
                ImageViewCompat.setImageTintList(imgLightOne, ColorStateList.valueOf(resources.getColor(R.color.colorAccent)))
            if(!securityStatus && isBound) {
                // sendRecord("There is a serious alert from anti-theft system! Please check your camera at $formatted as soon as possible!")
                //composeEmail(userData?.email, "There is a serious alert from anti-theft system! Please check your camera at $formatted as soon as possible!")
                //call()
                context?.let { alerService!!.setContext(it) }
                // if(userData?.isConnectMes!!)
                if(isMes)
                    alerService!!.sendRecord("There is a serious alert from anti-theft system! Please check your camera at $formatted as soon as possible!")
                //if(userData?.isConnectMail!!)
                if(isEmail)
                    alerService!!.composeEmail(email, "There is a serious alert from anti-theft system! Please check your camera at $formatted as soon as possible!")
                // if(userData?.isConnectPhone!!)
                if(isPhone)
                    alerService!!.call()
            }
            security.setOnClickListener {
                if(!securityStatus) {
                    command = "T"
                    securityStatus = true
                    ImageViewCompat.setImageTintList(imgLightOne, ColorStateList.valueOf(resources.getColor(R.color.teal_200)))
                    try {
                        outputStream!!.write(command!!.toByteArray())
                        outputStream!!.flush()
                    } catch (e : IOException){
                        e.printStackTrace()
                    }
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        if(!idDone){
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            val formatted = current.format(formatter)
            context?.let { alerService?.setContext(it) }
            alerService?.sendRecord("There is a serious alert from anti-theft system! Please check your camera at $formatted as soon as possible!")
            alerService?.composeEmail(userData?.email, "There is a serious alert from anti-theft system! Please check your camera at $formatted as soon as possible!")
            alerService?.call()
            idDone = false
        }
        userData.run {
            //check if bluetooth is available
            if(blueAdapter == null){
                statusBluetooth.text = "Bluetooth is not available"
            } else {
                statusBluetooth.text = "Bluetooth is available"
            }
            if(blueAdapter!!.isEnabled){
                imgBluetooth.setImageResource(R.drawable.ic_action_bt_on)
                if(inputStream!= null) connected = true
            } else {
                imgBluetooth.setImageResource(R.drawable.ic_action_bt_off)
            }
        }
    }

    fun sendRecord(subject : String?){
        val smsManager = SmsManager.getDefault() as SmsManager
        smsManager.sendTextMessage(userData?.phoneNumber, null, "$subject", null, null)

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

    class AsyncTaskExample(var context: Context, var socket: BluetoothSocket?, var outputStream: OutputStream?, var inputStream: InputStream?, var targetedDevice: BluetoothDevice?
                            , var PORT_UUID : UUID) : AsyncTask<Void, Void, Boolean>() {
        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg p0: Void?): Boolean {
            var connected  = false
            try {
                socket = targetedDevice!!.createRfcommSocketToServiceRecord(PORT_UUID)
                socket!!.connect()
                Log.d("TagCheck", "Bluetooth's socket is ready!")
                connected = true
            } catch (e : IOException){
                e.printStackTrace()
                connected = false
            }
            if(connected){
                try{
                    outputStream = socket?.outputStream
                    inputStream = socket?.inputStream
                    if(outputStream == null)
                    Log.d("TagCheckOS", "null") else Log.d("TagCheckOS", "!!!! no null")
                    if(inputStream == null)
                        Log.d("TagCheckIS", "null") else Log.d("TagCheckIS", "!!!! no null")
                } catch (e : IOException){
                    e.printStackTrace()
                }
            }
            return connected
        }

        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
        }
    }

    fun BluetoothStart()  {
        if (!blueAdapter!!.isEnabled()) {
            showToast("Turning On Bluetooth...")
            //intent to on bluetooth
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, REQUEST_ENABLE_BLUETOOTH)
        } else {
            showToast("Bluetooth is already on")
        }
    }

    fun BluetoothEnd(){
        outputStream?.close()
        inputStream?.close()
        connected = false
        if(blueAdapter!!.isEnabled){
            blueAdapter!!.disable()
            showToast("Turning bluetooth off!")
            imgBluetooth.setImageResource(R.drawable.ic_action_bt_off)
        } else {
            showToast("Bluetooth is already off!")
        }
    }

    fun BluetoothDiscover(){
        if(!blueAdapter!!.isDiscovering){
            showToast("Making your device discoverable")
            var intent : Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
            startActivityForResult(intent, REQUEST_DISCOVER_BT)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun BluetoothPairedDevice(){
        if(blueAdapter!!.isEnabled){
            var devices : Set<BluetoothDevice> = blueAdapter!!.bondedDevices
            pairedBluetooth.text = "Paired Deviced"
            if(devices.isEmpty()){
                showToast("Not found paired deviced!")
            } else {
                var isFound = false
                showToast("${devices.size}")
                for (device in devices) {
                    if (device!!.address == DEVICE_ADDRESS) {
                        targetedDevice = device
                        pairedBluetooth.text = "Target Device: ${device!!.name}"
                        showToast("Found the targeted device!")
                        isFound = true
                        connected = true
                    }
                }
                if(isFound){
                    if(BluetoothSetUpSocket()) connected = true
                }
            }

        } else {
            showToast("Turn on bluetooth to get paired devices")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun BluetoothSetUpSocket() : Boolean {
        var isConnect = false
        try {
            socket = targetedDevice!!.createRfcommSocketToServiceRecord(PORT_UUID)
            blueAdapter!!.cancelDiscovery()
            socket!!.connect()
            showToast("Bluetooth's socket is ready!")
            isConnect = true
        } catch (e : IOException){
            e.printStackTrace()
            isConnect = false
        }
        var blueThread : ConnectedThread = ConnectedThread(socket!!)
        blueThread.run()
        /*if(isConnect){
            try{
                outputStream = socket?.outputStream
                inputStream = socket?.inputStream
            } catch (e : IOException){
                e.printStackTrace()
            }
            if(outputStream == null)
                Log.d("TagCheckOS", "null") else Log.d("TagCheckOS", "!!!! no null")
            if(inputStream == null)
                Log.d("TagCheckIS", "null") else Log.d("TagCheckIS", "!!!! no null")
            if(outputStream != null) {
                val available = inputStream?.available()
                val bytes = available?.let { ByteArray(it) }
                Log.d("server", "Reading")
                available?.let { inputStream!!.read(bytes, 0, it) }
                val text = bytes?.let { String(it) }
                text?.let { Log.d("server_", it) }
                if(text!= null) Log.d("server_receiver", text)
                else Log.d("server_null", "text is null")
                if(text?.contains(entryOut)!!){
                    val indexStart = text?.indexOf(entryOut)
                    val indexEnd = text?.indexOf(":");
                    val trimOut =  text?.substring(indexStart + 8, indexEnd -1)
                    numOut = trimOut.toInt()
                }
                if(text!!.contains(entryIn)!!){
                    val indexStart = text?.indexOf(entryIn)
                    val indexEnd = text?.indexOf("_")
                    val trimIn = text?.substring(indexStart+ 7, indexEnd -1 )
                    numIn = trimIn.toInt()
                }
                if(text!!.contains("Security")!!){
                    securityStatus = false
                }
                statusIRout.text = "OUNT: $numOut"
                statusIRin.text = "IN: $numIn"
                val current = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                val formatted = current.format(formatter)
                statusIRecord.text = "Record until $formatted"

                lightOne.setOnClickListener {
                    showToast("Light 1 is click!")
                    command = when(lightOneStatus){
                        false -> {
                            statusLightOne.text = "ON"
                            ImageViewCompat.setImageTintList(imgLightOne, ColorStateList.valueOf(resources.getColor(R.color.green_100)))
                            "1"
                        }
                        true -> {
                            statusLightOne.text = "OFF"
                            ImageViewCompat.setImageTintList(imgLightOne, ColorStateList.valueOf(resources.getColor(R.color.colorAccent)))
                            "2"
                        }

                    }
                    lightOneStatus = !lightOneStatus
                    try {
                        outputStream!!.write(command!!.toByteArray())
                        outputStream!!.flush()
                    } catch ( e : IOException){
                        e.printStackTrace()
                    }
                    Log.d("TagOutputstream", command!!)

                }
                lightTwo.setOnClickListener{
                    command = when(lightTwoStatus){
                        false -> {
                            statusLightTwo.text = "ON"
                            ImageViewCompat.setImageTintList(imgLightTwo, ColorStateList.valueOf(resources.getColor(R.color.green_100)))
                            "3"
                        }
                        true -> {
                            statusLightTwo.text = "OFF"
                            ImageViewCompat.setImageTintList(imgLightTwo, ColorStateList.valueOf(resources.getColor(R.color.colorAccent)))
                            "4"
                        }
                    }
                    lightTwoStatus = !lightTwoStatus
                    try {
                        outputStream!!.write(command!!.toByteArray())
                        outputStream!!.flush()
                    }catch (e : IOException){
                        e.printStackTrace()
                    }
                    Log.d("TagOutputstream", command!!)
                }

                refrigerator.setOnClickListener {
                    command = when(refrigeratorStatus){
                        false -> {
                            statusRefrigerator.text = "ON"
                            ImageViewCompat.setImageTintList(imgRefrigerator, ColorStateList.valueOf(resources.getColor(R.color.teal_200)))
                            "5"
                        }
                        true -> {
                            statusRefrigerator.text = "OFF"
                            ImageViewCompat.setImageTintList(imgRefrigerator, ColorStateList.valueOf(resources.getColor(R.color.colorAccent)))
                            "6"
                        }
                    }
                    refrigeratorStatus = !refrigeratorStatus
                    try {
                        outputStream!!.write(command!!.toByteArray())
                        outputStream!!.flush()
                    } catch (e : IOException){
                        e.printStackTrace()
                    }
                    Log.d("TagOutputstream", command!!)
                }
                if(!securityStatus)
                    ImageViewCompat.setImageTintList(imgLightOne, ColorStateList.valueOf(resources.getColor(R.color.colorAccent)))
                if(!securityStatus && isBound) {
                    // sendRecord("There is a serious alert from anti-theft system! Please check your camera at $formatted as soon as possible!")
                    //composeEmail(userData?.email, "There is a serious alert from anti-theft system! Please check your camera at $formatted as soon as possible!")
                    //call()
                    context?.let { alerService!!.setContext(it) }
                    // if(userData?.isConnectMes!!)
                    if(isMes)
                        alerService!!.sendRecord("There is a serious alert from anti-theft system! Please check your camera at $formatted as soon as possible!")
                    //if(userData?.isConnectMail!!)
                    if(isEmail)
                        alerService!!.composeEmail(email, "There is a serious alert from anti-theft system! Please check your camera at $formatted as soon as possible!")
                    // if(userData?.isConnectPhone!!)
                    if(isPhone)
                        alerService!!.call()
                }
                security.setOnClickListener {
                    if(!securityStatus) {
                        command = "T"
                        securityStatus = true
                        ImageViewCompat.setImageTintList(imgLightOne, ColorStateList.valueOf(resources.getColor(R.color.teal_200)))
                        try {
                            outputStream!!.write(command!!.toByteArray())
                            outputStream!!.flush()
                        } catch (e : IOException){
                            e.printStackTrace()
                        }
                    }
                }
            }
        }*/
        return isConnect
    }

    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {
        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

        override fun run() {
            var numBytes: Int // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                // Read from the InputStream.
                numBytes = try {
                    mmInStream.read(mmBuffer)
                } catch (e: IOException) {
                    break
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        fun write(bytes: ByteArray) {
            try {
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                Log.e("server", "Error occurred when sending data", e)

                // Send a failure message back to the activity.
                val bundle = Bundle().apply {
                    putString("toast", "Couldn't send data to the other device")
                }
                return
            }
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e("server", "Could not close the connect socket", e)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_ENABLE_BLUETOOTH -> if (resultCode == RESULT_OK) {
                //bluetooth is on
                imgBluetooth.setImageResource(R.drawable.ic_action_bt_on)
                showToast("Bluetooth is on!")
            } else {
                //user denied to turn bluetooth on
                showToast("could not on bluetooth")
            }
            REQUEST_DISCOVER_BT -> {
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
    private fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ControlFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}