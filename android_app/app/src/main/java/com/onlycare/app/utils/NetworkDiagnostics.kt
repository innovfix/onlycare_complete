package com.onlycare.app.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL

/**
 * Network Diagnostics Utility
 * Deep network analysis to diagnose Agora connection issues
 */
object NetworkDiagnostics {
    
    private const val TAG = "NetworkDiagnostics"
    
    /**
     * Perform comprehensive network diagnostics
     */
    fun performFullDiagnostics(context: Context) {
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "🔍 NETWORK DIAGNOSTICS - FULL REPORT")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        
        checkNetworkConnectivity(context)
        checkNetworkType(context)
        checkNetworkCapabilities(context)
        checkActiveNetworkDetails(context)
        checkVPNStatus(context)
        checkDNSConfiguration(context)
        
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
    
    /**
     * Check basic network connectivity
     */
    private fun checkNetworkConnectivity(context: Context) {
        Log.d(TAG, "\n📡 NETWORK CONNECTIVITY STATUS:")
        
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            if (network == null) {
                Log.e(TAG, "   ❌ NO ACTIVE NETWORK")
                Log.e(TAG, "   📱 Device appears to be OFFLINE")
                return
            }
            
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            if (capabilities == null) {
                Log.e(TAG, "   ❌ Cannot get network capabilities")
                return
            }
            
            val isConnected = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                             capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            
            if (isConnected) {
                Log.i(TAG, "   ✅ Network is CONNECTED and VALIDATED")
            } else {
                Log.e(TAG, "   ❌ Network connected but NOT VALIDATED (no internet)")
                Log.e(TAG, "   ⚠️ This may indicate firewall blocking or captive portal")
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            if (networkInfo?.isConnected == true) {
                Log.i(TAG, "   ✅ Network is connected")
            } else {
                Log.e(TAG, "   ❌ No network connection")
            }
        }
    }
    
    /**
     * Check network type (WiFi, Mobile Data, Ethernet, etc.)
     */
    private fun checkNetworkType(context: Context) {
        Log.d(TAG, "\n📶 NETWORK TYPE:")
        
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return
            
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    Log.i(TAG, "   📡 Type: WiFi")
                    Log.w(TAG, "   ⚠️ WiFi may have firewall restrictions blocking Agora")
                    Log.w(TAG, "   💡 TIP: Try using mobile data to test if WiFi is blocking")
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    Log.i(TAG, "   📱 Type: Mobile Data (4G/5G)")
                    Log.i(TAG, "   ✅ Mobile data typically has fewer restrictions")
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    Log.i(TAG, "   🔌 Type: Ethernet")
                    Log.w(TAG, "   ⚠️ Corporate/office networks may block VoIP services")
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> {
                    Log.i(TAG, "   🔒 Type: VPN")
                    Log.w(TAG, "   ⚠️ VPN may affect Agora connectivity")
                }
                else -> {
                    Log.d(TAG, "   ❓ Type: Unknown")
                }
            }
        }
    }
    
    /**
     * Check detailed network capabilities
     */
    private fun checkNetworkCapabilities(context: Context) {
        Log.d(TAG, "\n🔧 NETWORK CAPABILITIES:")
        
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return
            
            Log.d(TAG, "   Internet Access: ${capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)}")
            Log.d(TAG, "   Validated: ${capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)}")
            Log.d(TAG, "   Not Restricted: ${capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)}")
            Log.d(TAG, "   Trusted: ${capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED)}")
            Log.d(TAG, "   Not Metered: ${capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)}")
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Log.d(TAG, "   Not Suspended: ${capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED)}")
            }
            
            // Check if network is restricted
            if (!capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)) {
                Log.e(TAG, "   ❌ NETWORK IS RESTRICTED!")
                Log.e(TAG, "   🚫 This may prevent Agora from connecting")
            }
            
            // Check bandwidth
            val downlink = capabilities.linkDownstreamBandwidthKbps
            val uplink = capabilities.linkUpstreamBandwidthKbps
            Log.d(TAG, "   Download Speed: ${downlink / 1024} Mbps")
            Log.d(TAG, "   Upload Speed: ${uplink / 1024} Mbps")
            
            if (downlink < 1000 || uplink < 500) {
                Log.w(TAG, "   ⚠️ Bandwidth may be insufficient for quality calls")
            }
        }
    }
    
    /**
     * Check active network details
     */
    private fun checkActiveNetworkDetails(context: Context) {
        Log.d(TAG, "\n🌐 ACTIVE NETWORK DETAILS:")
        
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            if (network != null) {
                val networkHandle = network.networkHandle
                Log.d(TAG, "   Network Handle: $networkHandle")
                
                val linkProperties = connectivityManager.getLinkProperties(network)
                if (linkProperties != null) {
                    Log.d(TAG, "   Interface Name: ${linkProperties.interfaceName}")
                    Log.d(TAG, "   DNS Servers: ${linkProperties.dnsServers.joinToString(", ")}")
                    Log.d(TAG, "   Domains: ${linkProperties.domains ?: "None"}")
                    
                    // Check if private DNS is enabled
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val privateDnsServerName = linkProperties.privateDnsServerName
                        if (privateDnsServerName != null) {
                            Log.d(TAG, "   Private DNS: $privateDnsServerName")
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Check VPN status
     */
    private fun checkVPNStatus(context: Context) {
        Log.d(TAG, "\n🔒 VPN STATUS:")
        
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return
            
            val isVPN = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
            if (isVPN) {
                Log.w(TAG, "   ⚠️ VPN IS ACTIVE")
                Log.w(TAG, "   💡 VPN may interfere with Agora connectivity")
                Log.w(TAG, "   💡 TIP: Try disabling VPN and test again")
            } else {
                Log.i(TAG, "   ✅ No VPN detected")
            }
        }
    }
    
    /**
     * Check DNS configuration
     */
    private fun checkDNSConfiguration(context: Context) {
        Log.d(TAG, "\n🌐 DNS CONFIGURATION:")
        
        try {
            // Try to resolve Agora domain
            val domain = "sd-rtn.com"
            Log.d(TAG, "   Testing DNS resolution for: $domain")
            
            val addresses = InetAddress.getAllByName(domain)
            if (addresses.isNotEmpty()) {
                Log.i(TAG, "   ✅ DNS resolution successful")
                addresses.forEach { addr ->
                    Log.d(TAG, "   IP: ${addr.hostAddress}")
                }
            } else {
                Log.e(TAG, "   ❌ DNS resolution failed - no addresses returned")
                Log.e(TAG, "   🚫 Cannot reach Agora servers - DNS blocked?")
            }
        } catch (e: Exception) {
            Log.e(TAG, "   ❌ DNS resolution FAILED: ${e.message}")
            Log.e(TAG, "   🚫 This indicates DNS blocking or network restriction")
            Log.e(TAG, "   💡 Firewall may be blocking Agora domains")
        }
    }
    
    /**
     * Test Agora server connectivity (async)
     */
    suspend fun testAgoraConnectivity(): AgoraConnectivityResult {
        return withContext(Dispatchers.IO) {
            val result = AgoraConnectivityResult()
            
            Log.d(TAG, "\n🔍 TESTING AGORA SERVER CONNECTIVITY:")
            
            // Test 1: DNS Resolution
            result.dnsResolvable = testDNSResolution()
            
            // Test 2: API Reachability
            result.apiReachable = testAPIReachability()
            
            // Test 3: Specific Agora domains
            result.sdRtnReachable = testDomain("sd-rtn.com")
            result.agoraApiReachable = testDomain("api.agora.io")
            
            Log.d(TAG, "\n📊 AGORA CONNECTIVITY SUMMARY:")
            Log.d(TAG, "   DNS Resolvable: ${if (result.dnsResolvable) "✅ YES" else "❌ NO"}")
            Log.d(TAG, "   API Reachable: ${if (result.apiReachable) "✅ YES" else "❌ NO"}")
            Log.d(TAG, "   sd-rtn.com: ${if (result.sdRtnReachable) "✅ YES" else "❌ NO"}")
            Log.d(TAG, "   api.agora.io: ${if (result.agoraApiReachable) "✅ YES" else "❌ NO"}")
            
            if (!result.isFullyReachable()) {
                Log.e(TAG, "\n🚫 AGORA SERVERS ARE NOT REACHABLE!")
                Log.e(TAG, "   ⚠️ This explains Error 110 (timeout)")
                Log.e(TAG, "   💡 Possible causes:")
                Log.e(TAG, "      • Firewall blocking Agora domains")
                Log.e(TAG, "      • Router blocking UDP ports (1080-1090, 4000-4030)")
                Log.e(TAG, "      • ISP blocking VoIP services")
                Log.e(TAG, "      • Corporate network restrictions")
                Log.e(TAG, "   💡 Solutions:")
                Log.e(TAG, "      • Try using mobile data instead of WiFi")
                Log.e(TAG, "      • Configure router to allow Agora ports")
                Log.e(TAG, "      • Use VPN to bypass restrictions")
            } else {
                Log.i(TAG, "\n✅ ALL AGORA SERVERS ARE REACHABLE")
            }
            
            result
        }
    }
    
    /**
     * Test DNS resolution for Agora
     */
    private fun testDNSResolution(): Boolean {
        return try {
            Log.d(TAG, "   Testing: DNS Resolution (sd-rtn.com)")
            val addresses = InetAddress.getAllByName("sd-rtn.com")
            val success = addresses.isNotEmpty()
            Log.d(TAG, "   Result: ${if (success) "✅ SUCCESS" else "❌ FAILED"}")
            success
        } catch (e: Exception) {
            Log.e(TAG, "   Result: ❌ FAILED - ${e.message}")
            false
        }
    }
    
    /**
     * Test Agora API reachability
     */
    private fun testAPIReachability(): Boolean {
        return try {
            Log.d(TAG, "   Testing: HTTPS Connection (api.agora.io)")
            val url = URL("https://api.agora.io/")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.requestMethod = "HEAD"
            
            val responseCode = connection.responseCode
            connection.disconnect()
            
            val success = responseCode in 200..399
            Log.d(TAG, "   Result: ${if (success) "✅ SUCCESS (HTTP $responseCode)" else "❌ FAILED (HTTP $responseCode)"}")
            success
        } catch (e: Exception) {
            Log.e(TAG, "   Result: ❌ FAILED - ${e.message}")
            false
        }
    }
    
    /**
     * Test specific domain reachability
     */
    private fun testDomain(domain: String): Boolean {
        return try {
            Log.d(TAG, "   Testing: $domain")
            val address = InetAddress.getByName(domain)
            val reachable = address.isReachable(5000)
            Log.d(TAG, "   Result: ${if (reachable) "✅ REACHABLE" else "⚠️ NOT REACHABLE (may be normal)"}")
            // Note: Many servers don't respond to ICMP ping, so this might fail even if accessible
            true // Consider DNS resolution success as reachable
        } catch (e: Exception) {
            Log.e(TAG, "   Result: ❌ FAILED - ${e.message}")
            false
        }
    }
    
    /**
     * Get network type string
     */
    fun getNetworkTypeString(context: Context): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return "NONE"
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return "UNKNOWN"
            
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile Data"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN"
                else -> "UNKNOWN"
            }
        }
        
        @Suppress("DEPRECATION")
        return connectivityManager.activeNetworkInfo?.typeName ?: "UNKNOWN"
    }
}

/**
 * Agora connectivity test result
 */
data class AgoraConnectivityResult(
    var dnsResolvable: Boolean = false,
    var apiReachable: Boolean = false,
    var sdRtnReachable: Boolean = false,
    var agoraApiReachable: Boolean = false
) {
    fun isFullyReachable() = dnsResolvable && apiReachable
}






