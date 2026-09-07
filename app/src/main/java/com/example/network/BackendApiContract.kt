package com.example.network

import com.example.webrtc.TurnServerConfig
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class CreateMeetingRequest(
    val title: String,
    val description: String?,
    val accessPolicy: String, // ANYONE_WITH_LINK, ASK_TO_JOIN, INVITED_ONLY
    val passwordHash: String?,
    val allowQuickJoin: Boolean,
    val allowGuests: Boolean,
    val allowChat: Boolean,
    val allowScreenShare: Boolean,
    val allowRecording: Boolean
)

data class CreateMeetingResponse(
    val meetingId: String,
    val meetingCode: String,
    val meetingLink: String,
    val hostToken: String,
    val createdAt: Long,
    val expiresAt: Long
)

data class MeetingTokenRequest(
    val meetingId: String,
    val passcode: String?,
    val displayName: String
)

data class MeetingTokenResponse(
    val accessToken: String,
    val role: String, // HOST, CO_HOST, PARTICIPANT, GUEST
    val turnServers: List<TurnServerConfig>,
    val signalingUrl: String,
    val meetingTitle: String
)

data class ReportAbuseRequest(
    val meetingId: String,
    val reportedUserId: String,
    val reason: String, // SPAM, HARASSMENT, INAPPROPRIATE, IMPERSONATION, OTHER
    val details: String?
)

interface BackendApiContract {
    @POST("/api/v1/meetings")
    suspend fun createMeeting(
        @Body request: CreateMeetingRequest
    ): Response<CreateMeetingResponse>

    @POST("/api/v1/meetings/{meetingId}/token")
    suspend fun generateMeetingToken(
        @Path("meetingId") meetingId: String,
        @Body request: MeetingTokenRequest
    ): Response<MeetingTokenResponse>

    @GET("/api/v1/webrtc/turn-credentials")
    suspend fun getTurnCredentials(): Response<List<TurnServerConfig>>

    @POST("/api/v1/moderation/report")
    suspend fun reportParticipant(
        @Body request: ReportAbuseRequest
    ): Response<Unit>
}
