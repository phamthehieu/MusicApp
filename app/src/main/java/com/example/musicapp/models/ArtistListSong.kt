package com.example.musicapp.models

class ArtistListSong {
    var id: Int = 0
    var name: String = ""
    var link: String = ""
    var picture: String = ""
    var picture_small: String = ""
    var picture_medium: String = ""
    var picture_big: String = ""
    var picture_xl: String = ""
    var tracklist: String = ""
    var type: String = ""

    constructor()

    constructor(
        id: Int,
        name: String,
        link: String,
        picture: String,
        picture_small: String,
        picture_medium: String,
        picture_big: String,
        picture_xl: String,
        tracklist: String,
        type: String
    ) {
        this.id = id
        this.name = name
        this.link = link
        this.picture = picture
        this.picture_small = picture_small
        this.picture_medium = picture_medium
        this.picture_big = picture_big
        this.picture_xl = picture_xl
        this.tracklist = tracklist
        this.type = type
    }
}