package com.example.musicapp.models

class ArtistsModel {

    var idArtists: String = ""
    var name: String = ""
    var profileImage: String = ""
    var timestamp: String = ""
    var uid: String = ""

    constructor()

    constructor(
        idArtists: String,
        name: String,
        profileImage: String,
        timestamp: String,
        uid: String
    ) {
        this.idArtists = idArtists
        this.name = name
        this.profileImage = profileImage
        this.timestamp = timestamp
        this.uid = uid
    }

}