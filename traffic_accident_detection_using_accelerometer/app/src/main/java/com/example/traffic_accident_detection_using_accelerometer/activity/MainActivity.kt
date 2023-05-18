package com.example.traffic_accident_detection_using_accelerometer.activity

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.traffic_accident_detection_using_accelerometer.R
import com.example.traffic_accident_detection_using_accelerometer.databinding.ActivityMainBinding
import com.example.traffic_accident_detection_using_accelerometer.model.*
import com.example.traffic_accident_detection_using_accelerometer.sendNotificationPack.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.math.*

object Constant{
    const val client = "client"
    const val fcmURL = "https://fcm.googleapis.com/"
    const val permissionId = 2
    const val channelId = "i.apps.notifications"
    const val channelName = "Test notification"
    const val  notificationId = 0
}
class MainActivity : AppCompatActivity() {
    companion object {
        var currentUser: User? = null
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var apiService: APIService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        fetchCurrentUser()
        verifyUserIsLoggedIn()
        binding = ActivityMainBinding.inflate(layoutInflater)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
        apiService = Client.getClient(Constant.fcmURL).create(APIService::class.java)
        binding.btnLocation.setOnClickListener {
            getLocation()
        }
        val view = binding.root
        setContentView(view)
    }


    private fun verifyUserIsLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, UsersSelectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else {
            updateToken()
        }
    }
    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val user = p0.getValue(User::class.java)
                currentUser = user
                currentUser?.let{

                    if(it.typeOfUser!=Constant.client){
                        binding.tvTitle.text = resources.getString(R.string.share_your_location)
                        binding.btnLocation.text = resources.getString(R.string.share_my_current_location)
                    }

                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })


    }

    private fun updateToken() {
        val firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        firebaseUser?.let {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Toast.makeText(
                        baseContext,
                        "Token Did not get Generated",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@OnCompleteListener
                }

                val refreshToken: String = task.result.toString()
                val token = Token(refreshToken)
                FirebaseDatabase.getInstance().getReference("Tokens")
                    .child(it.uid).setValue(token)
            })
        }

    }
    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) //check permission
        {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->

                    val location: Location? = task.result
                    if (location != null) {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            geocoder.getFromLocation(
                                location.latitude,
                                location.longitude, 1
                            ) { addresses ->
                                val getAddressLine = addresses[0].getAddressLine(0).toString()
                                val latitude = addresses[0].latitude.toString()
                                val longitude = addresses[0].longitude.toString()
                                val locality = addresses[0].locality.toString()
                                val countryName = addresses[0].countryName.toString()
                                setAndShareLocation(
                                    latitude,
                                    longitude,
                                    locality,
                                    countryName,
                                    getAddressLine
                                )
                            }
                        } else {

                            val list: List<Address> =
                                geocoder.getFromLocation(
                                    location.latitude,
                                    location.longitude,
                                    1
                                ) as List<Address>
                            val latitude = list[0].latitude.toString()
                            val longitude = list[0].longitude.toString()
                            val locality = list[0].locality.toString()
                            val countryName = list[0].countryName.toString()
                            val getAddressLine = list[0].getAddressLine(0).toString()
                            setAndShareLocation(
                                latitude,
                                longitude,
                                locality,
                                countryName,
                                getAddressLine
                            )
                        }
                    }
                    else{
                        Toast.makeText(this, "Location is not getting fetch", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    private fun setAndShareLocation(
        latitude: String,
        longitude: String,
        locality: String,
        countryName: String,
        getAddressLine: String
    ) {
        runOnUiThread {
            binding.apply {
                tvLatitude.text = resources.getString(R.string.set_latitude, latitude)
                tvLongitude.text =
                    resources.getString(R.string.set_longitude, longitude)
                tvCountryName.text =
                    resources.getString(R.string.set_locality, locality)
                tvLocality.text =
                    resources.getString(R.string.set_country_name, countryName)
                tvAddress.text =
                    resources.getString(R.string.set_address, getAddressLine)

            }
        }
        createNotificationChannel()
        currentUser?.let {
            if (it.typeOfUser != Constant.client) {
                val ref = FirebaseDatabase.getInstance()
                    .getReference("Location/${it.typeOfUser}/${countryName}/${locality}/${it.uid}")

                val user = DepLocation(
                    it.uid,
                    longitude,
                    latitude,
                    getAddressLine
                )
                ref.setValue(user)
                    .addOnSuccessListener {}
                    .addOnFailureListener {}
            } else {
                sendNotificationToSpecificUser(
                    countryName,
                    locality, latitude, longitude,it.username, getAddressLine
                )
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }


    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            Constant.permissionId
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode ==  Constant.permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constant.channelId,
                Constant.channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                lightColor = Color.BLUE
                enableLights(true)
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun sendNotificationToSpecificUser(
        countryName: String,
        locality: String,
        latitude: String,
        longitude: String,
        title: String,
        message: String
    ) {
        val departments = arrayOf( "Police", "FireMan", "Other","Hospital")
        for (i in departments) {
            val ref = FirebaseDatabase.getInstance()
                .getReference("/Location/$i/$countryName/$locality")
            var min1 = 100000000.00
            var minUid = ""
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    for (postSnapshot in p0.children) {
                        val depLocation = postSnapshot.getValue(DepLocation::class.java)
                        depLocation?.let{
                            val har = haversine(
                                latitude.toDouble(),
                                longitude.toDouble(),
                                it.latitude.toDouble(),
                                depLocation.longitude.toDouble()
                            )
                            if (min1 > har) {
                                min1 = har
                                minUid = depLocation.uid.toString()

                            }
                        }


                    }

                    if (minUid.trim().isNotEmpty()) {
                        val ref1 =
                            FirebaseDatabase.getInstance().getReference("/Tokens/$minUid")
                        ref1.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                snapshot.getValue(Token::class.java)
                                val token = snapshot.getValue(Token::class.java)
                                token?.let{
                                    sendNotification( token.token,
                                        title, message)
                                }

                            }

                            override fun onCancelled(error: DatabaseError) {}

                        })
                    }
                }

                override fun onCancelled(p0: DatabaseError) {}
            })
        }
    }

    fun haversine(
        lat: Double,
        lon: Double,
        destinationLatitude: Double,
        destinationLongitude: Double
    ): Double {
        val earthRadiusKm = 6372.8
        val dLat = Math.toRadians(destinationLatitude - lat)
        val dLon = Math.toRadians(destinationLongitude - lon)
        val originLat = Math.toRadians(lat)
        val destinationLat = Math.toRadians(destinationLatitude)

        val a =
            sin(dLat / 2).pow(2.toDouble()) + sin(dLon / 2).pow(2.toDouble()) * cos(originLat) * cos(
                destinationLat
            )
        val c = 2 * asin(sqrt(a))
        return earthRadiusKm * c
    }

    private fun sendNotification(userToken: String, title: String, message: String) {
        val data = Data(title, message)
        val sender = NotificationSender(data, userToken)
        apiService.sendNotification(sender)!!.enqueue(object : Callback<MyResponse?> {

            override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {
                if (response.code() == 200) {
                    if (response.body()!!.success != 1) {

                        Toast.makeText(this@MainActivity, "Failed ", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, response.code().toString(), Toast.LENGTH_LONG)
                        .show()
                }
            }

            override fun onFailure(call: Call<MyResponse?>, t: Throwable) {}
        })
    }



}
