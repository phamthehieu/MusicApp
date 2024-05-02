package com.example.musicapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.example.musicapp.service.MusicService
import java.io.Serializable

class MyReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val actionMusic = intent?.getIntExtra("action_music", 0)
        val bundle = intent?.extras
        val song = bundle?.get("object_song")
        val bundleSend = Bundle()
        bundleSend.putSerializable("object_song", song as Serializable?)
        val intentService = Intent(context, MusicService::class.java)
        intentService.putExtras(bundleSend);
        intentService.putExtra("actionMusic", actionMusic)
        intentService.putExtra("check", true)
        context?.startService(intentService)
    }
}