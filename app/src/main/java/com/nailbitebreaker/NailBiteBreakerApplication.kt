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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

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

        // 1. High priority UI-critical settings (Fast)
        Purchases.logLevel = LogLevel.DEBUG

        // 2. Use a dedicated background scope for staggered initialization.
        // This avoids blocking the main thread during the critical app launch window.
        val initScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        initScope.launch {
            // Configure RevenueCat (Network/Disk IO is internal, but we trigger it early)
            Purchases.configure(
                PurchasesConfiguration.Builder(this@NailBiteBreakerApplication, "goog_OffhHxMHhTRoSMOoLNbRKUfMQNz").build()
            )
            Purchases.sharedInstance.collectDeviceIdentifiers()
            
            // Allow Main thread to process first frame/layout
            yield()

            // 3. Start Agent System
            // computational tasks (like initial streak calculation) are offloaded
            orchestrator.start()

            // Allow UI to breathe
            yield()

            // 4. Initialize AdMob (IO-heavy initialization)
            withContext(Dispatchers.IO) {
                MobileAds.initialize(this@NailBiteBreakerApplication) {}
            }
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
