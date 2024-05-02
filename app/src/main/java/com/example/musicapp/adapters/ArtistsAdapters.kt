package com.example.musicapp.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.activities.SingerListActivity
import com.example.musicapp.databinding.RowSingerBinding
import com.example.musicapp.filters.FiltersArtists
import com.example.musicapp.models.ArtistsModel

interface OnSelectedArtistsIdsListener {
    fun onSelectedArtistsIdsUpdated(selectedArtistsIds: ArrayList<String>)
}

class ArtistsAdapters: RecyclerView.Adapter<ArtistsAdapters.HolderArtists>, Filterable {

    private val context: Context

    var artistsArrayList: ArrayList<ArtistsModel>

    private var arrayList: ArrayList<ArtistsModel>

    private var filter: FiltersArtists? = null

    private lateinit var binding: RowSingerBinding

    private val selectedArtistsIds: ArrayList<String> = ArrayList()

    constructor(context: Context, artistsArrayList: ArrayList<ArtistsModel>) {
        this.context = context
        this.artistsArrayList = artistsArrayList
        this.arrayList = artistsArrayList
    }

    private var listener: OnSelectedArtistsIdsListener? = null

    fun setOnSelectedArtistsIdsListener(listener: OnSelectedArtistsIdsListener) {
        this.listener = listener
    }
    override fun getItemCount(): Int {
        return artistsArrayList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderArtists {
        binding = RowSingerBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderArtists(binding.root)
    }

    override fun onBindViewHolder(holder: HolderArtists, position: Int) {
        val model = artistsArrayList[position]
        val idArtists = model.idArtists
        val name = model.name
        val profileImage = model.profileImage
        val timestamp = model.timestamp
        val uid = model.uid

        holder.checkArtists.visibility = View.GONE

        holder.nameArtistsEt.text = name

        try {
            Glide.with(context)
                .load(profileImage)
                .placeholder(R.drawable.ic_person_gray)
                .into(holder.imageArtists)

        } catch (e: Exception) {
            Log.d("loadUserInfo", "onDataChange: ${e.message}")
        }

        holder.imageArtists.setOnClickListener {
            val nameCheck = artistsArrayList[position].name
            if (selectedArtistsIds.contains(nameCheck)) {
                selectedArtistsIds.remove(name)
                holder.checkArtists.visibility = View.GONE
            } else {
                selectedArtistsIds.add(name)
                holder.checkArtists.visibility = View.VISIBLE
            }
            listener?.onSelectedArtistsIdsUpdated(selectedArtistsIds)

            val idArtists = artistsArrayList[position].idArtists
            if (selectedArtistsIds.contains(idArtists)) {
                selectedArtistsIds.remove(idArtists)
                holder.checkArtists.visibility = View.GONE
            } else {
                selectedArtistsIds.add(idArtists)
                holder.checkArtists.visibility = View.VISIBLE
            }

        }

    }

    inner class HolderArtists(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var nameArtistsEt: TextView = binding.nameArtistsEt
        var imageArtists: ImageView = binding.imageArtists
        val checkArtists: ImageView = binding.checkArtists

    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FiltersArtists(arrayList, this)
        }
        return filter as FiltersArtists
    }

}