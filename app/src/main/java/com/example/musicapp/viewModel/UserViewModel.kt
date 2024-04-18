package com.example.musicapp.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.fireBase.RepositoryUsersFirebase
import kotlinx.coroutines.launch

class UserViewModel() : ViewModel() {

    private val repository = RepositoryUsersFirebase()

    private val _nameUser = MutableLiveData<String?>()
    val name: MutableLiveData<String?>
        get() = _nameUser

    private val _email = MutableLiveData<String?>()
    val email: MutableLiveData<String?>
        get() = _email

    private val _birthday = MutableLiveData<String?>()
    val birthday: MutableLiveData<String?>
        get() = _birthday

    private val _profileImage = MutableLiveData<String?>()
    val profileImage: MutableLiveData<String?>
        get() = _profileImage

    private val _timestamp = MutableLiveData<String?>()
    val timestamp: MutableLiveData<String?>
        get() = _timestamp

    private val _uid = MutableLiveData<String?>()
    val uid: MutableLiveData<String?>
        get() = _uid

    private val _userType = MutableLiveData<String?>()
    val userType: MutableLiveData<String?>
        get() = _userType

    private val _checkFingerprint = MutableLiveData<Boolean?>()
        val checkFingerprint: MutableLiveData<Boolean?>
        get() = _checkFingerprint

    private val _listArtistsId = MutableLiveData<List<*>?>()
    val listArtistsId: MutableLiveData<List<*>?>
        get() = _listArtistsId

    val updateResult: LiveData<Boolean> = repository.updateResult


    init {
        getUserData()
    }

    private fun getUserData() {
        viewModelScope.launch {
            val (name, email, birthday, profileImage, timestamp, uid, userType, checkFingerprint, listArtistsId) = repository.getUserData()
            _nameUser.value = name
            _email.value = email
            _birthday.value = birthday
            _profileImage.value = profileImage
            _timestamp.value = timestamp
            _uid.value = uid
            _userType.value = userType
            _checkFingerprint.value = checkFingerprint
            _listArtistsId.value = listArtistsId
        }
    }

    suspend fun updateUserData(name: String?, email: String?, birthday: String?, profileImage: String?, userType: String?, checkFingerprint: Boolean?, listArtistsId: List<*>?) {
        if (listArtistsId != null) {
            repository.updateUserData(name, email, birthday, profileImage, userType, checkFingerprint, listArtistsId)
        }
    }
}