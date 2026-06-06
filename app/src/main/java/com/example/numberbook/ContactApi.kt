package com.example.numberbook

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ContactApi {
    @POST("insertContact.php")
    suspend fun insertContact(@Body contact: Contact): Response<ApiResponse>

    @GET("getAllContacts.php")
    suspend fun getAllContacts(): Response<List<Contact>>

    @GET("searchContact.php")
    suspend fun searchContacts(@Query("keyword") keyword: String): Response<List<Contact>>
}
