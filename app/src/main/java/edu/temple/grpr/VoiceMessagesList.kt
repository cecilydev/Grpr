package edu.temple.grpr

import android.speech.tts.Voice
import org.json.JSONArray

class VoiceMessagesList() {
    private val VMs = ArrayList<VoiceMessage>()

    fun add(vm: VoiceMessage){
        VMs.add(vm)
    }

    fun get(index: Int): VoiceMessage{
        return VMs[index]
    }

    fun getVMByTime(time: String): VoiceMessage{
        for (i in 0 until VMs.size){
            if (VMs.get(i).time==time) return VMs.get(i)
        }
        return VoiceMessage("", "", "")
    }

    fun createListFromJSONArray(vms: JSONArray){
        for (i in 0 until vms.length()){
            VMs.add(VoiceMessage(vms.getJSONObject(i)))
        }
    }

    fun size() = VMs.size
}