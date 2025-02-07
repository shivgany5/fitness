package com.example.try1

import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultRegistry
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.MethodChannel
import androidx.lifecycle.lifecycleScope
import androidx.activity.result.ActivityResultLauncher
import kotlinx.coroutines.launch
import androidx.health.connect.client.PermissionController
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.ActivityResultRegistryOwner
androidx.activity.result.contract.ActivityResultContract
androidx.activity.result.ActivityResultRegistryOwner



class MainActivity : FlutterActivity() {
    private val CHANNEL = "com.example.healthconnect"

    private val PERMISSIONS = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getWritePermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getWritePermission(StepsRecord::class)
    )

    private val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()
    private val requestPermissions = registerForActivityResult(requestPermissionActivityContract) { granted: Set<String> ->
        if (granted.containsAll(PERMISSIONS)) {
            Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MethodChannel(flutterEngine!!.dartExecutor, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "checkHealthConnectAvailability" -> checkHealthConnectAvailability(result)
                "requestHealthConnectPermissions" -> {
                    lifecycleScope.launch {
                        val healthConnectClient = HealthConnectClient.getOrCreate(this@MainActivity)
                        checkPermissionsAndRun(healthConnectClient)
                        result.success(null)
                    }
                }
                else -> result.notImplemented()
            }
        }

        lifecycleScope.launch {
            val healthConnectClient = HealthConnectClient.getOrCreate(this@MainActivity)
            checkPermissionsAndRun(healthConnectClient)
        }
    }

    private suspend fun checkPermissionsAndRun(healthConnectClient: HealthConnectClient) {
        try {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            if (granted.containsAll(PERMISSIONS)) {
                Toast.makeText(this, "Permissions already granted", Toast.LENGTH_SHORT).show()
            } else {
                requestPermissions.launch(PERMISSIONS)
            }
        } catch (exception: Exception) {
            Toast.makeText(this, "Failed to check permissions: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkHealthConnectAvailability(result: MethodChannel.Result) {
        val providerPackageName = "com.google.android.apps.healthdata"
        val availabilityStatus = HealthConnectClient.getSdkStatus(context, providerPackageName)
        
        when (availabilityStatus) {
            HealthConnectClient.SDK_UNAVAILABLE -> {
                result.success("Health Connect SDK is unavailable.")
            }
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                val uriString = "market://details?id=$providerPackageName&url=healthconnect%3A%2F%2Fonboarding"
                context.startActivity(
                    Intent(Intent.ACTION_VIEW).apply {
                        setPackage("com.android.vending")
                        data = Uri.parse(uriString)
                        putExtra("overlay", true)
                        putExtra("callerId", context.packageName)
                    }
                )
                result.success("Redirecting to Health Connect provider.")
            }
            else -> {
                result.success("Health Connect SDK is available.")
            }
        }
    }
}








































































































































// class MainActivity : FlutterActivity() {

//     private val CHANNEL = "com.example.healthconnect"

//     // Permissions set for Health Connect data
//     val PERMISSIONS = setOf(
//         HealthPermission.getReadPermission(HeartRateRecord::class),
//         HealthPermission.getWritePermission(HeartRateRecord::class),
//         HealthPermission.getReadPermission(StepsRecord::class),
//         HealthPermission.getWritePermission(StepsRecord::class)
//     )

    
//     private fun requestPermissions(result: MethodChannel.Result? = null) {
//         // Launch the request permissions process
//         requestPermissionLauncher.launch(PERMISSIONS.toTypedArray())
//         // Note: Permissions request result will be handled by the launcher callback defined above
//     }
    

//     private val requestPermissionLauncher = registerForActivityResult(
//         ActivityResultContracts.RequestMultiplePermissions()
//     ) { granted: Map<String, Boolean> ->  // Explicitly specify the type
//         if (granted.all { it.value }) {
//             Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
//         } else {
//             Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
//         }
//     }

//     override fun onCreate(savedInstanceState: Bundle?) {
//         super.onCreate(savedInstanceState)

//         // Set up the MethodChannel to communicate with Flutter
//         MethodChannel(flutterEngine!!.dartExecutor, CHANNEL).setMethodCallHandler { call, result ->
//             if (call.method == "checkHealthConnectAvailability") {
//                 checkHealthConnectAvailability(result)
//             } else if (call.method == "requestHealthConnectPermissions") {
//                 requestPermissions(result)
//             } else {
//                 result.notImplemented()
//             }
//         }

//         // Check and request permissions on startup
//         checkPermissions()
//     }

//     private fun checkPermissions() {
//         val healthConnectClient = HealthConnectClient.getOrCreate(this)
//         // Define permissions for health connect data
//         val PERMISSIONS = setOf(
//             HealthPermission.getReadPermission(HeartRateRecord::class),
//             HealthPermission.getWritePermission(HeartRateRecord::class),
//             HealthPermission.getReadPermission(StepsRecord::class),
//             HealthPermission.getWritePermission(StepsRecord::class),
//             // Add other permissions as needed
//         )
//         lifecycleScope.launch {
//             try {
//                 val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
//                 if (grantedPermissions.containsAll(PERMISSIONS)) {
//                     Toast.makeText(this@MainActivity, "Permissions already granted", Toast.LENGTH_SHORT).show()
//                 } else {
//                     requestPermissions()
//                 }
//             } catch (exception: Exception) {
//                 Toast.makeText(this@MainActivity, "Failed to check permissions: ${exception.message}", Toast.LENGTH_SHORT).show()
//             }
//         }

//         // Check if permissions are granted
//         healthConnectClient.permissionController.getGrantedPermissions()
//             .addOnSuccessListener { grantedPermissions ->
//                 if (grantedPermissions.containsAll(PERMISSIONS)) {
//                     Toast.makeText(this, "Permissions already granted", Toast.LENGTH_SHORT).show()
//                 } else {
//                     requestPermissions()
//                 }
//             }
//             .addOnFailureListener { exception ->
//                 Toast.makeText(this, "Failed to check permissions: ${exception.message}", Toast.LENGTH_SHORT).show()
//             }
//     }



//     private fun checkHealthConnectAvailability(result: MethodChannel.Result) {
//         val providerPackageName = "com.google.android.apps.healthdata"
//         val availabilityStatus = HealthConnectClient.getSdkStatus(context, providerPackageName)
        
//         when (availabilityStatus) {
//             HealthConnectClient.SDK_UNAVAILABLE -> {
//                 result.success("Health Connect SDK is unavailable.")
//             }
//             HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
//                 val uriString = "market://details?id=$providerPackageName&url=healthconnect%3A%2F%2Fonboarding"
//                 context.startActivity(
//                     Intent(Intent.ACTION_VIEW).apply {
//                         setPackage("com.android.vending")
//                         data = Uri.parse(uriString)
//                         putExtra("overlay", true)
//                         putExtra("callerId", context.packageName)
//                     }
//                 )
//                 result.success("Redirecting to Health Connect provider.")
//             }
//             else -> {
//                 val healthConnectClient = HealthConnectClient.getOrCreate(context)
//                 // Issue operations with healthConnectClient
//                 result.success("Health Connect SDK is available.")
//             }
//         }
//     }
//     // Function to check if permissions are granted and run health connect actions
//     suspend fun checkPermissionsAndRun(healthConnectClient: HealthConnectClient) {
//         val granted = healthConnectClient.permissionController.getGrantedPermissions()
//         if (granted.containsAll(PERMISSIONS)) {
//             // Permissions already granted, proceed with reading/writing data
//             // Implement data reading or writing logic here
//         } else {
//             requestPermissionLauncher.launch(PERMISSIONS.toTypedArray())
//         }
//     }
// }