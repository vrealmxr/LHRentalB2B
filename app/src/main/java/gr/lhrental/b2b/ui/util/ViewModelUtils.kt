package gr.lhrental.b2b.ui.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

/** Small helper so each screen doesn't hand-roll a ViewModelProvider.Factory. */
inline fun <reified VM : ViewModel> viewModelFactoryOf(crossinline build: () -> VM) = viewModelFactory {
    initializer { build() }
}
