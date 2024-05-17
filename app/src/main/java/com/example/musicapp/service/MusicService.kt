package com.example.musicapp.service

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import com.example.musicapp.activities.MediaPlayActivity

class MusicService : Service() {

    private lateinit var mediaPlayer: MediaPlayer

    companion object {
        const val ACTION_PAUSE = 1
        const val ACTION_RESUME = 2
        const val ACTION_PLAY = 3
        const val ACTION_SEEK_TO = 44
        const val ACTION_REPEAT_ONE = 1
        const val ACTION_REPEAT_ALL = 2
        const val ACTION_NO_REPEAT = 3
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

    private var pendingIntentRequestCode = 0
    private var currentBackground = 0
    private var totalBackground = 0
    private var playSongBackground = false
    private var checkIsLogin = true
    private var firstTime = true
    private var updateThread: Thread? = null
    private var repeatType = 3

    private lateinit var sharedPreferences: SharedPreferences

    private val sharedPreferencesListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            when (key) {
                "titleSong" -> {
                    mSong?.titleSong = sharedPreferences.getString("titleSong", null).toString()
                }

                "nameArtists" -> {
                    mSong?.nameArtists = sharedPreferences.getString("nameArtists", null).toString()
                }

                "imageAlbum" -> {
                    mSong?.imageAlbum = sharedPreferences.getString("imageAlbum", null).toString()
                }

                "preview" -> {
                    val previewUrl = sharedPreferences.getString("preview", null)
                    if (previewUrl != null) {
                        mSong?.preview = previewUrl
                    }
                }

                "checkIsLogin" -> checkIsLogin = sharedPreferences.getBoolean("checkIsLogin", true)
                "checkIsPlay" -> {
                    mSong?.checkIsPlay = sharedPreferences.getBoolean("checkIsPlay", false)
                }

                "firstTime" -> firstTime = sharedPreferences.getBoolean("firstTime", true)
            }
        }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences("SongsPrefs", Context.MODE_PRIVATE)
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferencesListener)
        mediaPlayer = MediaPlayer()
        registerReceiver(appStateReceiver, IntentFilter().apply {
            addAction("com.example.musicapp.APP_FOREGROUND")
            addAction("com.example.musicapp.APP_BACKGROUND")
        })
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val bundle = intent?.extras
        val actionService = intent?.getIntExtra("actionMusic", 0)
        val actionLoop = intent?.getIntExtra("actionLoop", 3)
        val songSend = bundle?.get("object_song")
        val actionType = intent?.getIntExtra("active_type", 0)
        if (actionService == ACTION_PAUSE) {
            handleActionMusic(ACTION_PAUSE)
            mSong = songSend as SongPlay?
        } else if (actionService == ACTION_RESUME) {
            handleActionMusic(ACTION_RESUME)
            mSong = songSend as SongPlay?
        }
        if (actionLoop != 0 && playSongBackground) {
            if (actionLoop != null) {
                repeatType = actionLoop
            }
            sendNotification(mSong)
        }

        when (actionType) {
            ACTION_PLAY -> {
                val songPlay = intent.getSerializableExtra("song_play") as SongPlay
                if (songPlay.preview != "") {
                    onPlaySong(songPlay.preview)
                    mSong = SongPlay(
                        titleSong = songPlay.titleSong,
                        idArtists = "0",
                        nameArtists = songPlay.nameArtists,
                        imageAlbum = songPlay.imageAlbum,
                        preview = songPlay.preview,
                        checkIsPlay = songPlay.checkIsPlay
                    )
                }
            }

            ACTION_PAUSE -> pauseMusic()
            ACTION_RESUME -> resumeMusic()
            ACTION_SEEK_TO -> {
                val seekPosition = intent.getIntExtra("seek_to", 0)
                mediaPlayer.seekTo(seekPosition)
            }
        }

        return START_NOT_STICKY
    }

    private val appStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                "com.example.musicapp.APP_FOREGROUND" -> {
                    stopForeground(true)
                    playSongBackground = false
                    isPlaying = false
                }

                "com.example.musicapp.APP_BACKGROUND" -> {
                    if (mSong?.checkIsPlay == true) {
                        playSongBackground = true
                        isPlaying = true
                        sendNotification(mSong)
                    }
                }
            }
        }
    }

    private fun startUpdateSeekBarThread() {
        updateThread = Thread {
            try {
                while (mediaPlayer.isPlaying) {
                    val total = mediaPlayer.duration
                    val current = mediaPlayer.currentPosition
                    val intent = Intent("com.example.musicapp.UPDATE_PLAYER")
                    intent.putExtra("current", current)
                    intent.putExtra("total", total)
                    sendBroadcast(intent)
                    if (playSongBackground) {
                        totalBackground = total
                        currentBackground = current
                        sendNotification(mSong)
                    }
                    Thread.sleep(1000)
                }
            } catch (e: InterruptedException) {
                Log.e("MusicService", "Thread interrupted: ${e.message}")
            }
        }
        updateThread?.start()
    }

    private fun restartUpdateSeekBarThread() {
        if (updateThread?.isAlive == true) {
            updateThread?.interrupt()
        }
        startUpdateSeekBarThread()
    }

    private fun sendNotification(song: SongPlay?) {
        val remoteViews = RemoteViews(packageName, R.layout.layout_custom_notification)
        remoteViews.setProgressBar(R.id.progressBar, totalBackground, currentBackground, false)
        remoteViews.setTextViewText(R.id.runTime, MyApplication.formatTime(currentBackground))
        remoteViews.setTextViewText(R.id.totalTime, MyApplication.formatTime(totalBackground))
        remoteViews.setTextViewText(R.id.nameSong, song?.titleSong)
        remoteViews.setTextViewText(R.id.nameArtists, song?.nameArtists)
        remoteViews.setImageViewResource(R.id.playSongNotification, R.drawable.pause24black)
        remoteViews.setImageViewResource(R.id.loopSongNotification, R.drawable.iconloop24black)
        when (repeatType) {
            ACTION_REPEAT_ONE -> {
                remoteViews.setImageViewResource(
                    R.id.loopSongNotification,
                    R.drawable.iconloop24oneblack
                )
                remoteViews.setOnClickPendingIntent(
                    R.id.loopSongNotification,
                    getPendingLoopIntent(this, ACTION_REPEAT_ALL)
                )
            }

            ACTION_REPEAT_ALL -> {
                remoteViews.setImageViewResource(
                    R.id.loopSongNotification,
                    R.drawable.iconloop24green
                )
                remoteViews.setOnClickPendingIntent(
                    R.id.loopSongNotification,
                    getPendingLoopIntent(this, ACTION_NO_REPEAT)
                )
            }

            ACTION_NO_REPEAT -> {
                remoteViews.setImageViewResource(
                    R.id.loopSongNotification,
                    R.drawable.iconloop24black
                )
                remoteViews.setOnClickPendingIntent(
                    R.id.loopSongNotification,
                    getPendingLoopIntent(this, ACTION_REPEAT_ONE)
                )
            }
        }

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
                    updateNotification(remoteViews)
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    remoteViews.setImageViewResource(R.id.imageSong, R.drawable.ic_image_white)
                    updateNotification(remoteViews)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
    }

    private fun getPendingLoopIntent(context: Context, action: Int): PendingIntent {
        val intent = Intent(context, MyReceiver::class.java)
        intent.putExtra("action_loop", action)
        return PendingIntent.getBroadcast(
            context.applicationContext,
            pendingIntentRequestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }


    private fun getPendingIntent(context: Context, action: Int): PendingIntent? {
        val intent = Intent(this, MyReceiver::class.java)
        val bundle = Bundle()
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
        val intent = Intent(this, MediaPlayActivity::class.java)
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
                    startUpdateSeekBarThread()
                }
                setOnCompletionListener {
                    onSongCompletion()
                }
            }
        }
    }

    private fun onSongCompletion() {
        val sharedPreferences = this.getSharedPreferences("SongsPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("checkIsPlay", false)
        editor.apply()
        if (repeatType == ACTION_REPEAT_ONE) {
            mSong?.let { onPlaySong(it.preview) }
        }
    }

    private fun handleActionMusic(action: Int) {
        when (action) {
            1 -> {
                pauseMusic()
            }

            2 -> {
                resumeMusic()
            }

            else -> {
                Log.d("Error", "handleActionMusic")
            }
        }

    }

    private fun resumeMusic() {
        Log.d("Hieu321", "resumeMusic")
        if (playSongBackground) {
            val sharedPreferences = this.getSharedPreferences("SongsPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            if (!isPlaying) {
                mediaPlayer.start()
                isPlaying = true
                sendNotification(mSong)
                startUpdateSeekBarThread()
                handler.removeCallbacks(stopNotificationRunnable)
                editor.putBoolean("checkIsPlay", true)
                editor.apply()
            }
        } else {
            mediaPlayer.start()
            restartUpdateSeekBarThread()
        }

    }

    private fun pauseMusic() {
        if (playSongBackground) {
            val sharedPreferences = this.getSharedPreferences("SongsPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            if (isPlaying) {
                mediaPlayer.pause()
                isPlaying = false
                sendNotification(mSong)
                updateThread?.interrupt()
                handler.postDelayed(stopNotificationRunnable, 30000)
                editor.putBoolean("checkIsPlay", false)
                editor.apply()
            }
        } else {
            mediaPlayer.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        updateThread?.interrupt()
        mediaPlayer.release()
        handler.removeCallbacks(stopNotificationRunnable)
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferencesListener)
        unregisterReceiver(appStateReceiver)
    }

}