package gr.lhrental.b2b.data.repo

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import gr.lhrental.b2b.data.local.TokenStore
import gr.lhrental.b2b.data.model.ApiEnvelope
import gr.lhrental.b2b.data.model.ApiErrorBody
import gr.lhrental.b2b.data.model.Category
import gr.lhrental.b2b.data.model.ChangePasswordRequest
import gr.lhrental.b2b.data.model.ProductAvailability
import gr.lhrental.b2b.data.model.CreateOrderRequest
import gr.lhrental.b2b.data.model.CreatedOrder
import gr.lhrental.b2b.data.model.Invoice
import gr.lhrental.b2b.data.model.OrderDetail
import gr.lhrental.b2b.data.model.OrderSummary
import gr.lhrental.b2b.data.model.Pagination
import gr.lhrental.b2b.data.model.Product
import gr.lhrental.b2b.data.model.UpdateProfileRequest
import gr.lhrental.b2b.data.model.User
import gr.lhrental.b2b.data.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Response
import java.io.File
import java.io.IOException

class B2bRepository(
    private val api: ApiService,
    private val httpClient: OkHttpClient,
    private val tokenStore: TokenStore,
    private val cacheDir: File,
) {
    /** Locale sent to every endpoint — matches Localization::SUPPORTED_LOCALES server-side. */
    var locale: String = "gr"

    suspend fun login(username: String, password: String): ApiResult<User> {
        return when (val result = unwrap(safeCall { api.login(mapOf("username" to username, "password" to password)) }) { it }) {
            is ApiResult.Success -> {
                tokenStore.save(result.value.token)
                ApiResult.Success(result.value.user)
            }
            is ApiResult.Failure -> result
        }
    }

    suspend fun logout() {
        safeCall { api.logout() }
        tokenStore.clear()
    }

    suspend fun me(): ApiResult<User> =
        unwrap(safeCall { api.me() }) { it.user }

    suspend fun categories(): ApiResult<List<Category>> =
        unwrap(safeCall { api.categories(locale) }) { it.categories }

    suspend fun products(
        categoryId: Int?,
        query: String?,
        page: Int,
        dates: EventDates?,
        filters: ProductFilters = ProductFilters(),
    ): ApiResult<Pair<List<Product>, Pagination>> =
        unwrap(safeCall {
            api.products(
                lang = locale,
                categoryId = categoryId,
                query = query,
                page = page,
                dateStart = dates?.startIso,
                dateReturn = dates?.endIso,
                minPrice = filters.minPrice,
                maxPrice = filters.maxPrice,
                sort = filters.sort.apiValue,
            )
        }) { it.products to it.pagination }

    suspend fun product(id: Int, dates: EventDates?): ApiResult<Product> =
        unwrap(safeCall { api.product(id, locale, dates?.startIso, dates?.endIso) }) { it.product }

    /** Bulk re-check, e.g. right before checkout or after the customer edits the event dates. */
    suspend fun checkAvailability(productIds: List<Int>, dates: EventDates): ApiResult<List<ProductAvailability>> =
        unwrap(safeCall { api.availability(productIds.joinToString(","), dates.startIso, dates.endIso) }) { it.availability }

    suspend fun orders(): ApiResult<List<OrderSummary>> =
        unwrap(safeCall { api.orders() }) { it.orders }

    suspend fun order(id: Int): ApiResult<OrderDetail> =
        unwrap(safeCall { api.order(id, locale) }) { it.order }

    suspend fun createOrder(request: CreateOrderRequest): ApiResult<CreatedOrder> =
        unwrap(safeCall { api.createOrder(request) }) { it.order }

    suspend fun invoices(): ApiResult<List<Invoice>> =
        unwrap(safeCall { api.invoices() }) { it.invoices }

    suspend fun updateProfile(request: UpdateProfileRequest): ApiResult<User> =
        unwrap(safeCall { api.updateProfile(request) }) { it.user }

    suspend fun changePassword(currentPassword: String, newPassword: String): ApiResult<Unit> =
        unwrap(safeCall { api.changePassword(ChangePasswordRequest(currentPassword, newPassword)) }) { }

    /**
     * Downloads one invoice PDF through the authenticated OkHttp client
     * (the bearer token has to be on the request — this is not a browser
     * link) and caches it under cacheDir/invoices for the caller to open
     * via FileProvider.
     */
    suspend fun downloadInvoice(invoice: Invoice): ApiResult<File> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(invoice.downloadUrl).build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext ApiResult.Failure("Δεν ήταν δυνατή η λήψη του τιμολογίου (${response.code}).")
                }
                val dir = File(cacheDir, "invoices").apply { mkdirs() }
                val file = File(dir, "invoice-${invoice.id}.pdf")
                response.body?.byteStream()?.use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                } ?: return@withContext ApiResult.Failure("Άδεια απάντηση από τον διακομιστή.")
                ApiResult.Success(file)
            }
        } catch (e: IOException) {
            ApiResult.Failure("Δεν ήταν δυνατή η σύνδεση με τον διακομιστή.")
        }
    }

    /** Reactive session state — collect to know when the user is logged in. */
    val tokenFlow get() = tokenStore.tokenFlow

    /** Whether the customer has opted in to biometric unlock on this device. */
    val biometricEnabledFlow get() = tokenStore.biometricEnabledFlow

    suspend fun setBiometricEnabled(enabled: Boolean) = tokenStore.setBiometricEnabled(enabled)

    // ---- plumbing -----------------------------------------------------

    private suspend fun <T> safeCall(call: suspend () -> Response<ApiEnvelope<T>>): Response<ApiEnvelope<T>>? {
        return try {
            call()
        } catch (e: IOException) {
            null // network error — surfaced as a generic failure by unwrap()
        }
    }

    private val errorBodyAdapter = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
        .adapter(ApiErrorBody::class.java)

    /**
     * Retrofit only converts response.body() for 2xx responses — for
     * anything else (401, 422, 500…) body() is null and the real payload
     * sits in errorBody(), unparsed. This reads that error body so the
     * server's actual reason (via friendlyErrorMessage) reaches the user
     * instead of a bare status code.
     */
    private fun <T, R> unwrap(response: Response<ApiEnvelope<T>>?, onSuccess: (T) -> R): ApiResult<R> {
        if (response == null) {
            return ApiResult.Failure("Δεν ήταν δυνατή η σύνδεση με τον διακομιστή. Ελέγξτε τη σύνδεσή σας.")
        }

        if (!response.isSuccessful) {
            val errorBody = try {
                response.errorBody()?.string()?.let { errorBodyAdapter.fromJson(it) }
            } catch (e: Exception) {
                null
            }
            return ApiResult.Failure(
                friendlyErrorMessage(errorBody?.error?.code, errorBody?.error?.message, response.code())
            )
        }

        val envelope = response.body()
        if (envelope == null || !envelope.ok || envelope.data == null) {
            return ApiResult.Failure(
                friendlyErrorMessage(envelope?.error?.code, envelope?.error?.message, response.code())
            )
        }
        return ApiResult.Success(onSuccess(envelope.data))
    }
}
