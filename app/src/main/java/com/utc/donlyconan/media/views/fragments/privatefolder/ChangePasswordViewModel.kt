package com.utc.donlyconan.media.views.fragments.privatefolder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.utc.donlyconan.media.app.settings.Settings
import javax.inject.Inject

class ChangePasswordViewModel : ViewModel() {

    companion object {

    }

    @Inject lateinit var settings: Settings

    private val _event = MutableLiveData<Result>()
    val event: LiveData<Result> get()  = MutableLiveData<Result>()


    fun verifyPassword(password: String): Boolean {
        return true
    }

    fun checkEmail(email: String): Boolean {
        return true
    }

    fun checkPassword(password: String): Boolean {
        return true
    }


    fun register(email: String, password: String) {
        val resultOfEmail = checkEmail(email)
        val resultOfPassword = checkPassword(password)
        if( resultOfEmail and resultOfPassword) {
            // TODO save password
            _event.postValue(Result.OnSuccess)
        }  else {
            _event.postValue(Result.OnRegistrationFailure(resultOfEmail, resultOfPassword))
        }
    }

    sealed class Result {
        class OnRegistrationFailure(resultOfEmail: Boolean, resultOfPassword: Boolean): Result()
        object OnLoginFailure: Result()
        object OnSuccess: Result()
    }

}