package com.example.cooksmart.utils

import androidx.lifecycle.MutableLiveData
import com.example.cooksmart.infra.net.SmartNet

class DataFetcher(private val smartNet: SmartNet) {

    fun fetchRecipeText(question: String, responseState: MutableLiveData<String>) {
        smartNet.makeCall("get_answer", question) {
            responseState.postValue(it)
        }
    }

    fun fetchImageUrl(question: String, imageUrlState: MutableLiveData<String>) {
        smartNet.makeCall("get_images", question) {
            imageUrlState.postValue(it)
        }
    }
}
