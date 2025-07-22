package com.rimaro.musify.utils

import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.rimaro.musify.service.PlaybackService

class CallListener (
    private val context: Context,
) {
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    val executor = ContextCompat.getMainExecutor(context)
    private var oldPhoneStateListener: PhoneStateListener? = null

    private lateinit var _mediaController: MediaController
    private var playbackWasPaused = false

    fun startListening() {
        val token = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )
        val controllerFuture = MediaController.Builder(
            context,
            token
        ).buildAsync()

        controllerFuture.addListener (
            {
                val controller = controllerFuture.get()
                _mediaController = controller

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    telephonyManager.registerTelephonyCallback(executor, telephonyCallback)
                } else {
                    oldPhoneStateListener = object : PhoneStateListener() {
                        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                            handleCallState(state)
                        }
                    }
                }
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private val telephonyCallback = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
        override fun onCallStateChanged(state: Int) {
            handleCallState(state)
        }
    }

    fun stopListening() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyManager.unregisterTelephonyCallback(telephonyCallback)
        } else {
            telephonyManager.listen(oldPhoneStateListener, PhoneStateListener.LISTEN_NONE)
        }
    }

    private fun handleCallState(state: Int) {
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                if(_mediaController.isPlaying){
                    _mediaController.pause()
                    playbackWasPaused = true
                }
           }
            TelephonyManager.CALL_STATE_IDLE -> {
                if(playbackWasPaused){
                    _mediaController.play()
                    playbackWasPaused = false
                }
            }
        }
    }
}