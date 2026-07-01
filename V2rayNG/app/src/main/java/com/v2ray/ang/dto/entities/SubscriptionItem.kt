package com.v2ray.ang.dto.entities

data class SubscriptionItem(
    var remarks: String = "",
    var url: String = "",
    var enabled: Boolean = true,
    val addedTime: Long = System.currentTimeMillis(),
    var lastUpdated: Long = -1,
    var autoUpdate: Boolean = false,
    var updateInterval: Long = 1440, // in minutes, default to 24 hours
    var prevProfile: String? = null,
    var nextProfile: String? = null,
    var filter: String? = null,
    var allowInsecureUrl: Boolean = false,
    var userAgent: String? = null,
    var upload: Long = 0L,
    var download: Long = 0L,
    var total: Long = 0L,
    var expire: Long = 0L
)

