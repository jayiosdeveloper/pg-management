package com.pg.management.data.room.remote

import com.pg.management.core.network.ApiEnvelope
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RoomApi {
    @GET("rooms")
    suspend fun list(
        @Query("status") status: String? = "all",
        @Query("q") q: String? = null,
    ): ApiEnvelope<List<RoomDto>>

    @GET("rooms/{id}")
    suspend fun get(@Path("id") id: String): ApiEnvelope<RoomDto>

    @POST("rooms")
    suspend fun create(@Body req: CreateRoomRequest): ApiEnvelope<RoomDto>

    @PATCH("rooms/{id}")
    suspend fun update(@Path("id") id: String, @Body req: UpdateRoomRequest): ApiEnvelope<RoomDto>

    @DELETE("rooms/{id}")
    suspend fun delete(@Path("id") id: String): ApiEnvelope<Unit>

    @POST("rooms/{id}/beds")
    suspend fun addBed(@Path("id") id: String, @Body req: BedInput): ApiEnvelope<RoomDto>

    @DELETE("rooms/{id}/beds/{bedId}")
    suspend fun removeBed(@Path("id") id: String, @Path("bedId") bedId: String): ApiEnvelope<Unit>
}
