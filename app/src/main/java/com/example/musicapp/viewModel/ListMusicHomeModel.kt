package com.example.musicapp.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.repostirory.ApiService
import com.example.musicapp.models.Check
import com.google.gson.Gson
import kotlinx.coroutines.launch

class ListMusicHomeModel : ViewModel() {
    val tracksDataLive1 = MutableLiveData<List<Check.DataSong>>()
    val tracksDataLive2 = MutableLiveData<List<Check.DataSong>>()
    val tracksDataLive3 = MutableLiveData<List<Check.DataSong>>()
    val isLoading = MutableLiveData<Boolean>()

    private var dataLoaded1 = false
    private var dataLoaded2 = false
    private var dataLoaded3 = false

    fun loadArtistData(artistId: String, index: Int) {
        when (index) {
            1 -> if (dataLoaded1) return
            2 -> if (dataLoaded2) return
            3 -> if (dataLoaded3) return
        }

        isLoading.postValue(true)
        viewModelScope.launch {
            val apiService = ApiService()
            val responseData = apiService.search(artistId)
            val gson = Gson()
            val response = gson.fromJson(responseData, Check.ApiResponse::class.java)
            val tracks = response.data.map { trackData ->
                Check.Track1(
                    trackData.id,
                    trackData.title,
                    trackData.preview,
                    Check.Artist1(trackData.artist.id, trackData.artist.name),
                    Check.Album1(trackData.album.cover_big, trackData.album.title, trackData.album.id)
                )
            }.map { mapTrackToDataSong(it) }
            Log.d("Hieu44", "$tracks")
            when (index) {
                1 -> {
                    tracksDataLive1.postValue(tracks)
                    dataLoaded1 = true
                }
                2 -> {
                    tracksDataLive2.postValue(tracks)
                    dataLoaded2 = true
                }
                3 -> {
                    tracksDataLive3.postValue(tracks)
                    dataLoaded3 = true
                }
            }
            isLoading.postValue(false)
        }
    }

    private fun mapTrackToDataSong(track: Check.Track1): Check.DataSong {
        return Check.DataSong(
            titleSong = track.title,
            idArtists = track.artist.id,
            nameArtists = track.artist.name,
            imageAlbum = track.album.cover_big,
            preview = track.preview,
            titleAlbum = track.album.title,
            idAlbum = track.album.id
        )
    }

    // Reset data load state when needed
    fun resetDataLoadState() {
        dataLoaded1 = false
        dataLoaded2 = false
        dataLoaded3 = false
    }
}





