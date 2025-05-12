package com.reflect.app.models

import androidx.lifecycle.ViewModel


expect abstract class ViewModel() : ViewModel {
    protected override fun onCleared()
}