package com.example.musicapp.models

class Album {
    var id: Int = 0
    var title: String = ""
    var cover: String = ""
    var cover_small: String = ""
    var cover_medium: String = ""
    var cover_big: String = ""
    var cover_xl: String = ""
    var md5_image: String = ""
    var tracklist: String = ""
    var type: String = ""

    constructor()
    constructor(
        id: Int,
        title: String,
        cover: String,
        cover_small: String,
        cover_medium: String,
        cover_big: String,
        cover_xl: String,
        md5_image: String,
        tracklist: String,
        type: String
    ) {
        this.id = id
        this.title = title
        this.cover = cover
        this.cover_small = cover_small
        this.cover_medium = cover_medium
        this.cover_big = cover_big
        this.cover_xl = cover_xl
        this.md5_image = md5_image
        this.tracklist = tracklist
        this.type = type
    }


}