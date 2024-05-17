package com.example.musicapp.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.activities.MediaPlayActivity
import com.example.musicapp.databinding.RowSongBinding
import com.example.musicapp.filters.FiltersSongs
import com.example.musicapp.models.Check
import com.example.musicapp.models.SongPlay
import com.example.musicapp.service.MusicService

class SongRowOneAdapters : RecyclerView.Adapter<SongRowOneAdapters.HolderSong>, Filterable {

    private lateinit var binding: RowSongBinding

    private val context: Context


    var songArrayList: ArrayList<Check.DataSong>

    private var arrayList: ArrayList<Check.DataSong>

    private var filter: FiltersSongs? = null

    constructor(
        context: Context,
        songArrayList: ArrayList<Check.DataSong>
    ) : super() {
        this.context = context
        this.songArrayList = songArrayList
        this.arrayList = songArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderSong {
        binding = RowSongBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderSong(binding.root)
    }

    override fun getItemCount(): Int {
        return songArrayList.size
    }

    override fun onBindViewHolder(holder: HolderSong, position: Int) {
        val model = songArrayList[position]
        val titleSong = model.titleSong
        val idArtists = model.idArtists
        val nameArtists = model.nameArtists
        val imageAlbum = model.imageAlbum
        val preview = model.preview
        val titleAlbum = model.titleAlbum
        val idAlbum = model.idAlbum
        holder.nameMusicEt.text = titleSong
        holder.nameArtistsEt.text = nameArtists
        holder.playMusic.setOnClickListener {
            playMusic(titleSong, idArtists, nameArtists, imageAlbum, preview, titleAlbum, idAlbum)
        }

        try {
            Glide.with(context)
                .load(imageAlbum)
                .placeholder(R.drawable.ic_person_gray)
                .into(holder.imageArtists)

        } catch (e: Exception) {
            Log.d("loadUserInfo", "onDataChange: ${e.message}")
        }

    }

    private fun playMusic(
        titleSong: String,
        idArtists: String,
        nameArtists: String,
        imageAlbum: String,
        preview: String,
        titleAlbum: String,
        idAlbum: String
    ) {
        val sharedPreferences = context.getSharedPreferences("SongsPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("titleSong", titleSong)
            putString("idArtists", idArtists)
            putString("nameArtists", nameArtists)
            putString("imageAlbum", imageAlbum)
            putString("preview", preview)
            putBoolean("checkIsLogin", false)
            putBoolean("checkIsPlay", true)
            putString("titleAlbum", titleAlbum)
            putString("idAlbum", idAlbum)
            apply()
        }
        val songPlay = SongPlay(
            titleSong = titleSong,
            idArtists = "0",
            nameArtists = nameArtists,
            imageAlbum = imageAlbum,
            preview = preview,
            checkIsPlay = true
        )
        val intent = Intent(context, MusicService::class.java)
        intent.putExtra("active_type", MusicService.ACTION_PLAY)
        intent.putExtra("song_play", songPlay)
        context.startService(intent)
    }


    inner class HolderSong(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var nameArtistsEt: TextView = binding.nameArtistsEt
        var imageArtists: ImageView = binding.imageArtists
        var nameMusicEt: TextView = binding.nameMusicEt
        var likeBtn: ImageView = binding.likeBtn
        var playMusic: RelativeLayout = binding.playMusic
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FiltersSongs(arrayList, this)
        }
        return filter as FiltersSongs
    }

}