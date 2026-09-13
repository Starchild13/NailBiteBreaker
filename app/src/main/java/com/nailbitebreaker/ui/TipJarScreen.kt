package com.nailbitebreaker.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offering
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.ui.revenuecatui.ExperimentalPreviewRevenueCatUIPurchasesAPI
import com.revenuecat.purchases.ui.revenuecatui.Paywall
import com.revenuecat.purchases.ui.revenuecatui.PaywallListener
import com.revenuecat.purchases.ui.revenuecatui.PaywallOptions

@OptIn(ExperimentalPreviewRevenueCatUIPurchasesAPI::class)
@Composable
fun TipJarScreen(
    onBack: () -> Unit
) {
    var offering by remember { mutableStateOf<Offering?>(null) }

    // Fetch the specific offering provided by the user
    LaunchedEffect(Unit) {
        Purchases.sharedInstance.getOfferingsWith(
            onError = { /* Log error if needed */ },
            onSuccess = { offerings ->
                // Try to find the specific offering ID, fallback to current
                offering = offerings["ofrng4d0b7afb17"] ?: offerings.current
            }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Paywall(
            options = PaywallOptions.Builder(
                dismissRequest = onBack
            )
                .apply {
                    offering?.let { setOffering(it) }
                }
                .setListener(
                    object : PaywallListener {
                        override fun onPurchaseCompleted(
                            customerInfo: CustomerInfo,
                            storeTransaction: StoreTransaction
                        ) {
                            // Optional: handle successful purchase
                            onBack()
                        }

                        override fun onRestoreCompleted(customerInfo: CustomerInfo) {
                            // Optional: handle restore
                        }
                    }
                )
                .build()
        )

        IconButton(
            onClick = onBack,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }
    }
}


