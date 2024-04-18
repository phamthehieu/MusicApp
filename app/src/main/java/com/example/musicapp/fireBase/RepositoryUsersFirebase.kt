package com.example.musicapp.fireBase

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RepositoryUsersFirebase {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val databaseReference = FirebaseDatabase.getInstance().getReference("Users")

    val updateResult: MutableLiveData<Boolean> = MutableLiveData()

    data class UserData(
        val name: String?,
        val email: String?,
        val birthday: String?,
        val profileImage: String?,
        val timestamp: String?,
        val uid: String?,
        val userType: String?,
        val checkFingerprint: Boolean?,
        val listArtistsId: List<*>?
    )

    suspend fun getUserData(): UserData {
        return withContext(Dispatchers.IO) {
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                val snapshot = databaseReference.child(firebaseUser.uid).get().await()
                val name = snapshot.child("name").value as? String
                val email = snapshot.child("email").value as? String
                val birthday = snapshot.child("birthday").value as? String
                val profileImage = snapshot.child("profileImage").value as? String
                val timestamp = snapshot.child("timestamp").value as? String
                val uid = snapshot.child("uid").value as? String
                val userType = snapshot.child("userType").value as? String
                val checkFingerprint = snapshot.child("checkFingerprint").value as? Boolean
                val listArtistsId = snapshot.child("listArtistsId").value as? List<*>
                UserData(name, email, birthday, profileImage, timestamp, uid, userType, checkFingerprint, listArtistsId)
            } else {
                UserData(null, null, null, null, null, null, null, null, null)
            }
        }
    }

    suspend fun updateUserData(name: String?, email: String?, birthday: String?, profileImage: String?, userType: String?, checkFingerprint: Boolean?, listArtistsId: List<*>) {
        withContext(Dispatchers.IO) {
            val firebaseUser = firebaseAuth.currentUser
            firebaseUser?.let { user ->
                val userReference = databaseReference.child(user.uid)
                val userDataMap = HashMap<String, Any?>()
                if (name != null) userDataMap["name"] = name
                if (email != null) userDataMap["email"] = email
                if (birthday != null) userDataMap["birthday"] = birthday
                if (profileImage != null) userDataMap["profileImage"] = profileImage
                if (userType != null) userDataMap["userType"] = userType
                 userDataMap["listArtistsId"] = listArtistsId
                userDataMap["checkFingerprint"] = checkFingerprint

                userReference.updateChildren(userDataMap)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            updateResult.postValue(true)
                        } else {
                            updateResult.postValue(false)
                        }
                    }
            }
        }
    }
}