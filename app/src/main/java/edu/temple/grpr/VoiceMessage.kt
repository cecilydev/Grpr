package edu.temple.grpr

import org.json.JSONObject
import java.io.Serializable

data class VoiceMessage(val username: String, val time: Long, val fileName: String): Serializable {
    constructor(voiceMessage: JSONObject) : this(
        voiceMessage.getString("username"),
        voiceMessage.getLong("time"),
        voiceMessage.getString("fileName")
    )

    fun VMAsJSON(): JSONObject {
        val json = JSONObject()
        json.put("username", this.username)
        json.put("time", this.time)
        json.put("fileName", this.fileName)
        return json
    }
}
