package com.example.model

data class CallSession(
    val id: String = "",
    val caller: String = "",
    val callee: String = "",
    val callerName: String = "",
    val video: Boolean = false,
    val offer: String = "",
    val answer: String = "",
    val status: String = "calling", // calling | ringing | connected | ended | rejected
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "caller" to caller,
            "callee" to callee,
            "callerName" to callerName,
            "video" to video,
            "offer" to offer,
            "answer" to answer,
            "status" to status,
            "timestamp" to timestamp
        )
    }

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>?): CallSession {
            if (map == null) return CallSession(id = id)
            return CallSession(
                id = id,
                caller = (map["caller"] as? String).orEmpty(),
                callee = (map["callee"] as? String).orEmpty(),
                callerName = (map["callerName"] as? String).orEmpty(),
                video = (map["video"] as? Boolean) ?: false,
                offer = (map["offer"] as? String).orEmpty(),
                answer = (map["answer"] as? String).orEmpty(),
                status = (map["status"] as? String) ?: "ended",
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: 0L
            )
        }
    }
}
