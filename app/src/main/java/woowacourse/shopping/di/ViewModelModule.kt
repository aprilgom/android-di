package woowacourse.shopping.di

import com.angrypig.autodi.autoDIModule.autoDIModule
import woowacourse.shopping.ui.MainViewModel
import woowacourse.shopping.ui.cart.CartViewModel

val viewModelModule = autoDIModule {
    viewModel { MainViewModel(inject("disposable"), inject("singleton")) }
    viewModel { CartViewModel(inject("singleton")) }
}
