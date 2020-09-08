package com.example.biometria

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.biometria.Recognition.DataProcessor
import com.example.biometria.Recognition.Labels
import com.example.biometria.Recognition.Prediction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity() {

    var email=""
    var uid = ""
    private lateinit var wordOne: String
    private lateinit var wordTwo: String
    private val verification=Verification(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this, permissions,0)}
        verification.setMedia()
        setContentView(R.layout.activity_main)
        login_button.setOnClickListener {
            signIn()
        }
        not_yet_text.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }
        button_record_word_one.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> verification.startRecording()
                MotionEvent.ACTION_UP -> {
                    val audio=verification.stopRecordingPredict()
                    val processor =DataProcessor()
                    val samples = processor.processData(audio)
                    val prediction = Prediction()
                    prediction.fetchInterpreter(this)
                    val bestResult = prediction.predict(samples)

                    val label = Labels.values()[bestResult].name
                    Log.d("jooo", label)
                    toast("Znalezione słowo: $label")
                    if(label==wordOne){
                        toast("Poprawnie zidentyfiokwano pierwsze słowo")
                    }
                }
            }
            v?.onTouchEvent(event) ?: true
        }
        button_record_word_two.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> verification.startRecording()
                MotionEvent.ACTION_UP -> {
                    val audio=verification.stopRecordingPredict()
                    val processor =DataProcessor()
                    val samples = processor.processData(audio)
                    val prediction = Prediction()
                    prediction.fetchInterpreter(this)
                    val bestResult = prediction.predict(samples)

                    val label = Labels.values()[bestResult].name
                    Log.d("jooo", label)
                    toast("Znalezione słowo: $label")
                    if(label==wordTwo){
                        toast("Poprawnie zidentyfiokwano drugie słowo")
                    }
                }            }
            v?.onTouchEvent(event) ?: true
        }
    }

    private fun signIn() {
        var ref = FirebaseDatabase.getInstance().reference.child("users")
        email = email_login.text.toString()
        val password = password_login.text.toString()
        if (email.isEmpty() || password.isEmpty()) {
            toast( "Pola nie mogą być puste")

        }
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("jooo", dataSnapshot.value.toString())
                getEmails(dataSnapshot.value as Map<String, Any>)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("Main", "loadPost:onCancelled", databaseError.toException()!!)
            }
        }
        ref.addValueEventListener(postListener)
        toast("Czekam na połączenie z serwerem")
        Log.d("Main", "ref=$ref")
        //verification.sendPostRequest()
    }

    private fun getEmails(map: Map<String, Any>) {
        val password = password_login.text.toString()
        val auth = FirebaseAuth.getInstance()
        Log.d("jooo", "idzie mail")
        Log.d("joooo", email)
        for (entry in map.entries) {
            val singleUser = entry.value as Map<*, *>
            Log.d("jooo", singleUser["email"] as String)
            if ((singleUser["email"] as String) == email) {
                Log.d("jooo", "znalazło")
                uid=entry.key
                wordOne=singleUser["wordOne"] as String
                wordTwo=singleUser["wordTwo"] as String
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            toast("Pomyślnie zalogowano, należy nagrać słowa")
                            button_record_word_one.visibility= View.VISIBLE
                            button_record_word_two.visibility=View.VISIBLE
                        }
                    }
                    .addOnFailureListener() {
                        toast("Niepoprawny email lub hasło")
                    }
            }
        }
    }
}