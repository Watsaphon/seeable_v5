package com.estazo.project.seeable.app

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.estazo.project.seeable.app.R.layout
import com.estazo.project.seeable.app.helperClass.Locations
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.alert_dialog_bus_sign_detection.view.*
import kotlinx.android.synthetic.main.alert_dialog_crosswalk_detection.view.*
import kotlinx.android.synthetic.main.alert_dialog_fall_dectection.view.*
import java.util.*


class MainActivity : AppCompatActivity(), UpdateMyText {


    private lateinit var  mAlertDialog : AlertDialog
    private var textToSpeech: TextToSpeech? = null

    private lateinit var database: DatabaseReference
    private lateinit var listener: ValueEventListener

    private lateinit var sharedPrefPhone: SharedPreferences

    private val viewModel : UserTypeViewModel by viewModels()

    var msg: String? = ""

    //Declaring the needed Variables
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    val PERMISSION_ID = 1010
    private  var RC_OVERLAY = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)
        hideSystemUI()
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        Log.i("MainActivity", "onCreate called ")
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, IntentFilter("Msg"))

        sharedPrefPhone = getSharedPreferences("value", 0)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        viewModel.userType.observe(this, Observer<String> { typeView ->
            Log.d("checkUser_Main","type : $typeView")
            when(typeView){
                "not found!" -> { Toast.makeText(this, "Login Section",Toast.LENGTH_LONG).show() }
                "caretaker" -> {
                    sharedPrefPhone = getSharedPreferences("value", 0)
                    val phone = sharedPrefPhone.getString("stringKeyPhone", "not found!").toString()
                    Toast.makeText(this, "Caretaker Section",Toast.LENGTH_LONG).show()
                    FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            Log.d("testNotification", "Fetching FCM registration token failed", task.exception)
                            return@OnCompleteListener
                        }
                        // Get new FCM registration token
                        val token = task.result
                        // Log and toast
                        val msg = getString(R.string.msg_token_fmt, token)
                        Log.d("testNotification", msg)
                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                        val ref = FirebaseDatabase.getInstance().reference
                        val childUpdates = hashMapOf<String, Any>("users_caretaker/$phone/FCM" to "$token")
                        ref.updateChildren(childUpdates)
                    })
                }
                "blind" -> {
                    blindSection()
                    Toast.makeText(this, "Blind Section",Toast.LENGTH_LONG).show()
                    FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            Log.d("testNotification", "Fetching FCM registration token failed", task.exception)
                            return@OnCompleteListener
                        }
                        // Get new FCM registration token
                        val token = task.result
                        // Log and toast
                        val msg = getString(R.string.msg_token_fmt, token)
                        Log.d("testNotification", msg)
                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
//                        val ref = FirebaseDatabase.getInstance().reference
//                        val childUpdates = hashMapOf<String, Any>("users_caretaker/$phone/FCM" to "$token")
//                        ref.updateChildren(childUpdates)
                    })
                }
            }
        })

    }

    private val onNotice: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val pack = intent.getStringExtra("package")
            val title = intent.getStringExtra("title")
            val text = intent.getStringExtra("text")
            Log.i("notificationServiceMain","pack :$pack , title : $title , text : $text ")
        }
    }

    override fun onStart() {
        super.onStart()
        Log.i("MainActivity", "onStart called")
    }
    override fun onRestart() {
        super.onRestart()
        Log.i("MainActivity", "onRestart called")
    }
    override fun onResume() {
        super.onResume()
        Log.i("MainActivity", "onResume called")
        updateUI()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            openOverlaySettings()
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun openOverlaySettings() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")
        )
        try {
            startActivityForResult(intent, RC_OVERLAY)
        } catch (e: ActivityNotFoundException) {
            Log.e("openOverlaySettings", e.message!!)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

    }

    override fun onPause() {
        super.onPause()
        Log.i("MainActivity", "onPause called")
    }

    override fun onStop() {
        super.onStop()
        Log.i("MainActivity", "onStop called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("MainActivity", "onDestroy called")
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        hideSystemUI()
        Log.d("MainActivity", "onWindowFocusChanged called")
    }

    /** hide navigation and status bar in each activity */
    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    private fun updateUI() {
        val decorView = window.decorView
        decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            }
        }
    }

    fun closeKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun alertDialogFallDetection() {
        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this).inflate(layout.alert_dialog_fall_dectection, null)
        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
        //show dialog
        mAlertDialog  = mBuilder.show()
        mAlertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        mAlertDialog.setCanceledOnTouchOutside(false)
        mAlertDialog.setCancelable(false)
        textToSpeech = TextToSpeech(this, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech!!.language = Locale.US
                textToSpeech!!.speak(getString(R.string.fallDetection_info2), TextToSpeech.QUEUE_FLUSH, null)
            }
        })
        textToSpeech!!.setSpeechRate(0.9f)

        mDialogView.fallDetection.setOnVeryLongClickListener{
            vibrate()
            val tts = getString(R.string.fallDetection_fine)
            textToSpeech!!.speak(tts, TextToSpeech.QUEUE_FLUSH, null)
            mAlertDialog.dismiss()
            val ref = FirebaseDatabase.getInstance().reference
            val sharedPrefPhone = getSharedPreferences("value", 0)
            val phone = sharedPrefPhone.getString("stringKeyPhone","not found!")
            val childUpdates = hashMapOf<String, Any>("users_blind/$phone/Device/fall_Detection" to "no")
            ref.updateChildren(childUpdates)
        }
        Handler().postDelayed({
            if (mAlertDialog.isShowing) {
                mAlertDialog.dismiss()
                Log.i("testfd","no cancel ja")
                getLastLocation()
                sendLocation()
                val ref = FirebaseDatabase.getInstance().reference
                val sharedPrefPhone = getSharedPreferences("value", 0)
                val phone = sharedPrefPhone.getString("stringKeyPhone","not found!")
                val childUpdates = hashMapOf<String, Any>("users_blind/$phone/Device/fall_Detection" to "no")
                ref.updateChildren(childUpdates)
            }
            else{
                Log.i("testfd","cancel ja")
            }
        }, 30000) //change 5000 with a specific time you want

//
//        val mDialogView = LayoutInflater.from(this).inflate(layout.alert_dialog_fall_dectection, null)
//        val dialogBuilder = AlertDialog.Builder(this).setView(mDialogView)
//
//
//        Log.i("dialog","dialog call")
//        val dialog: AlertDialog = dialogBuilder.create()
//        val dialogWindow: Window = dialog.window!!
//        val dialogWindowAttributes: WindowManager.LayoutParams = dialogWindow.attributes
//
//        val lp = WindowManager.LayoutParams()
//        lp.copyFrom(dialogWindowAttributes)
//        lp.width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 280f, resources.displayMetrics).toInt()
//        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
//        dialogWindow.attributes = lp
//
//        dialogWindow.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
//        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
//        dialog.show()
//
//        dialog.setCanceledOnTouchOutside(false)
//        dialog.setCancelable(false)
//        textToSpeech = TextToSpeech(this, TextToSpeech.OnInitListener { status ->
//            if (status != TextToSpeech.ERROR) {
//                textToSpeech!!.language = Locale.US
//                textToSpeech!!.speak(getString(R.string.fallDetection_info2), TextToSpeech.QUEUE_FLUSH, null);
//            }
//        })
//
//        mDialogView.fallDetection.setOnVeryLongClickListener{
//            vibrate()
//            val tts = getString(R.string.fallDetection_fine)
//            textToSpeech!!.speak(tts, TextToSpeech.QUEUE_FLUSH, null)
//            val ref = FirebaseDatabase.getInstance().reference
//            val sharedPrefPhone = getSharedPreferences("value", 0)
//            val phone = sharedPrefPhone.getString("stringKeyPhone","not found!")
//            val childUpdates = hashMapOf<String, Any>("users_blind/$phone/Device/fall_Detection" to "no")
//            ref.updateChildren(childUpdates)
//            dialog.dismiss()
//        }
//
//        Handler().postDelayed({
//            if (dialog.isShowing) {
//                dialog.dismiss()
//                Log.i("testfd","no cancel ja")
//                getLastLocation()
//                sendLocation()
//            }
//            else{
//                Log.i("testfd","cancel ja")
//            }
//        }, 30000) //change 5000 with a specific time you want
    }


    /** function press and hold button for few seconds */
    private fun View.setOnVeryLongClickListener(listener: () -> Unit) {
        setOnTouchListener(object : View.OnTouchListener {
            private val longClickDuration = 2000L
            private val handler = Handler()
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event?.action == MotionEvent.ACTION_DOWN) {
                    handler.postDelayed({ listener.invoke() }, longClickDuration)
                } else if (event?.action == MotionEvent.ACTION_UP) {
                    handler.removeCallbacksAndMessages(null)
                }
                return true
            }
        })
    }

    /** function vibrations */
    private fun vibrate(){
        // Vibrate for 300 milliseconds
        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
        }
        else {
            //deprecated in API 26
            v.vibrate(300)
        }
    }

    private fun blindSection(){
        val sharedPrefPhone = getSharedPreferences("value", 0)
        val phone = sharedPrefPhone.getString("stringKeyPhone","not found!")
        viewModel.userType.observe(this, Observer<String> { typeView ->
            Log.d("checkUser_Main_BS","type : $typeView")
            when(typeView){
                "not found!" -> {
                    if(this::database.isInitialized){
                        database.child("users_blind/$phone/Device").removeEventListener(listener)
                        Log.d("checkUser_Main_BS","logout to Login section")
                    }
                }
                "caretaker" -> {
                    if(this::database.isInitialized){
                        database.child("users_blind/$phone/Device").removeEventListener(listener)
                        Log.d("checkUser_Main_BS","login to caretaker section")
                    }
                }

            }
        })
        /**For java*/
//        database = FirebaseDatabase.getInstance().reference
        /**For kotlin*/
//        database = Firebase.database.reference
        database = Firebase.database.reference
        listener = database.child("users_blind/$phone/Device").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                val fallDetections = dataSnapshot.child("fall_Detection").value.toString()
                Log.d("checkUser_Main_FD", "fallDetections : $fallDetections")
                    if (fallDetections == "fall") {
                        alertDialogFallDetection()
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("checkUser_Main_FD","onCancelled call")
            }
        })
    }

    private fun checkPermission():Boolean {
        //this function will return a boolean
        //true: if we have permission
        //false if not
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED||
            ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED||
            ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED||
            ActivityCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED||
            ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
    }
    private fun requestPermission(){
        //this function will allows us to tell the user to requesut the necessary permsiion if they are not garented
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CALL_PHONE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_ID)
    }

    /** Config location */
    @SuppressLint("MissingPermission")
    fun getLastLocation(){
        Log.d("Debug:" ,"getLastLocation call" )
        if(checkPermission()){
            if(isLocationEnabled()){
                fusedLocationProviderClient.lastLocation.addOnCompleteListener {task->
                    val location: Location? = task.result
                    if(location == null){
                        newLocationData()
                    }else{
                        Log.d("Debug:" ,"getLastLocation() -> Your Location : Long: "+ location.longitude + " , Lat: " + location.latitude )
                        val text = "You Current Location is : Long: "+ location.longitude + " , Lat: " + location.latitude + "\n"
                        Toast.makeText(this,"$text",Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                Toast.makeText(this,"Please Turn on Your device Location",Toast.LENGTH_SHORT).show()
            }
        }else{
            requestPermission()
        }
    }
    private fun newLocationData(){
        val locationRequest =  LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,locationCallback,Looper.myLooper()
        )
    }
    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            val lastLocation: Location = locationResult.lastLocation
            Log.d("Debug:","onLocationResult -> your last location: latitude = "+ lastLocation.latitude.toString()+", longitude = "+ lastLocation.longitude.toString())
            val text = "You Last Location is : Long: "+ lastLocation.longitude + " , Lat: " + lastLocation.latitude + "\n"

            Toast.makeText(this@MainActivity,"$text",Toast.LENGTH_SHORT).show()
        }
    }
    private fun isLocationEnabled():Boolean{
        //this function will return to us the state of the location service
        //if the gps or the network provider is enabled then it will return true otherwise it will return false
        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == PERMISSION_ID){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d("Debug:","onRequestPermissionsResult -> You have the Permission")
            }
        }
    }

    @SuppressLint("MissingPermission", "DefaultLocale")
    fun sendLocation(){
        Log.d("Debug_sendLocation:" ,"sendLocation call" )
        fusedLocationProviderClient.lastLocation.addOnCompleteListener {task->
            val location: Location? = task.result
            newLocationData()
            if(location == null){
                Log.i("Debug_sendLocation","call if")
                newLocationData()
            }else{
                val link = java.lang.String.format("%f,%f", location.latitude,location.longitude)
                Log.i("Debug_sendLocation","call else")
                Log.d("Debug_sendLocation:" ,"link : $link" )
                Log.d("Debug_sendLocation:" ,"location : $location" )

                val currentPhone = sharedPrefPhone.getString("stringKeyPhone", "not found!")

                Log.d("Debug_sendLocation","$currentPhone")

                val ref = FirebaseDatabase.getInstance().reference
                val postLocation =  Locations(location.latitude,location.longitude)
                val postValues = postLocation.toMap()
                val childUpdates = hashMapOf<String, Any>("users_blind/$currentPhone/Location" to postValues)
                ref.updateChildren(childUpdates)
            }
        }
    }

    fun alertDialogCrosswalkDetection() {
        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this).inflate(layout.alert_dialog_crosswalk_detection, null)
        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this).setView(mDialogView)
        //show dialog
        mAlertDialog  = mBuilder.show()
        mAlertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        mAlertDialog.setCanceledOnTouchOutside(false)
        mAlertDialog.setCancelable(false)

        textToSpeech = TextToSpeech(this, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech!!.language = Locale.US
                textToSpeech!!.speak(getString(R.string.crosswalkDetection_speak), TextToSpeech.QUEUE_FLUSH, null)
            }
        })
        textToSpeech!!.setSpeechRate(0.9f)

        mDialogView.crosswalkDetection.setOnVeryLongClickListener{
            vibrate()
//            val tts = getString(R.string.fallDetection_fine)
//            textToSpeech!!.speak(tts, TextToSpeech.QUEUE_FLUSH, null)
            mAlertDialog.dismiss()
        }

    }

    private fun alertDialogBusSignDetection() {
        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.alert_dialog_bus_sign_detection, null)
        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this).setView(mDialogView)
        //show dialog
        mAlertDialog  = mBuilder.show()
        mAlertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        mAlertDialog.setCanceledOnTouchOutside(false)
        mAlertDialog.setCancelable(false)

        textToSpeech = TextToSpeech(this, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech!!.language = Locale.US
                textToSpeech!!.speak(getString(R.string.busSignDetection_speak), TextToSpeech.QUEUE_FLUSH, null)
            }
        })
        textToSpeech!!.setSpeechRate(0.9f)

        mDialogView.busSignDetection.setOnVeryLongClickListener{
            vibrate()
//            val tts = getString(R.string.fallDetection_fine)
//            textToSpeech!!.speak(tts, TextToSpeech.QUEUE_FLUSH, null)
            mAlertDialog.dismiss()
        }

    }

    override fun onTextUpdate(string: String?) {
        if (string != null) {
            Log.d("onTextUpdate: ", string)
            updateText(string)
        }
    }

    private fun updateText(string: String?){
        msg = string
        when(msg) {
            "0.0" -> { alertDialogCrosswalkDetection() }
            "1.0"->{ alertDialogBusSignDetection() }
            else -> {Log.d("Score"," not found")}
        }
    }

}

interface UpdateMyText{
    fun onTextUpdate(string: String?)
}
