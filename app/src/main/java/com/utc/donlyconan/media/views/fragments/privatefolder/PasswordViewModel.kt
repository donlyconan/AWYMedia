package com.utc.donlyconan.media.views.fragments.privatefolder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.utc.donlyconan.media.app.settings.Settings
import javax.inject.Inject

class PasswordViewModel : ViewModel() {

    companion object {

    }

    @Inject lateinit var settings: Settings

    private val _event = MutableLiveData<Result>()
    val event: LiveData<Result> get()  = _event


    fun verifyPassword(password: String) {
        if(password == settings.password) {
            _event.postValue(Result.OnSuccess)
        } else {
            _event.postValue(Result.OnFailure(true))
        }
    }

    fun verifyBiometric() {
        _event.postValue(Result.OnFailure(false))
    }

    sealed class Result {
        class OnFailure(usingPassword: Boolean): Result()
        object OnSuccess: Result()
    }

}