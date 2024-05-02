package com.example.musicapp.models


class SongListSearch {
    var id: Int = 0
    var readable: Boolean = false
    var title: String = ""
    var title_short: String = ""
    var title_version: String = ""
    var link: String = ""
    var duration: Int = 0
    var rank: Int = 0
    var explicit_lyrics: Boolean = false
    var explicit_content_lyrics: Int = 0
    var explicit_content_cover: Int = 0
    var preview: String = ""
    var md5_image: String = ""
    lateinit var artist: ArtistListSong
    lateinit var album: Album
    var type: String = ""

    constructor()

    constructor(
        id: Int,
        readable: Boolean,
        title: String,
        title_short: String,
        title_version: String,
        link: String,
        duration: Int,
        rank: Int,
        explicit_lyrics: Boolean,
        explicit_content_lyrics: Int,
        explicit_content_cover: Int,
        preview: String,
        md5_image: String,
        artist: ArtistListSong,
        album: Album,
        type: String
    ) {
        this.id = id
        this.readable = readable
        this.title = title
        this.title_short = title_short
        this.title_version = title_version
        this.link = link
        this.duration = duration
        this.rank = rank
        this.explicit_lyrics = explicit_lyrics
        this.explicit_content_lyrics = explicit_content_lyrics
        this.explicit_content_cover = explicit_content_cover
        this.preview = preview
        this.md5_image = md5_image
        this.artist = artist
        this.album = album
        this.type = type
    }


}
