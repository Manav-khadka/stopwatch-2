package com.example.stopwatch

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import android.content.Intent
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.app.ActivityManager
import android.content.Context
import com.example.stopwatch.service.TimerService
class MainActivity: FlutterActivity() {
    private val CHANNEL = "timer_channel"
    private var methodChannel: MethodChannel? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        methodChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
        methodChannel?.setMethodCallHandler { call, result ->
            when (call.method) {
                "startTimer" -> {
                    startTimerService()
                    result.success(null)
                }
                "stopTimer" -> {
                    stopTimerService()
                    result.success(null)
                }
                "getTimerState" -> {
                    result.success(isTimerRunning())
                }
                "checkNotificationPermissions" -> {
                    result.success(checkNotificationPermissions())
                }
                "requestNotificationPermissions" -> {
                    requestNotificationPermissions()
                    result.success(true)
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
    }

    private fun startTimerService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkNotificationPermissions()) {
                startService()
            } else {
                requestNotificationPermissions()
            }
        } else {
            startService()
        }
    }

//    private fun startService() {
//        Intent(this, TimerService::class.java).also { intent ->
//            intent.action = Constants.ACTION_START_TIMER
//            startService(intent)
//        }
//    }

    private fun stopTimerService() {
        Intent(this, TimerService::class.java).also { intent ->
            intent.action = Constants.ACTION_STOP_TIMER
            startService(intent)
        }
    }

    private fun isTimerRunning(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (TimerService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun checkNotificationPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                101
            )
        }
    }

    private fun startService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
                return
            }
        }

        Intent(this, TimerService::class.java).also { intent ->
            intent.action = Constants.ACTION_START_TIMER
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
    }


}