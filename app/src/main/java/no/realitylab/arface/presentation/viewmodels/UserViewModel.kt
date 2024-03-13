package no.realitylab.arface.presentation.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import no.realitylab.arface.data.models.UserData

class UserViewModel: ViewModel() {
    private val _userData = MutableLiveData<UserData>()
    val userData : MutableLiveData<UserData> = _userData

    fun updateUserModel(response: UserData) {
        _userData.value = response
    }

}