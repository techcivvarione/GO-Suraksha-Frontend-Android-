package com.gosuraksha.app.scam.location

import android.Manifest
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

data class ScamLookupLocation(
    val lat: Double,
    val lng: Double,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null
)

object ScamLookupLocationProvider {

    suspend fun getLastKnownLocation(context: Context): ScamLookupLocation? {
        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val location = suspendCancellableCoroutine<com.google.android.gms.maps.model.LatLng?> { continuation ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { lastLocation ->
                    if (!continuation.isActive) return@addOnSuccessListener
                    continuation.resume(
                        lastLocation?.let {
                            com.google.android.gms.maps.model.LatLng(it.latitude, it.longitude)
                        }
                    )
                }
                .addOnFailureListener {
                    if (continuation.isActive) continuation.resume(null)
                }
        } ?: return null

        val address = reverseGeocode(context, location.latitude, location.longitude)
        return ScamLookupLocation(
            lat = location.latitude,
            lng = location.longitude,
            city = address?.locality?.takeIf { it.isNotBlank() },
            state = address?.adminArea?.takeIf { it.isNotBlank() },
            country = address?.countryName?.takeIf { it.isNotBlank() }
        )
    }

    private suspend fun reverseGeocode(
        context: Context,
        lat: Double,
        lng: Double
    ): Address? {
        if (!Geocoder.isPresent()) return null
        val geocoder = Geocoder(context, Locale.getDefault())
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine<Address?> { continuation ->
                    geocoder.getFromLocation(lat, lng, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            if (continuation.isActive) {
                                continuation.resume(addresses.firstOrNull())
                            }
                        }

                        override fun onError(errorMessage: String?) {
                            if (continuation.isActive) continuation.resume(null)
                        }
                    })
                }
            } else {
                withContext(Dispatchers.IO) {
                    geocoder.getFromLocation(lat, lng, 1)?.firstOrNull()
                }
            }
        }.getOrNull()
    }
}
