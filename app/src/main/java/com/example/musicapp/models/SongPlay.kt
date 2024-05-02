package com.example.musicapp.models

data class SongPlay(
    var titleSong: String,
    var idArtists: String,
    var nameArtists: String,
    var imageAlbum: String,
    var preview: String,
    var checkIsPlay: Boolean
): java.io.Serializable