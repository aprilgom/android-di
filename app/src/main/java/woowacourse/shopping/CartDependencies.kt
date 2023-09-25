package woowacourse.shopping

import com.dygames.di.DependencyInjector.inject
import com.dygames.di.dependencies
import com.dygames.di.lifecycle
import com.dygames.di.provider
import com.dygames.di.qualifier
import woowacourse.shopping.data.CartRepository
import woowacourse.shopping.data.DefaultCartRepository
import woowacourse.shopping.data.DefaultProductRepository
import woowacourse.shopping.data.ProductRepository
import woowacourse.shopping.data.Room
import woowacourse.shopping.data.ShoppingDatabase
import woowacourse.shopping.ui.MainViewModel
import woowacourse.shopping.ui.cart.CartActivity
import woowacourse.shopping.ui.cart.DateFormatter
import kotlin.reflect.typeOf

class CartDependencies {
    init {
        dependencies {
            lifecycle<CartApplication> {
                qualifier(Room()) {
                    provider {
                        ShoppingDatabase.getDatabase(inject()).cartProductDao()
                    }
                    provider<CartRepository>(typeOf<DefaultCartRepository>())
                }
            }
            lifecycle<MainViewModel> {
                provider<ProductRepository> { DefaultProductRepository() }
            }
            lifecycle<CartActivity> {
                provider<DateFormatter>(typeOf<DateFormatter>())
            }
        }
    }
}
