package com.example.musicapp.filters

import android.widget.Filter
import com.example.musicapp.adapters.ArtistsAdapters
import com.example.musicapp.adapters.SongRowOneAdapters
import com.example.musicapp.models.ArtistsModel
import com.example.musicapp.models.Check

class FiltersSongs: Filter {

    private var filterList: ArrayList<Check.DataSong>

    private var songAdapters: SongRowOneAdapters

    constructor(filterList: ArrayList<Check.DataSong>, songAdapters: SongRowOneAdapters) {
        this.filterList = filterList
        this.songAdapters = songAdapters
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val results = FilterResults()

        if (!constraint.isNullOrEmpty()) {
            constraint = constraint.toString().uppercase()
            val filteredModel: ArrayList<Check.DataSong> = ArrayList()
            for (i in 0 until filterList.size) {
                if (filterList[i].titleSong.uppercase().contains(constraint)) {
                    filteredModel.add(filterList[i])
                }
            }
            results.count = filteredModel.size
            results.values = filteredModel
        } else {
            results.count = filterList.size
            results.values = filterList
        }
        return results;
    }

    override fun publishResults(constraint: CharSequence, results: FilterResults) {
        songAdapters.songArrayList = results.values as ArrayList<Check.DataSong>
        songAdapters.notifyDataSetChanged()
    }
}