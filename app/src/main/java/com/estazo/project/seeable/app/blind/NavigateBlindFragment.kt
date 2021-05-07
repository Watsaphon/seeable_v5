package com.estazo.project.seeable.app.blind

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.*
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.estazo.project.seeable.app.R
import com.estazo.project.seeable.app.databinding.FragmentNavigateBlindBinding
import com.estazo.project.seeable.app.objectDetection.TFLiteDetection
import kotlinx.android.synthetic.main.alert_dialog_bus_sign_detection.view.*
import kotlinx.android.synthetic.main.alert_dialog_crosswalk_detection.view.*
import java.util.*

class NavigateBlindFragment : Fragment() {


    private lateinit var binding: FragmentNavigateBlindBinding
    private lateinit var mAlertDialog: AlertDialog
    private var textToSpeech: TextToSpeech? = null
    private lateinit var sharedPrefNavigate: SharedPreferences

    private var alert: Boolean = false

    private lateinit var viewModel: NavigateBlindViewModel
    private var isAlreadyFound = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("NavigateBlindFragment", "onCreate call")
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // in here you can do logic when backPress is clicked
                sharedPrefNavigate = requireActivity().getSharedPreferences("value", 0)
                val editor = sharedPrefNavigate.edit()
                editor.putString("stringKeyNavigate", "not found!")
                editor.apply()
                val launchIntent =
                    requireActivity().packageManager.getLaunchIntentForPackage("com.estazo.project.seeable.app")
                startActivity(launchIntent)

            }
        })

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_navigate_blind, container, false)
        Log.i("NavigateBlindFragment", "onCreateView call")



        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.i("NavigateBlindFragment", "onActivityCreated call")

    }

    override fun onStart() {
        super.onStart()
        Log.i("NavigateBlindFragment", "onStart call")


    }

    override fun onResume() {
        super.onResume()
//        Log.i("NavigateBlindFragment", "onResume call detect = $detect ")
//        alertDialogCrosswalkDetection()

//        binding.camera.open()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //        viewModel = ViewModelProviders.of(this).get(NavigateBlindViewModel::class.java)
        viewModel = ViewModelProvider(this).get(NavigateBlindViewModel::class.java)
        var a = viewModel.detect.hasActiveObservers()
        var b = viewModel.detect.hasObservers()
        viewModel.detectDialog().observe(viewLifecycleOwner, Observer<String> { detect ->
            Log.i("NavigateBlindFragment", "viewModel detect = $detect")
            binding.test.text = " detect si wa"
            if (detect == "0.0") {
                binding.test.text = detect
                alertDialogCrosswalkDetection()
            } else if (detect == "1.0") {
                binding.test.text = detect
                alertDialogBusSignDetection()
            }
        })
        a = viewModel.detect.hasActiveObservers()
        b = viewModel.detect.hasObservers()


//        binding.camera.setLifecycleOwner(viewLifecycleOwner)
        val tfLiteDetection = TFLiteDetection(requireContext()) {
            receiveIMG(it)
        }
        binding.camera.addFrameProcessor { frame ->
            TFLiteDetection(requireContext(), onDetect = {
                receiveIMG(it)
            }).detect(frame)
//            tfLiteDetection.detect(frame)
        }

        binding.camera.open()
        a = viewModel.detect.hasActiveObservers()
        b = viewModel.detect.hasObservers()

    }

    fun onDetect(s: String) {
        receiveIMG(s)
    }

    override fun onPause() {
        super.onPause()
        Log.i("NavigateBlindFragment", "onPause call")
        binding.camera.close()
    }

    override fun onStop() {
        super.onStop()
        Log.i("NavigateBlindFragment", "OnStop call")

    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i("NavigateBlindFragment", "onDestroyView call")
        binding.camera.destroy()

    }

    fun receiveIMG(msg: String) {
       val a = viewModel.detect.hasActiveObservers()
       val b = viewModel.detect.hasObservers()
        if (msg != null) {
            Log.i("Score", "receiveIMG msg = $msg")
//                viewModel.detect.value = "$msg"
            checkDetection("$msg")
//                detect = msg
//                when (msg) {
//                            "0.0" -> {
//                                Log.i("Score","in when msg = $msg ")
//                                checkDetection("crosswalk")
//                                alertDialogCrosswalkDetection()
//                            }
//                            "1.0"->{
//                                Log.i("Score","in when msg = $msg ")
//                                checkDetection("bussign")
//                                alertDialogBusSignDetection()
//                            }
//                }
        }
    }

    private fun checkDetection(detect: String) {
        viewModel.detect.postValue(detect)
    }

    private fun alertDialogBusSignDetection() {
        Toast.makeText(requireContext(), "asdfasdfasdf", Toast.LENGTH_SHORT).show()
        Log.i("checkdt", "in alert")
        //Inflate the dialog with custom view
        val mDialogView =
            LayoutInflater.from(requireContext())
                .inflate(R.layout.alert_dialog_bus_sign_detection, null)
        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(requireContext()).setView(mDialogView)
        //show dialog
        Log.i("checkdt", "set view already")

        mAlertDialog = mBuilder.show()
        Log.i("checkdt", "show ja")
        mAlertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        mAlertDialog.setCanceledOnTouchOutside(false)
        mAlertDialog.setCancelable(false)
        mAlertDialog.show()
        Log.i("checkdt", "set other")

        textToSpeech = TextToSpeech(context, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech!!.language = Locale.US
                textToSpeech!!.speak(
                    getString(R.string.busSignDetection_speak),
                    TextToSpeech.QUEUE_FLUSH,
                    null
                )
            }
        })
        Log.i("checkdt", "set tts")

        textToSpeech!!.setSpeechRate(0.9f)
        Log.i("checkdt", "use tts")

        mDialogView.busSignDetection.setOnVeryLongClickListener {
            vibrate()
//            val tts = getString(R.string.fallDetection_fine)
//            textToSpeech!!.speak(tts, TextToSpeech.QUEUE_FLUSH, null)
            mAlertDialog.dismiss()

            alert = false

        }
        Log.i("checkdt", "end ja")


//        val alertDialogBuilder = AlertDialog.Builder(context)
//        alertDialogBuilder.setTitle("Your Title")
//
//        alertDialogBuilder
//            .setMessage("Click yes to exit!")
//            .setCancelable(false)
//            .setPositiveButton("Yes") { dialog, id -> // if this button is clicked, close
//                // current activity
//                this@NavigateBlindFragment.requireActivity().finish()
//            }
//            .setNegativeButton("No") { dialog, id -> // if this button is clicked, just close
//                // the dialog box and do nothing
//                dialog.cancel()
//            }
//
//        // create alert dialog
//        val alertDialog = alertDialogBuilder.create()
//
//        // show it
//        alertDialog.show()

    }

    private fun alertDialogCrosswalkDetection() {
        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(requireActivity())
            .inflate(R.layout.alert_dialog_crosswalk_detection, null)
        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(requireContext()).setView(mDialogView)
        //show dialog
        val mAlertDialog = mBuilder.show()
        mAlertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        mAlertDialog.setCanceledOnTouchOutside(false)
        mAlertDialog.setCancelable(false)

        textToSpeech = TextToSpeech(requireContext(), TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech!!.language = Locale.US
                textToSpeech!!.speak(
                    getString(R.string.crosswalkDetection_speak),
                    TextToSpeech.QUEUE_FLUSH,
                    null
                )
            }
        })
        textToSpeech!!.setSpeechRate(0.9f)

        mDialogView.crosswalkDetection.setOnVeryLongClickListener {
            vibrate()
//            val tts = getString(R.string.fallDetection_fine)
//            textToSpeech!!.speak(tts, TextToSpeech.QUEUE_FLUSH, null)
            mAlertDialog.dismiss()
            alert = false
        }

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
    private fun vibrate() {
        // Vibrate for 300 milliseconds
        val v = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            //deprecated in API 26
            v.vibrate(300)
        }
    }
}

