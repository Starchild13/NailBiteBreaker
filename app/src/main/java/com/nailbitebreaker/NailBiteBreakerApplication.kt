package com.nailbitebreaker

import android.app.Application
import com.nailbitebreaker.agents.AgentOrchestrator
import com.nailbitebreaker.data.HabitDatabase
import com.nailbitebreaker.data.HabitRepository
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.LogLevel
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Custom Application class that acts as the root dependency container.
 *
 * It wires up the database -> repository -> orchestrator chain using
 * lazy initialisation so objects are only created when first accessed.
 * The orchestrator is started in [onCreate] so all agents are live for
 * the entire app lifetime.
 */
class NailBiteBreakerApplication : Application() {

    /** Room database singleton — created lazily on first access. */
    val database: HabitDatabase by lazy {
        HabitDatabase.getInstance(this)
    }

    /** Repository wrapping the DAO. */
    val repository: HabitRepository by lazy {
        HabitRepository(database.urgEventDao())
    }

    /**
     * The central agent orchestrator that wires together all agents
     * via a shared coroutine event bus.
     */
    val orchestrator: AgentOrchestrator by lazy {
        AgentOrchestrator(repository)
    }

    override fun onCreate() {
        super.onCreate()
        // Start all agents when the application process is created.
        orchestrator.start()

        // Enable debug logs for RevenueCat during development
        Purchases.logLevel = LogLevel.DEBUG

        // Initialize RevenueCat
        Purchases.configure(
            PurchasesConfiguration.Builder(this, "goog_OffhHxMHhTRoSMOoLNbRKUfMQNz").build()
        )
        
        // For ad attribution & tracking
        Purchases.sharedInstance.collectDeviceIdentifiers()

        // Initialize AdMob
        val backgroundScope = CoroutineScope(Dispatchers.IO)
        backgroundScope.launch {
            MobileAds.initialize(this@NailBiteBreakerApplication) {}
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        // Gracefully stop all agents and cancel coroutine scopes.
        orchestrator.stop()
    }

    /**
     * Example of how to use RevenueCat AdMob tracking:
     *
     * In your Activity/Fragment:
     *
     * val adRequest = AdRequest.Builder().build()
     * InterstitialAd.loadAndTrack(
     *     context,
     *     "your-ad-unit-id",
     *     adRequest,
     *     object : InterstitialAdLoadCallback() {
     *         override fun onAdLoaded(ad: InterstitialAd) {
     *             ad.show(activity)
     *         }
     *     }
     * )
     */
}
