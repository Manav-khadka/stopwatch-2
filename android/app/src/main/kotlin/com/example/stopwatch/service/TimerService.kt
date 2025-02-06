package com.example.stopwatch.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import java.util.Timer
import java.util.TimerTask
import com.example.stopwatch.Constants
import com.example.stopwatch.NotificationHelper
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build

class TimerService : Service() {
    private var isTimerRunning = false
    private var timeInMillis = 0L
    private val notificationHelper by lazy { NotificationHelper(this) }
    private var timer: Timer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Constants.ACTION_START_TIMER -> startTimer()
            Constants.ACTION_STOP_TIMER -> stopTimer()
        }
        return START_STICKY
    }

    private fun startTimer() {
        if (!isTimerRunning) {
            isTimerRunning = true

            val notification = notificationHelper.buildNotification(formatTime(timeInMillis))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                startForeground(
                    Constants.NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE
                )
            } else {
                startForeground(Constants.NOTIFICATION_ID, notification)
            }

            timer = Timer()
            timer?.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    timeInMillis += 100 // Update every 100ms
                    updateNotification()
                }
            }, 0, 100)
        }
    }

    private fun updateNotification() {
        val notification = notificationHelper.buildNotification(formatTime(timeInMillis))
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(Constants.NOTIFICATION_ID, notification)
    }

    private fun stopTimer() {
        isTimerRunning = false
        timer?.cancel()
        timer = null
        stopForeground(true)
        stopSelf()
    }

    private fun formatTime(millis: Long): String {
        val minutes = (millis / 60000).toInt()
        val seconds = ((millis % 60000) / 1000).toInt()
        val hundredths = ((millis % 1000) / 10).toInt()
        return String.format("%02d:%02d:%02d", minutes, seconds, hundredths)
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}