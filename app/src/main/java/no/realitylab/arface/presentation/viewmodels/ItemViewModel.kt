package no.realitylab.arface.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import no.realitylab.arface.data.models.Photo

class ItemViewModel: ViewModel() {
    private val _models = MutableLiveData<List<Photo>>()
    val models: LiveData<List<Photo>> = _models

    private val _loading = MutableLiveData<Boolean>(true)
    val loading: LiveData<Boolean> = _loading

    fun updateModels(newModels: List<Photo>) {
        _models.value = newModels
    }

    fun getPhotosFromFirebase() {
        val fireDatabase = FirebaseDatabase
            .getInstance("https://styleapp-50e33-default-rtdb.firebaseio.com/")
            .reference
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        _loading.value = true
        if (userId != null) {
            fireDatabase.child("users").child(userId).child("photos")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val photoList = mutableListOf<Photo>()
                        for (photoSnapshot in snapshot.children) {
                            val value = photoSnapshot.getValue(String::class.java)
                            if (value != null) {
                                photoList.add(Photo(value))
                            }
                        }
                        _models.value = photoList
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Manejar errores de consulta si es necesario
                    }
                })
            _loading.value = false
        }
    }
}