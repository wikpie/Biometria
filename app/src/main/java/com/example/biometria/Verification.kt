package com.example.biometria

import android.annotation.TargetApi
import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import cafe.adriel.androidaudioconverter.AndroidAudioConverter
import cafe.adriel.androidaudioconverter.callback.IConvertCallback
import cafe.adriel.androidaudioconverter.callback.ILoadCallback
import cafe.adriel.androidaudioconverter.model.AudioFormat
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL


class Verification(context: Context) {
    private var output: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false
    private lateinit var encoded:ByteArray
    private var recordNumber=1
    private var jsonToSend= JSONObject()
    private val context=context
    fun setMedia(){
        mediaRecorder = MediaRecorder()
        output = Environment.getExternalStorageDirectory().absolutePath + "/recording$recordNumber.mp3"
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder?.setAudioSamplingRate(44100)
        mediaRecorder?.setAudioEncodingBitRate(320000)
        mediaRecorder?.setAudioChannels(2)
        mediaRecorder?.setOutputFile(output)
        AndroidAudioConverter.load(context, object : ILoadCallback {
            override fun onSuccess() { // Great!
            }

            override fun onFailure(error: Exception) { // FFmpeg is not supported by device
            }
        })
    }
    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(Build.VERSION_CODES.O)
    fun stopRecording() {
        if(state){
            mediaRecorder?.stop()
            mediaRecorder?.release()
            state = false
        }
        var file=File(Environment.getExternalStorageDirectory().absolutePath + "/recording$recordNumber.mp3")
        val callback: IConvertCallback = object : IConvertCallback {
            override fun onSuccess(convertedFile: File?) { // So fast? Love it!
                file=convertedFile!!
            }

            override fun onFailure(error: java.lang.Exception) { // Oops! Something went wrong
            }
        }
        AndroidAudioConverter.with(context) // Your current audio file
            .setFile(file) // Your desired audio format
            .setFormat(AudioFormat.WAV) // An callback to know when conversion is finished
            .setCallback(callback) // Start conversion
            .convert()
        file=File(Environment.getExternalStorageDirectory().absolutePath + "/recording$recordNumber.wav")
        encoded= file.readBytes()
        Log.d("jooooooo", file.toString())
        Log.d("jooooooo", encoded.contentToString())
        var jo=java.util.Base64.getEncoder().encodeToString(encoded)
        Log.d("jooooooo", jo)
        jsonToSend.put("number",recordNumber)
        jsonToSend.put("audio", jo)
        recordNumber++
        mediaRecorder = MediaRecorder()
        output = Environment.getExternalStorageDirectory().absolutePath + "/recording$recordNumber.mp3"
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder?.setAudioSamplingRate(44100)
        mediaRecorder?.setAudioEncodingBitRate(320000)
        mediaRecorder?.setAudioChannels(2)
        mediaRecorder?.setOutputFile(output)

    }
    fun stopRecordingPredict(): ByteArray {
        if(state){
            mediaRecorder?.stop()
            mediaRecorder?.release()
            state = false
        }
        var file=File(Environment.getExternalStorageDirectory().absolutePath + "/recording$recordNumber.mp3")
        val callback: IConvertCallback = object : IConvertCallback {
            override fun onSuccess(convertedFile: File?) { // So fast? Love it!
                file=convertedFile!!
            }

            override fun onFailure(error: java.lang.Exception) { // Oops! Something went wrong
            }
        }
        AndroidAudioConverter.with(context) // Your current audio file
            .setFile(file) // Your desired audio format
            .setFormat(AudioFormat.WAV) // An callback to know when conversion is finished
            .setCallback(callback) // Start conversion
            .convert()
        file=File(Environment.getExternalStorageDirectory().absolutePath + "/recording$recordNumber.wav")
        encoded= file.readBytes()
        Log.d("jooooooo", file.toString())
        Log.d("jooooooo", encoded.contentToString())
        recordNumber++
        mediaRecorder = MediaRecorder()
        output = Environment.getExternalStorageDirectory().absolutePath + "/recording$recordNumber.mp3"
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setAudioSamplingRate(44100)
        mediaRecorder?.setAudioEncodingBitRate(320000)
        mediaRecorder?.setAudioChannels(2)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder?.setOutputFile(output)
        return encoded
    }
    fun startRecording() {
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
    fun convertToBase64(attachment: File): String {
        return Base64.encodeToString(attachment.readBytes(), Base64.NO_WRAP)
    }
    fun sendPostRequest() {

        val mURL = URL("http://33ef6894.ngrok.io/Recognition")

        with(mURL.openConnection() as HttpURLConnection) {
            // optional default is GET
            requestMethod = "POST"
            Log.d("jooooooo", jsonToSend.toString())
            val wr = OutputStreamWriter(outputStream)
            wr.write(jsonToSend.toString())
            wr.flush()
            jsonToSend.remove("number")
            jsonToSend.remove("username")
            jsonToSend.remove("audio")
            jsonToSend.remove("word")
            println("URL : $url")
            println("Response Code : $responseCode")

            /*BufferedReader(InputStreamReader(inputStream)).use {
                val response = StringBuffer()

                var inputLine = it.readLine()
                while (inputLine != null) {
                    response.append(inputLine)
                    inputLine = it.readLine()
                }
                it.close()
                println("Response : $response")*/
            //}
        }
    }

    fun putExtraPost(name: String, content: String){
        jsonToSend.put(name,content)
    }
}
