package edu.temple.grpr

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class LocationViewModel: ViewModel(){
    private val latLng: MutableLiveData<LatLng> by lazy {
        MutableLiveData()
    }

    fun getLatLng(): LiveData<LatLng> {
        return latLng
    }

    fun setLatLng(newLatLng: LatLng){
        this.latLng.value=newLatLng
    }
}
