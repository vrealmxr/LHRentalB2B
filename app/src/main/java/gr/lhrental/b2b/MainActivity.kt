package gr.lhrental.b2b

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import gr.lhrental.b2b.ui.nav.LhNavGraph
import gr.lhrental.b2b.ui.theme.LhRentalB2bTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as LhB2bApplication

        setContent {
            var sessionChecked by mutableStateOf(false)
            var startLoggedIn by mutableStateOf(false)

            lifecycleScope.launch {
                startLoggedIn = app.tokenStore.tokenFlow.first() != null
                sessionChecked = true
            }

            LhRentalB2bTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (sessionChecked) {
                        LhNavGraph(
                            repository = app.repository,
                            cartStore = app.cartStore,
                            startLoggedIn = startLoggedIn,
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}
