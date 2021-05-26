package com.estazo.project.seeable.app.device

import android.util.Log
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HealthStatusRunnable(val callback: TextView, private val currentBlindPhone : String) : Runnable {

    private lateinit var database: DatabaseReference
    private lateinit var listener: ValueEventListener

    override fun run() {
        Log.d("selectItemHealthStatus","currentBlindPhone : $currentBlindPhone")
        if(currentBlindPhone != "-"){
            database = Firebase.database.reference
            listener = database.child("users_blind/$currentBlindPhone/Device/health_Status").addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val status = dataSnapshot.value.toString()
                        Log.d("healthStatus_worker","status : $status")
                        callback.text = status
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }


}