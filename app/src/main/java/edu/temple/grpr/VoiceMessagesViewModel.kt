package edu.temple.grpr

import android.speech.tts.Voice
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VoiceMessagesViewModel: ViewModel() {

    private val voiceMessages by lazy{
        VoiceMessagesList()
    }

    private val obsVoiceMessages by lazy{
        MutableLiveData<VoiceMessagesList>()
    }

    //setter to add new VM
    fun addVM(vm: VoiceMessage){
        voiceMessages.add(vm)
        this.obsVoiceMessages.value = voiceMessages
    }

    fun getVMsToObserve(): LiveData<VoiceMessagesList>{
        return obsVoiceMessages
    }

    fun getVMs(): VoiceMessagesList{
        return voiceMessages
    }

}