package com.example.musicapp.models

object Check {

    data class Track1(
        val id: String,
        val title: String,
        val preview: String,
        val artist: Artist1,
        val album: Album1
    )

    data class Artist1(
        val id: String,
        val name: String
    )

    data class Album1(
        val cover_medium: String,
        val title: String,
        val id: String
    )

    data class ApiResponse(
        val data: List<Track1>,
        val total: String,
        val next: String
    )

    data class DataSong(
        val titleSong: String,
        val idArtists: String,
        val nameArtists: String,
        val imageAlbum: String,
        val preview: String,
        val titleAlbum: String,
        val idAlbum: String
    )

}