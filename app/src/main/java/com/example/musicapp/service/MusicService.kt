package com.example.musicapp.service

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.example.musicapp.R
import com.example.musicapp.activities.MainActivity
import com.example.musicapp.models.SongPlay
import com.bumptech.glide.request.transition.Transition
import com.example.musicapp.MyApplication
import com.example.musicapp.MyReceiver
import kotlin.math.log

class MusicService : Service() {

    private lateinit var mediaPlayer: MediaPlayer

    companion object {
        const val ACTION_PAUSE = 1
        const val ACTION_RESUME = 2
        const val ACTION_PLAY = 3
    }

    private var isPlaying: Boolean = true
    private var mSong: SongPlay? = null
    private val handler = Handler(Looper.getMainLooper())
    private val stopNotificationRunnable = Runnable {
        if (!isPlaying) {
            stopForeground(true)
            isPlaying = true
        }
    }

    private var titleSong = ""
    private var nameArtists = ""
    private var imageAlbum = ""
    private var audioUrl = ""
    private var checkIsLogin = true
    private var checkIsPlay = false
    private var firstTime = true

    private lateinit var sharedPreferences: SharedPreferences

    private val sharedPreferencesListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            when (key) {
                "titleSong" -> {
                    titleSong = sharedPreferences.getString("titleSong", null).toString()
                }

                "nameArtists" -> {
                    nameArtists = sharedPreferences.getString("nameArtists", null).toString()
                }

                "imageAlbum" -> {
                    imageAlbum = sharedPreferences.getString("imageAlbum", null).toString()
                }

                "preview" -> {
                    val previewUrl = sharedPreferences.getString("preview", null)
                    if (previewUrl != null) {
                        audioUrl = previewUrl
                    }
                }

                "checkIsLogin" -> checkIsLogin = sharedPreferences.getBoolean("checkIsLogin", true)
                "checkIsPlay" -> {
                    checkIsPlay = sharedPreferences.getBoolean("checkIsPlay", false)
                }

                "firstTime" -> firstTime = sharedPreferences.getBoolean("firstTime", true)
            }
        }

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences("SongsPrefs", Context.MODE_PRIVATE)
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferencesListener)
        mediaPlayer = MediaPlayer()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val sharedPreferences = this.getSharedPreferences("SongsPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val bundle = intent?.extras
        val actionService = intent?.getIntExtra("actionMusic", 0)
        val songSend = bundle?.get("object_song")
        val actionType = intent?.getIntExtra("active_type", 0)
        val urlAudio = intent?.getStringExtra("url_audio")
        if (actionType == ACTION_PLAY) {
            if (urlAudio != null) {
                onPlaySong(urlAudio)
            }
        } else if (actionType == ACTION_PAUSE) {
            pauseMusic()
        } else if (actionType == ACTION_RESUME) {
            resumeMusic()
        }
        editor.apply()
        if (actionService != null && songSend != null) {
            handleActionMusic(actionService)
            mSong = songSend as SongPlay?
        }

        return START_NOT_STICKY
    }

    private fun sendNotification(song: SongPlay?) {
        val remoteViews = RemoteViews(packageName, R.layout.layout_custom_notification)
        remoteViews.setTextViewText(R.id.nameSong, song?.titleSong)
        remoteViews.setTextViewText(R.id.nameArtists, song?.nameArtists)
        remoteViews.setImageViewResource(R.id.playSongNotification, R.drawable.pause24black)
        if (isPlaying) {
            remoteViews.setOnClickPendingIntent(
                R.id.playSongNotification,
                getPendingIntent(this, ACTION_PAUSE)
            )
            remoteViews.setImageViewResource(R.id.playSongNotification, R.drawable.pause24black)
        } else {
            remoteViews.setOnClickPendingIntent(
                R.id.playSongNotification,
                getPendingIntent(this, ACTION_RESUME)
            )
            remoteViews.setImageViewResource(R.id.playSongNotification, R.drawable.play24black)
        }

        Glide.with(this)
            .asBitmap()
            .load(song?.imageAlbum)
            .placeholder(R.drawable.ic_person_gray)
            .error(R.drawable.ic_image_white)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    remoteViews.setImageViewBitmap(R.id.imageSong, resource)
                    updateNotification(remoteViews) // Update the notification after image loads
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    remoteViews.setImageViewResource(R.id.imageSong, R.drawable.ic_image_white)
                    updateNotification(remoteViews) // Update the notification on load failure
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Optional cleanup actions
                }
            })
    }

    private fun getPendingIntent(context: Context, action: Int): PendingIntent? {
        val intent = Intent(this, MyReceiver::class.java)
        var bundle = Bundle()
        bundle.putSerializable("object_song", mSong)
        intent.putExtras(bundle);
        intent.putExtra("action_music", action)

        return PendingIntent.getBroadcast(
            context.applicationContext,
            action,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    @SuppressLint("ForegroundServiceType")
    private fun updateNotification(remoteViews: RemoteViews) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val notification = NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(pendingIntent)
            .setCustomContentView(remoteViews)
            .setSound(null)
            .build()

        startForeground(1, notification)
    }

    private fun onPlaySong(song: String) {
        val editor = sharedPreferences.edit()
        if (song.isNotBlank()) {
            mediaPlayer.apply {
                reset()
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(song)
                prepareAsync()
                setOnPreparedListener {
                    start()
                    editor.putBoolean("firstTime", true)
                    editor.apply()
                }
            }
        }


    }

    private fun handleActionMusic(action: Int) {
        when (action) {
            ACTION_PAUSE -> {
                pauseMusic()
            }

            ACTION_RESUME -> {
                resumeMusic()
            }

            else -> {
                Log.d("Error", "handleActionMusic")
            }
        }

    }

    private fun resumeMusic() {
        mediaPlayer.start()
//        if (!isPlaying) {

//            isPlaying = true
//            sendNotification(mSong)
//            handler.removeCallbacks(stopNotificationRunnable)
//        }
    }

    private fun pauseMusic() {
        mediaPlayer.pause()
//        if (isPlaying) {

//            isPlaying = false
//            sendNotification(mSong)
//            handler.postDelayed(stopNotificationRunnable, 30000)
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        handler.removeCallbacks(stopNotificationRunnable)
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferencesListener)
    }

}