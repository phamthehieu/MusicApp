package com.example.musicapp.filters

import android.widget.Filter
import com.example.musicapp.adapters.ArtistsAdapters
import com.example.musicapp.models.ArtistsModel

class FiltersArtists : Filter {

    private var filterList: ArrayList<ArtistsModel>

    private var adapterArtists: ArtistsAdapters

    constructor(filterList: ArrayList<ArtistsModel>, adapterArtists: ArtistsAdapters) : super() {
        this.filterList = filterList
        this.adapterArtists = adapterArtists
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val results = FilterResults()

        if (!constraint.isNullOrEmpty()) {
            constraint = constraint.toString().uppercase()
            val filteredModel: ArrayList<ArtistsModel> = ArrayList()
            for (i in 0 until filterList.size) {
                if (filterList[i].name.uppercase().contains(constraint)) {
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
        adapterArtists.artistsArrayList = results.values as ArrayList<ArtistsModel>
        adapterArtists.notifyDataSetChanged()
    }
}