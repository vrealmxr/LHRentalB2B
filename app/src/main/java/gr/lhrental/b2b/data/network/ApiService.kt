package gr.lhrental.b2b.data.network

import gr.lhrental.b2b.data.model.ApiEnvelope
import gr.lhrental.b2b.data.model.AvailabilityData
import gr.lhrental.b2b.data.model.CategoriesData
import gr.lhrental.b2b.data.model.ChangePasswordRequest
import gr.lhrental.b2b.data.model.CreateOrderRequest
import gr.lhrental.b2b.data.model.CreatedOrderData
import gr.lhrental.b2b.data.model.InvoicesData
import gr.lhrental.b2b.data.model.LoginData
import gr.lhrental.b2b.data.model.OrderDetailData
import gr.lhrental.b2b.data.model.OrdersData
import gr.lhrental.b2b.data.model.ProductData
import gr.lhrental.b2b.data.model.ProductsData
import gr.lhrental.b2b.data.model.UpdateProfileRequest
import gr.lhrental.b2b.data.model.UserEnvelopeData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Maps 1:1 onto the PHP endpoints under domains/lhrental.gr/public_html/b2b/api/
 * (see docs/API.md in this repo for the full contract).
 */
interface ApiService {

    @POST("auth/login.php")
    suspend fun login(@Body body: Map<String, String>): Response<ApiEnvelope<LoginData>>

    @POST("auth/logout.php")
    suspend fun logout(): Response<ApiEnvelope<Map<String, Boolean>>>

    @GET("auth/me.php")
    suspend fun me(): Response<ApiEnvelope<UserEnvelopeData>>

    @GET("categories/index.php")
    suspend fun categories(@Query("lang") lang: String): Response<ApiEnvelope<CategoriesData>>

    @GET("products/index.php")
    suspend fun products(
        @Query("lang") lang: String,
        @Query("category_id") categoryId: Int? = null,
        @Query("q") query: String? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 24,
        @Query("date_start") dateStart: String? = null,
        @Query("date_return") dateReturn: String? = null,
        @Query("min_price") minPrice: Double? = null,
        @Query("max_price") maxPrice: Double? = null,
        @Query("sort") sort: String? = null,
    ): Response<ApiEnvelope<ProductsData>>

    @GET("products/show.php")
    suspend fun product(
        @Query("id") id: Int,
        @Query("lang") lang: String,
        @Query("date_start") dateStart: String? = null,
        @Query("date_return") dateReturn: String? = null,
    ): Response<ApiEnvelope<ProductData>>

    /** Bulk re-check for the cart, keyed by a comma-separated id list. */
    @GET("products/availability.php")
    suspend fun availability(
        @Query("ids") ids: String,
        @Query("date_start") dateStart: String,
        @Query("date_return") dateReturn: String,
    ): Response<ApiEnvelope<AvailabilityData>>

    @GET("orders/index.php")
    suspend fun orders(): Response<ApiEnvelope<OrdersData>>

    @GET("orders/show.php")
    suspend fun order(@Query("id") id: Int, @Query("lang") lang: String): Response<ApiEnvelope<OrderDetailData>>

    @POST("orders/create.php")
    suspend fun createOrder(@Body body: CreateOrderRequest): Response<ApiEnvelope<CreatedOrderData>>

    @GET("invoices/index.php")
    suspend fun invoices(): Response<ApiEnvelope<InvoicesData>>

    @POST("account/update.php")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): Response<ApiEnvelope<UserEnvelopeData>>

    @POST("account/change_password.php")
    suspend fun changePassword(@Body body: ChangePasswordRequest): Response<ApiEnvelope<Map<String, Boolean>>>
}
