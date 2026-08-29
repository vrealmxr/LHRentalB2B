package gr.lhrental.b2b.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Envelope every endpoint responds with — see backend/b2b/api/_bootstrap.php. */
@JsonClass(generateAdapter = true)
data class ApiEnvelope<T>(
    val ok: Boolean,
    val data: T? = null,
    val error: ApiError? = null,
)

@JsonClass(generateAdapter = true)
data class ApiError(
    val code: String,
    val message: String,
)

/**
 * Shape used to parse a FAILED response's body (Retrofit only auto-converts
 * response.body() for 2xx — an error response has to be read from
 * response.errorBody() and parsed with this instead). Ignores `data`, which
 * is absent/null on every error response anyway.
 */
@JsonClass(generateAdapter = true)
data class ApiErrorBody(
    val ok: Boolean = false,
    val error: ApiError? = null,
)

@JsonClass(generateAdapter = true)
data class LoginData(
    val token: String,
    @Json(name = "expires_at") val expiresAt: String,
    val user: User,
)

@JsonClass(generateAdapter = true)
data class User(
    val id: Int,
    val username: String,
    val email: String?,
    @Json(name = "company_name") val companyName: String?,
    @Json(name = "vat_number") val vatNumber: Int? = null,
    val address: String? = null,
    val city: String? = null,
    val postcode: Int? = null,
    val country: String? = null,
    @Json(name = "phone_number") val phoneNumber: String? = null,
    val profession: String? = null,
    @Json(name = "discount_percent") val discountPercent: Int? = null,
)

@JsonClass(generateAdapter = true)
data class UserEnvelopeData(val user: User)

/**
 * Every field optional — account/update.php only touches whatever keys are
 * actually present in the JSON body, so building this with only the
 * changed fields set (rest null) leaves everything else untouched.
 */
@JsonClass(generateAdapter = true)
data class UpdateProfileRequest(
    @Json(name = "company_name") val companyName: String? = null,
    val email: String? = null,
    @Json(name = "vat_number") val vatNumber: Int? = null,
    val address: String? = null,
    val city: String? = null,
    val postcode: Int? = null,
    val country: String? = null,
    @Json(name = "phone_number") val phoneNumber: String? = null,
    val profession: String? = null,
)

@JsonClass(generateAdapter = true)
data class ChangePasswordRequest(
    @Json(name = "current_password") val currentPassword: String,
    @Json(name = "new_password") val newPassword: String,
)

@JsonClass(generateAdapter = true)
data class CategoriesData(val categories: List<Category>)

@JsonClass(generateAdapter = true)
data class Category(
    val id: Int,
    val label: String,
    val link: String?,
    @Json(name = "image_url") val imageUrl: String?,
    @Json(name = "is_parent") val isParent: Boolean,
    val children: List<Category> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class ProductsData(
    val products: List<Product>,
    val pagination: Pagination,
)

@JsonClass(generateAdapter = true)
data class Pagination(
    val page: Int,
    @Json(name = "per_page") val perPage: Int,
    val total: Int,
    @Json(name = "total_pages") val totalPages: Int,
)

@JsonClass(generateAdapter = true)
data class Product(
    val id: Int,
    val sku: String,
    val name: String,
    val description: String? = null,
    val dimensions: String? = null,
    val pieces: Int? = null,
    @Json(name = "category_id") val categoryId: Int,
    @Json(name = "image_url") val imageUrl: String?,
    val price: Double,
    @Json(name = "is_waterproof") val isWaterproof: Boolean = false,
    @Json(name = "is_transferable") val isTransferable: Boolean = false,
    @Json(name = "is_sunproof") val isSunproof: Boolean = false,
    @Json(name = "has_people_capacity") val hasPeopleCapacity: Boolean = false,
    @Json(name = "people_capacity") val peopleCapacity: Int = 0,
    @Json(name = "on_outlet") val onOutlet: Boolean,
    @Json(name = "outlet_price") val outletPrice: Double?,
    /**
     * Present only when the request carried date_start/date_return —
     * how many are free for exactly that window (see api_reserved_quantities
     * in the backend). Null means "no dates chosen yet, availability
     * unknown", never "unlimited".
     */
    @Json(name = "available_quantity") val availableQuantity: Int? = null,
    /** A direct .glb URL when this product has a 3D scan, else null — see AR button on the detail screen. */
    @Json(name = "model_3d_url") val model3dUrl: String? = null,
) {
    /** The price the customer actually pays — outlet price when the item is on outlet. */
    val effectivePrice: Double get() = if (onOutlet && outletPrice != null) outletPrice else price
}

@JsonClass(generateAdapter = true)
data class ProductData(val product: Product)

@JsonClass(generateAdapter = true)
data class AvailabilityData(val availability: List<ProductAvailability>)

@JsonClass(generateAdapter = true)
data class ProductAvailability(
    @Json(name = "product_id") val productId: Int,
    val exists: Boolean,
    @Json(name = "available_quantity") val availableQuantity: Int,
)

@JsonClass(generateAdapter = true)
data class OrdersData(val orders: List<OrderSummary>)

@JsonClass(generateAdapter = true)
data class OrderSummary(
    val id: Int,
    val title: String,
    @Json(name = "event_type") val eventType: String?,
    @Json(name = "date_start") val dateStart: String?,
    @Json(name = "date_return") val dateReturn: String?,
    val location: String?,
    val stage: Int,
    @Json(name = "date_ordered") val dateOrdered: String?,
    @Json(name = "transportation_cost") val transportationCost: Double,
    @Json(name = "extra_cost") val extraCost: Double,
    @Json(name = "delay_cost") val delayCost: Double,
)

@JsonClass(generateAdapter = true)
data class OrderDetailData(val order: OrderDetail)

@JsonClass(generateAdapter = true)
data class OrderDetail(
    val id: Int,
    val title: String,
    @Json(name = "event_type") val eventType: String?,
    @Json(name = "date_start") val dateStart: String?,
    @Json(name = "date_return") val dateReturn: String?,
    val location: String?,
    val stage: Int,
    @Json(name = "date_ordered") val dateOrdered: String?,
    @Json(name = "transportation_cost") val transportationCost: Double,
    @Json(name = "extra_cost") val extraCost: Double,
    @Json(name = "delay_cost") val delayCost: Double,
    @Json(name = "delivery_address") val deliveryAddress: String?,
    @Json(name = "delivery_postcode") val deliveryPostcode: String?,
    @Json(name = "delivery_town") val deliveryTown: String?,
    val comments: String?,
    val items: List<OrderLineItem>,
)

@JsonClass(generateAdapter = true)
data class OrderLineItem(
    @Json(name = "product_id") val productId: Int,
    val sku: String?,
    val name: String,
    @Json(name = "image_url") val imageUrl: String?,
    @Json(name = "unit_price") val unitPrice: Double,
    val quantity: Int,
    @Json(name = "line_total") val lineTotal: Double,
)

@JsonClass(generateAdapter = true)
data class CreateOrderRequest(
    val title: String,
    @Json(name = "event_type") val eventType: String,
    @Json(name = "date_start") val dateStart: String,
    @Json(name = "date_return") val dateReturn: String,
    val location: String,
    val address: String,
    val postcode: String,
    val town: String,
    val comments: String,
    val items: List<CreateOrderItem>,
)

@JsonClass(generateAdapter = true)
data class CreateOrderItem(
    @Json(name = "product_id") val productId: Int,
    val quantity: Int,
)

@JsonClass(generateAdapter = true)
data class CreatedOrderData(val order: CreatedOrder)

@JsonClass(generateAdapter = true)
data class CreatedOrder(val id: Int, val total: Double)

@JsonClass(generateAdapter = true)
data class InvoicesData(val invoices: List<Invoice>)

@JsonClass(generateAdapter = true)
data class Invoice(
    val id: Int,
    @Json(name = "file_name") val fileName: String,
    @Json(name = "date_uploaded") val dateUploaded: String,
    @Json(name = "download_url") val downloadUrl: String,
)
