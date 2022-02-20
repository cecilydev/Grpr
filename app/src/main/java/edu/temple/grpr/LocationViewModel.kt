package edu.temple.grpr

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class LocationViewModel: ViewModel(){

    private val myLatLng: MutableLiveData<LatLng> by lazy {
        MutableLiveData()
    }

    fun getMyLatLng(): LiveData<LatLng> {
        return myLatLng
    }

    fun setMyLatLng(newLatLng: LatLng){
        this.myLatLng.value=newLatLng
    }

    private val usersLocations : MutableMap<String, LatLng> by lazy {
        mutableMapOf()
    }

    fun getUserLoc(username: String): LatLng? {
        return this.usersLocations[username]
    }

    fun setUserLoc(username: String, loc: LatLng){
        this.usersLocations[username] = loc
    }


}
