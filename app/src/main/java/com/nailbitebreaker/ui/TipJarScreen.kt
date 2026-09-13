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
import com.revenuecat.purchases.ui.revenuecatui.Paywall
import com.revenuecat.purchases.ui.revenuecatui.PaywallOptions

const val TIP_JAR_ROUTE = "tip_jar"

@Composable
fun TipJarScreen(
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Paywall(
            options = PaywallOptions.Builder(
                dismissRequest = onBack
            ).build()
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


