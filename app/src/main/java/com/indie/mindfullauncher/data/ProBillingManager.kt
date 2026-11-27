package com.indie.mindfullauncher.data

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ProBillingState(
    val isProUnlocked: Boolean = false,
    val formattedPrice: String = "$3.99",
    val isStoreConnected: Boolean = false,
    val statusMessage: String? = null
)

class ProBillingManager(
    context: Context,
    private val prefsRepo: PreferencesRepository
) : PurchasesUpdatedListener {

    private val _state = MutableStateFlow(
        ProBillingState(isProUnlocked = prefsRepo.isProUnlocked())
    )
    val state: StateFlow<ProBillingState> = _state.asStateFlow()

    private var productDetails: ProductDetails? = null

    private val billingClient: BillingClient = BillingClient.newBuilder(context.applicationContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .build()

    fun connectAndSync() {
        if (billingClient.isReady) {
            queryProductDetails()
            queryOwnedPurchases(showFeedback = false)
            return
        }
        runCatching {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        _state.update { it.copy(isStoreConnected = true) }
                        queryProductDetails()
                        queryOwnedPurchases(showFeedback = false)
                    } else {
                        _state.update { it.copy(isStoreConnected = false) }
                    }
                }

                override fun onBillingServiceDisconnected() {
                    _state.update { it.copy(isStoreConnected = false) }
                }
            })
        }
    }

    private fun queryProductDetails() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRO_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        billingClient.queryProductDetailsAsync(params) { result, detailsResult ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val details = detailsResult.productDetailsList?.firstOrNull { it.productId == PRO_PRODUCT_ID }
                productDetails = details
                val price = details?.oneTimePurchaseOfferDetails?.formattedPrice
                if (!price.isNullOrBlank()) {
                    _state.update { it.copy(formattedPrice = price) }
                }
            }
        }
    }

    fun launchPurchase(activity: Activity) {
        val details = productDetails
        if (!billingClient.isReady || details == null) {
            connectAndSync()
            _state.update {
                it.copy(statusMessage = "Không thể kết nối Google Play Store lúc này. Hãy kiểm tra tài khoản Google Play.")
            }
            return
        }
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .build()
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParams))
            .build()
        billingClient.launchBillingFlow(activity, flowParams)
    }

    fun restorePurchases() {
        if (!billingClient.isReady) {
            connectAndSync()
            _state.update {
                it.copy(statusMessage = "Đang kết nối lại Google Play Store...")
            }
            return
        }
        queryOwnedPurchases(showFeedback = true)
    }

    private fun queryOwnedPurchases(showFeedback: Boolean) {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        billingClient.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val owned = handlePurchases(purchases)
                if (showFeedback) {
                    _state.update {
                        it.copy(
                            statusMessage = if (owned) {
                                "Đã khôi phục Stillness Pro!"
                            } else {
                                "Chưa tìm thấy giao dịch Stillness Pro trên tài khoản này."
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases != null) {
                    val unlocked = handlePurchases(purchases)
                    if (unlocked) {
                        _state.update { it.copy(statusMessage = "Đã mở khóa Stillness Pro trọn đời!") }
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _state.update { it.copy(statusMessage = null) }
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                prefsRepo.setProUnlocked(true)
                _state.update {
                    it.copy(
                        isProUnlocked = true,
                        statusMessage = "Bạn đã sở hữu Stillness Pro!"
                    )
                }
            }
            else -> {
                _state.update {
                    it.copy(statusMessage = "Giao dịch chưa hoàn tất (${result.debugMessage}).")
                }
            }
        }
    }

    private fun handlePurchases(purchases: List<Purchase>): Boolean {
        var unlocked = false
        purchases.forEach { purchase ->
            if (PRO_PRODUCT_ID in purchase.products &&
                purchase.purchaseState == Purchase.PurchaseState.PURCHASED
            ) {
                unlocked = true
                if (!purchase.isAcknowledged) {
                    val ackParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient.acknowledgePurchase(ackParams) { }
                }
            }
        }
        if (unlocked) {
            prefsRepo.setProUnlocked(true)
            _state.update { it.copy(isProUnlocked = true) }
        }
        return unlocked
    }

    fun clearStatusMessage() {
        _state.update { it.copy(statusMessage = null) }
    }

    fun endConnection() {
        runCatching { billingClient.endConnection() }
    }

    companion object {
        const val PRO_PRODUCT_ID = "stillness_pro_lifetime"
    }
}
