package eu.tutorials.gooddeedproject.utils

import com.google.android.gms.maps.model.LatLng

// This is the updated function with all 16 locations
fun getLatLngForLocation(locationName: String): LatLng? {
    return when {
        // Old Locations
        locationName.contains("Upper Lake", ignoreCase = true) -> LatLng(23.2532, 77.3382)
        locationName.contains("DB City Mall", ignoreCase = true) -> LatLng(23.2330, 77.4301)
        locationName.contains("Arera Colony", ignoreCase = true) -> LatLng(23.2111, 77.4329)
        locationName.contains("Bhopal Railway Station", ignoreCase = true) -> LatLng(23.2672, 77.4129)
        locationName.contains("Shahpura", ignoreCase = true) -> LatLng(23.1952, 77.4264)
        locationName.contains("Bharat Bhavan", ignoreCase = true) -> LatLng(23.2470, 77.3925)
        locationName.contains("T.T. Nagar Stadium", ignoreCase = true) -> LatLng(23.2383, 77.4011)
        locationName.contains("Kolar Dam", ignoreCase = true) -> LatLng(22.9595, 77.3461)
        locationName.contains("Hamidia Hospital", ignoreCase = true) -> LatLng(23.2642, 77.3970)
        locationName.contains("Barkatullah University", ignoreCase = true) -> LatLng(23.2058, 77.4522)
        locationName.contains("MP Nagar", ignoreCase = true) -> LatLng(23.2351, 77.4332)

        // New Locations
        locationName.contains("Bhopal City Center", ignoreCase = true) -> LatLng(23.2361, 77.4350)
        locationName.contains("New Market", ignoreCase = true) -> LatLng(23.2370, 77.4032)
        locationName.contains("Ashoka Garden", ignoreCase = true) -> LatLng(23.2646, 77.4330)
        locationName.contains("Sethani Ghat", ignoreCase = true) -> LatLng(22.7533, 77.7249)
        locationName.contains("Old City, Bhopal", ignoreCase = true) -> LatLng(23.2591, 77.4126)
        locationName.contains("Aastha Old Age Home", ignoreCase = true) -> LatLng(23.1994, 77.4207)
        locationName.contains("District Court, Bhopal", ignoreCase = true) -> LatLng(23.2546, 77.4024)
        locationName.contains("Rural Bhopal Outskirts", ignoreCase = true) -> LatLng(23.1745, 77.3314)
        locationName.contains("Gauhar Mahal", ignoreCase = true) -> LatLng(23.2508, 77.3957)
        locationName.contains("Transport Nagar", ignoreCase = true) -> LatLng(23.2163, 77.4475)
        locationName.contains("Shahpura Park", ignoreCase = true) -> LatLng(23.2096, 77.4331)
        locationName.contains("Red Cross Hospital", ignoreCase = true) -> LatLng(23.2400, 77.4182)
        locationName.contains("Rahat Bhopal Warehouse", ignoreCase = true) -> LatLng(23.2180, 77.4505)

        else -> null // Return null if a location is not found
    }
}