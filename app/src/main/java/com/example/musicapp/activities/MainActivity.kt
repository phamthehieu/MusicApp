package com.example.musicapp.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.databinding.ActivityMainBinding
import com.example.musicapp.fragmentes.HomeFragment
import com.example.musicapp.fragmentes.LibrariesFragment
import com.example.musicapp.fragmentes.SearchesFragment
import com.example.musicapp.fragmentes.SettingAdminFragment
import com.example.musicapp.fragmentes.SettingUserFragment
import com.example.musicapp.models.SongPlay
import com.example.musicapp.service.MusicService
import com.example.musicapp.viewModel.UserViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlin.math.log

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var check: Fragment

    private var userImage = ""
    private var userEmail = ""
    private var checkIsPlay = false
    private var checkIsLogin = true
    private var audioUrl = ""

    private val sharedPreferencesListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            when (key) {
                "titleSong" -> binding.nameSong.text =
                    sharedPreferences.getString("titleSong", null)

                "nameArtists" -> binding.nameArtistsEt.text =
                    sharedPreferences.getString("nameArtists", null)

                "imageAlbum" ->
                    Glide.with(this)
                        .load(sharedPreferences.getString("imageAlbum", null))
                        .placeholder(R.drawable.ic_person_gray)
                        .into(binding.imageArtists)

                "checkIsPlay" -> {
                    checkIsPlay = sharedPreferences.getBoolean("checkIsPlay", false)
                    Log.d("hieu55", checkIsPlay.toString())
                    if (checkIsPlay) {
                        binding.playMusicAction.setImageResource(R.drawable.ic_pause_white)
                    } else {
                        binding.playMusicAction.setImageResource(R.drawable.ic_play_white)
                    }
                }

                "checkIsLogin" -> {
                    checkIsLogin = sharedPreferences.getBoolean("checkIsLogin", true)
                }

                "preview" -> {
                    val previewUrl = sharedPreferences.getString("preview", null)
                    if (previewUrl != null) {
                        audioUrl = previewUrl
                    }
                }
            }
        }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        setContentView(binding.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        userImage = intent.getStringExtra("userImage") ?: ""
        userEmail = intent.getStringExtra("email") ?: ""

        replaceFragment(HomeFragment())

        userViewModel.userType.observe(this) { userType ->
            check = if (userType == "user") {
                SettingUserFragment()
            } else {
                SettingAdminFragment()
            }
        }

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> replaceFragment(HomeFragment())
                R.id.search -> replaceFragment(SearchesFragment())
                R.id.libraries -> replaceFragment(LibrariesFragment())
                R.id.setting -> replaceFragment(check)
            }
            true
        }

        binding.playMusicAction.setOnClickListener {
            updatePlayButton()
        }

        binding.playMusic.setOnClickListener {
            val intent = Intent(this, MediaPlayActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down)
        }

    }

    private fun updatePlayButton() {
        val intentMain = Intent(this, MusicService::class.java)
        val sharedPreferences = this.getSharedPreferences("SongsPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        if (checkIsPlay) {
            checkIsPlay = false
            intentMain.putExtra("active_type", MusicService.ACTION_PAUSE)
            startService(intentMain)
            binding.playMusicAction.setImageResource(R.drawable.ic_play_white)
        } else {
            binding.playMusicAction.setImageResource(R.drawable.ic_play_white)
            if (checkIsLogin) {
                checkIsLogin = false
                checkIsPlay = true
                intentMain.putExtra("active_type", MusicService.ACTION_PLAY)
                intentMain.putExtra("url_audio", audioUrl)
                startService(intentMain)
                binding.playMusicAction.setImageResource(R.drawable.ic_pause_white)
            } else {
                checkIsPlay = true
                intentMain.putExtra("active_type", MusicService.ACTION_RESUME)
                startService(intentMain)
                binding.playMusicAction.setImageResource(R.drawable.ic_pause_white)
            }
        }
        Log.d("hieu145", checkIsPlay.toString())
        editor.putBoolean("checkAutoPlay", false)
        editor.putBoolean("checkIsPlay", checkIsPlay)
        editor.putBoolean("checkIsLogin", checkIsLogin)
        editor.putBoolean("checkIsLogin", false)
        editor.apply()
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
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferencesListener)
    }

    private fun replaceFragment(fragment: Fragment) {
        val bundle = Bundle()
        bundle.putString("userImage", userImage)
        fragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        val sharedPreferences = getSharedPreferences("SongsPrefs", Context.MODE_PRIVATE)
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferencesListener)
    }


}
