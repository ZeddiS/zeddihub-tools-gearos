package com.zeddihub.gearos.data.api

import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Standalone fallback API endpointy.
 * Skutečné endpointy v `zeddihub-tools-website/api/watch/*` zatím neexistují (M1 cross-repo task).
 */
interface GearOsApi {

    @POST("api/watch/pair")
    suspend fun pair(@Body req: PairRequest): PairResponse

    @GET("api/watch/profile")
    suspend fun profile(@Header("Authorization") bearer: String): ProfileResponse

    @POST("api/watch/heartbeat")
    suspend fun heartbeat(
        @Header("Authorization") bearer: String,
        @Body req: HeartbeatRequest,
    ): HeartbeatResponse

    @POST("api/watch/panic")
    suspend fun panic(@Header("Authorization") bearer: String): PanicResponse
}

@JsonClass(generateAdapter = true)
data class PairRequest(val code: String, val deviceLabel: String)

@JsonClass(generateAdapter = true)
data class PairResponse(val token: String, val username: String, val expiresAt: Long)

@JsonClass(generateAdapter = true)
data class ProfileResponse(val username: String, val role: String, val avatarUrl: String?)

@JsonClass(generateAdapter = true)
data class HeartbeatRequest(val battery: Int, val phoneConnected: Boolean)

@JsonClass(generateAdapter = true)
data class HeartbeatResponse(val ok: Boolean, val pendingPushCount: Int = 0)

@JsonClass(generateAdapter = true)
data class PanicResponse(val ok: Boolean, val sessionsRevoked: Int = 0)
