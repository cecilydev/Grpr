package edu.temple.grpr

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class MapsFragment : Fragment() {

    var map: GoogleMap? = null
    var myMarker: Marker? = null
    var otherMarkers = mutableMapOf<String, Marker>()
    var bounds = emptySet<LatLng>()
    lateinit var username: String

    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        /*val sydney = LatLng(-34.0, 151.0)
        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng())*/
        map = googleMap
        val phl = LatLng(39.9, -75.2)
        map?.moveCamera(CameraUpdateFactory.newLatLng(phl))
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        username = Helper.user.get(this.requireContext()).username

        //viewmodel observer user
        ViewModelProvider(requireActivity())
            .get(LocationViewModel::class.java)
            .getMyLatLng()
            .observe(requireActivity()) {
                if (myMarker == null) {
                    myMarker = map?.addMarker(
                        MarkerOptions().position(it).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    )
                } else {
                    myMarker?.position = it
                }
                if (otherMarkers.isEmpty()) map?.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 14f))
            }

        //viewmodel observe others
        ViewModelProvider(requireActivity())
            .get(LocationViewModel::class.java)
            .getUsersLocations()
            .observe(requireActivity()){
                val keys = it.keys

                //reset bounds for bounding box
                bounds = emptySet()

                //for each item in map, update bounds, then if marker exists update location, otherwise add
                for (i in 0 until keys.size){
                    val key = keys.elementAt(i).toString()
                    val loc: LatLng = it[keys.elementAt(i)]!!
                    bounds= bounds.plus(loc)

                    if (otherMarkers.containsKey(key)) {
                        otherMarkers[key]!!.position = loc
                    }
                    else {
                        if (key!=username) otherMarkers[key] = map?.addMarker(MarkerOptions().position(loc).title(key))!!
                    }
                }

                //check if anyone isnt in group anymore, remove from map
                val removeKeys = (otherMarkers.keys).minus(keys)
                removeKeys.forEach {
                   otherMarkers[it]?.remove()
                   otherMarkers.remove(it)
                }

                //update map view
                map?.moveCamera(CameraUpdateFactory.newLatLngBounds(getBounds(), 100))
            }

    }

    fun getBounds() : LatLngBounds{
        val builder = LatLngBounds.builder()
        for (i in bounds.indices){
            builder.include(bounds.elementAt(i))
        }
        return builder.build()
    }
}