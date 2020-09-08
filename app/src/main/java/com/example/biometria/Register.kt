package com.example.biometria

import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*
import org.jetbrains.anko.toast
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random

class Register: AppCompatActivity() {
    private val wordList= listOf<String>("kawał", "banan", "piłka", "mama","tata","słońce","szkoła","miska")
    private val r= Random.nextInt(0,wordList.size)
    private lateinit var wordOne: String
    private lateinit var wordTwo: String
    private var wordOneRegistered=false
    private var wordTwoRegistered=false
    private var buttonOneClicks=0
    private var buttonTwoClicks=0
    private var output: String? = null
    private val verification=Verification(this)
    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false
    private lateinit var encoded:ByteArray
    private var recordNumber=1
    private var jsonToSend= JSONObject()


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        wordOne=wordList[r]
        val r1= Random.nextInt(0,wordList.size)
        wordTwo=wordList[r1]
        if(wordTwo == wordOne) {
            val r1= Random.nextInt(0,wordList.size)
            wordTwo=wordList[r1]
        }
        verification.setMedia()
        /*mediaRecorder = MediaRecorder()
        output = Environment.getExternalStorageDirectory().absolutePath + "/recording$recordNumber.mp3"
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder?.setOutputFile(output)*/
        word_one.text="Słowo numer 1: $wordOne"
        word_two.text="Słowo numer 2: $wordTwo"
        register_button.setOnClickListener {
            register()
            finish()
        }
        already_text.setOnClickListener {
            finish()
        }
        button_record_word_one.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN ->
                    if(!wordOneRegistered) {
                        if (buttonOneClicks >= 9) {
                            wordOneRegistered = true
                        }
                        val username=username_register.text.toString()
                        verification.putExtraPost("username",username)
                        verification.putExtraPost("word",wordOne)
                        verification.startRecording()
                        Log.d("joooo", "wcisnął")
                        //startRecording()
                        recordNumber++
                        buttonOneClicks++

                    }
                MotionEvent.ACTION_UP ->{
                    verification.stopRecording()
                    //stopRecording()
                    Thread{
                        verification.sendPostRequest()
                        //sendPostRequest()
                        runOnUiThread {
                        }
                    }.start()
            }}
            v?.onTouchEvent(event) ?: true
        }
        button_record_word_two.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN ->
                    if(!wordTwoRegistered) {
                        if(buttonTwoClicks>=9) {
                            wordTwoRegistered = true
                        }
                        val username=username_register.text.toString()
                        verification.putExtraPost(username,wordTwo)
                        verification.startRecording()
                        //startRecording()
                        recordNumber++
                        buttonTwoClicks++
                    }
                MotionEvent.ACTION_UP ->{
                    verification.stopRecording()
                    Thread{
                        verification.sendPostRequest()
                        //sendPostRequest()
                        runOnUiThread {
                        }
                    }.start()
                    //verification.stop

                    //stopRecording()
            }}

            v?.onTouchEvent(event) ?: true
        }
    }
    private fun register(){
        val username=username_register.text.toString()
        val email=email_register.text.toString()
        val password=password_register.text.toString()
        if(email.isEmpty() || password.isEmpty()){
            toast( "Pola nie mogą być puste")
            return
        }
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if(!it.isSuccessful) return@addOnCompleteListener
                else{
                    toast("Utworzono nowego użytkownika")
                }
            }
            .addOnFailureListener {
                toast("Nie udało się utworzyć użytkownika: $it")
            }

        val ref= FirebaseDatabase.getInstance().getReference("/users")
        val uid=ref.push().key
        val user= User(username, email, wordOne, wordTwo)
        if (uid != null) {
            ref.child(uid).setValue(user)
        }
        /*Thread{
            verification.sendPostRequest()
            //sendPostRequest()
            runOnUiThread {
            }
        }.start()*/
    }

    /*private fun stopRecording() {
        toast("koniec")
        if(state){
            mediaRecorder?.stop()
            mediaRecorder?.release()
            state = false
        }
        encoded= File(output).readBytes()
        Log.d("jooooooo",encoded.toString().toFloat().toString())
        jsonToSend.put("audio", encoded)
        Thread{
            sendPostRequest()
            runOnUiThread {
            }
        }.start()

    }

    private fun startRecording() {
        toast("mów")
        try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            state = true
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    private fun sendPostRequest() {

        val mURL = URL("http://e58bd460.ngrok.io/roadsystem/accident")

        with(mURL.openConnection() as HttpURLConnection) {
            // optional default is GET
            requestMethod = "POST"
            val wr = OutputStreamWriter(outputStream)
            wr.write(jsonToSend.toString())
            wr.flush()

            println("URL : $url")
            println("Response Code : $responseCode")

            BufferedReader(InputStreamReader(inputStream)).use {
                val response = StringBuffer()

                var inputLine = it.readLine()
                while (inputLine != null) {
                    response.append(inputLine)
                    inputLine = it.readLine()
                }
                it.close()
                println("Response : $response")
            }
        }
    }*/
    class User(val username: String, val email:String, val wordOne: String, val wordTwo:String)
}