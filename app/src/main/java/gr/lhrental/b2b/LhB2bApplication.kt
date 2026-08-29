package gr.lhrental.b2b

import android.app.Application
import gr.lhrental.b2b.data.local.TokenStore
import gr.lhrental.b2b.data.network.NetworkModule
import gr.lhrental.b2b.data.repo.B2bRepository
import gr.lhrental.b2b.data.repo.CartStore
import gr.lhrental.b2b.data.repo.EventDatesStore

/**
 * Hand-rolled service locator — no DI framework yet. Fine at this size;
 * revisit (Hilt) once the screen count grows past what this can hold clearly.
 */
class LhB2bApplication : Application() {

    lateinit var tokenStore: TokenStore
        private set

    lateinit var repository: B2bRepository
        private set

    val cartStore = CartStore()
    val eventDatesStore = EventDatesStore()

    override fun onCreate() {
        super.onCreate()
        tokenStore = TokenStore(this)
        val httpClient = NetworkModule.createHttpClient(tokenStore)
        val api = NetworkModule.create(httpClient)
        repository = B2bRepository(api, httpClient, tokenStore, cacheDir = cacheDir)
    }
}
