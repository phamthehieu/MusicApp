package com.example.musicapp.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.musicapp.MyApplication
import com.example.musicapp.R
import com.example.musicapp.databinding.ActivityMediaPlayBinding
import com.example.musicapp.models.SongPlay
import com.example.musicapp.service.MusicService
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.imageview.ShapeableImageView

class MediaPlayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMediaPlayBinding
    private lateinit var updatePlayerReceiver: BroadcastReceiver
    private lateinit var mediaPlayer: MediaPlayer

    private var checkIsPlay = false
    private var checkIsLogin = true
    private var audioUrl = ""
    private var idAlbum = ""
    private var nameAlbum = ""
    private var nameSong = ""
    private var nameArtists = ""
    private var imageAlbum = ""

    private val sharedPreferencesListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            when (key) {
                "titleSong" -> {
                    binding.nameSong.text = sharedPreferences.getString("titleSong", null)
                    nameSong = sharedPreferences.getString("titleSong", null).toString()
                }

                "nameArtists" -> {
                    binding.nameArtists.text = sharedPreferences.getString("nameArtists", null)
                    nameArtists = sharedPreferences.getString("nameArtists", null).toString()
                }

                "imageAlbum" -> {
                    Glide.with(this)
                        .load(sharedPreferences.getString("imageAlbum", null))
                        .placeholder(R.drawable.ic_person_gray)
                        .into(binding.profileSong)
                    imageAlbum = sharedPreferences.getString("imageAlbum", null).toString()
                }

                "checkIsPlay" -> {
                    checkIsPlay = sharedPreferences.getBoolean("checkIsPlay", false)
                    if (checkIsPlay) {
                        binding.playBtn.setImageResource(R.drawable.pause)
                    } else {
                        binding.playBtn.setImageResource(R.drawable.play)
                    }
                }

                "checkIsLogin" -> {
                    checkIsLogin = sharedPreferences.getBoolean("checkIsLogin", true)
                }

                "preview" -> {
                    val previewUrl = sharedPreferences.getString("preview", null)
                    if (previewUrl != null) {
                        audioUrl = previewUrl
                        mediaPlayer = MediaPlayer().apply {
                            setDataSource(previewUrl)
                            prepareAsync()
                            setOnPreparedListener { player ->
                                val duration = player.duration
                                binding.totalTime.text = MyApplication.formatTime(duration)
                            }
                        }
                    }
                }

                "idAlbum" -> idAlbum = sharedPreferences.getString("idAlbum", null).toString()

                "titleAlbum" -> {
                    binding.album.text = sharedPreferences.getString("titleAlbum", null)
                    nameAlbum = sharedPreferences.getString("titleAlbum", null).toString()
                }
            }
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.playBtn.setOnClickListener {
            updatePlayButton()
        }

        binding.backBtn.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down)
        }

        binding.moreVertBtn.setOnClickListener {
            showDialog()
        }

        val intentMain = Intent(this, MusicService::class.java)
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val newPosition = progress * mediaPlayer.duration / 100
                     intentMain.putExtra("active_type", MusicService.ACTION_SEEK_TO)
                     intentMain.putExtra("seek_to", newPosition)
                    startService(intentMain)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        setupBroadcastReceiver()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun setupBroadcastReceiver() {
        val sharedPreferences = this.getSharedPreferences("SongsPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        updatePlayerReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val current = intent?.getIntExtra("current", 0) ?: 0
                val total = intent?.getIntExtra("total", 0) ?: 0
                binding.runTime.text = MyApplication.formatTime(current)
                binding.totalTime.text = MyApplication.formatTime(total)
                binding.seekBar.progress = (current.toFloat() / total * 100).toInt()
                if (current == total) {
                    binding.playBtn.setImageResource(R.drawable.play)
                    editor.putBoolean("checkIsPlay", false)
                    editor.apply()
                }
            }
        }

        val filter = IntentFilter("com.example.musicapp.UPDATE_PLAYER")
        registerReceiver(updatePlayerReceiver, filter, RECEIVER_NOT_EXPORTED)
    }

    @SuppressLint("SetTextI18n")
    private fun showDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog)
        val nameSongDiaLog: TextView? = bottomSheetDialog.findViewById(R.id.nameSongDiaLog)
        val nameArtistsDiaLog: TextView? = bottomSheetDialog.findViewById(R.id.nameArtistsDiaLog)
        val imageDialog: ShapeableImageView = bottomSheetDialog.findViewById(R.id.imageDiaLog)!!
        nameArtistsDiaLog?.text = "$nameArtists + $nameAlbum"
        nameSongDiaLog?.text = nameSong
        Glide.with(this)
            .load(imageAlbum)
            .placeholder(R.drawable.ic_person_gray)
            .into(imageDialog)
        bottomSheetDialog.show()
    }

    private fun updatePlayButton() {
        val intentMain = Intent(this, MusicService::class.java)
        val sharedPreferences = this.getSharedPreferences("SongsPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        if (checkIsPlay) {
            checkIsPlay = false
            intentMain.putExtra("active_type", MusicService.ACTION_PAUSE)
            startService(intentMain)
            binding.playBtn.setImageResource(R.drawable.play)
        } else {
            binding.playBtn.setImageResource(R.drawable.play)
            if (checkIsLogin) {
                checkIsLogin = false
                checkIsPlay = true
                val songPlay = SongPlay(
                    titleSong = nameSong,
                    idArtists = "0",
                    nameArtists = nameArtists,
                    imageAlbum = imageAlbum,
                    preview = audioUrl,
                    checkIsPlay = true
                )
                intentMain.putExtra("active_type", MusicService.ACTION_PLAY)
                intentMain.putExtra("song_play", songPlay)
                startService(intentMain)
                binding.playBtn.setImageResource(R.drawable.pause)
            } else {
                checkIsPlay = true
                intentMain.putExtra("active_type", MusicService.ACTION_RESUME)
                startService(intentMain)
                binding.playBtn.setImageResource(R.drawable.pause)
            }
        }
        editor.putBoolean("checkAutoPlay", false)
        editor.putBoolean("checkIsPlay", checkIsPlay)
        editor.putBoolean("checkIsLogin", checkIsLogin)
        editor.putBoolean("checkIsLogin", false)
        editor.apply()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down)
    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences = getSharedPreferences("SongsPrefs", Context.MODE_PRIVATE)
        sharedPreferencesListener.onSharedPreferenceChanged(sharedPreferences, "titleSong")
        sharedPreferencesListener.onSharedPreferenceChanged(sharedPreferences, "nameArtists")
        sharedPreferencesListener.onSharedPreferenceChanged(sharedPreferences, "imageAlbum")
        sharedPreferencesListener.onSharedPreferenceChanged(sharedPreferences, "checkIsPlay")
        sharedPreferencesListener.onSharedPreferenceChanged(sharedPreferences, "checkIsLogin")
        sharedPreferencesListener.onSharedPreferenceChanged(sharedPreferences, "preview")
        sharedPreferencesListener.onSharedPreferenceChanged(sharedPreferences, "idAlbum")
        sharedPreferencesListener.onSharedPreferenceChanged(sharedPreferences, "titleAlbum")
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferencesListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        val sharedPreferences = getSharedPreferences("SongsPrefs", Context.MODE_PRIVATE)
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferencesListener)
        unregisterReceiver(updatePlayerReceiver)
    }
}