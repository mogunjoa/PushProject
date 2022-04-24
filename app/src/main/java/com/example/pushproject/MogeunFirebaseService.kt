package com.example.pushproject

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MogeunFirebaseService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.i("onNewToken", "Success save token")
        sendRegistrationToServer(token) // Token을 서버로 전송
    }

    private fun sendRegistrationToServer(token: String?) {
        Log.d(TAG, "sendRegistrationTokenToServer($token)")
    }

    // 메시지를 수신하는 메서드
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            sendNotification(
                remoteMessage.data["title"].toString(),
                remoteMessage.data["body"].toString()
            )
        } else {
            remoteMessage.notification?.let {
                sendNotification(
                    remoteMessage.notification?.title.toString(),
                    remoteMessage.notification?.body.toString()
                )
            }
        }
    }

    // 알림을 생성하는 메서드
    private fun sendNotification(title: String, body: String) {
        val notifyId = (System.currentTimeMillis() / 7).toInt()

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent =
            PendingIntent.getActivity(this, notifyId, intent, PendingIntent.FLAG_ONE_SHOT)

        val channelId = getString(R.string.default_notification_channel_id)
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationManagerCompat.IMPORTANCE_HIGH)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "HOLYMOLY",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(notifyId, notificationBuilder.build())

        savePush(title, body)
    }

    private fun savePush(title: String, body: String) {
        val sharedPreferences = getSharedPreferences("Push", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("title", title)
        editor.putString("body", body)
        editor.apply()

        getPushData(sharedPreferences)
    }

    private fun getPushData(pref: SharedPreferences) {
        pref.getString("title", "")
        pref.getString("body", "")
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}