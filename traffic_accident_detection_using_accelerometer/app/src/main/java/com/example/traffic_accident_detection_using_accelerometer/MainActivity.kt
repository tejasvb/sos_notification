package com.example.traffic_accident_detection_using_accelerometer

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.traffic_accident_detection_using_accelerometer.databinding.ActivityMainBinding
import com.example.traffic_accident_detection_using_accelerometer.model.DepLocation
import com.example.traffic_accident_detection_using_accelerometer.model.User
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
import com.vaibhavmojidra.demokotlin.SendNotificationPack.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class MainActivity : AppCompatActivity() {
    companion object {
        var currentUser: User? = null
        var latitude: String? = null
        var longitude: String? = null
        var countryName: String? = null
        var getAddressLine: String? = null
        var locality: String? = null
    }

    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val permissionId = 2
    private val CHANNEL_ID = "i.apps.notifications"
    private val CHANNEL_NAME = "Test notification"
    val NOTIF_ID = 0
    private lateinit var apiService: APIService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        fetchCurrentUser()
        verifyUserIsLoggedIn()
        val filter = IntentFilter()

        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService::class.java)

        mainBinding.btnLocation.setOnClickListener {

            getLocation()
            //  updateToken()
        }

    }

    private fun UpdateToken() {
        var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Toast.makeText(
                    baseContext,
                    "Token Did not get Generated",
                    Toast.LENGTH_SHORT
                ).show()
                return@OnCompleteListener
            }

            // fetching the token

            var refreshToken: String = task.result.toString()

            var token: Token = Token(refreshToken)

            FirebaseDatabase.getInstance().getReference("Tokens")
                .child(FirebaseAuth.getInstance().currentUser!!.getUid()).setValue(token)

        })


    }

    private fun sendNotificationToSpecificUser(title: String, message: String) {
        var departments = arrayOf("Hospital", "Police", "FireMan", "Other")
        for(i in departments){
            val ref = FirebaseDatabase.getInstance()
                .getReference("/Location/$i/${countryName}/${locality}")
            var min1 = 100000000.00
            var minUid = ""
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    for (postSnapshot in p0.children) {
                        val deplocation = postSnapshot.getValue(DepLocation::class.java)
                        val har = haversine(
                            latitude!!.toDouble(),
                            longitude!!.toDouble(),
                            deplocation!!.latitude!!.toDouble(),
                            deplocation!!.longitude!!.toDouble()
                        )
                        if (min1 > har) {
                            min1 = har
                            minUid = deplocation!!.uid!!.toString()

                        }

                    }

                    if (minUid.trim().isNotEmpty()) {
                        val ref1 =
                            FirebaseDatabase.getInstance().getReference("/Tokens/$minUid")
                        ref1.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val token = snapshot.getValue(Token::class.java)
                                sendNotification(
                                    token!!.token,
                                    title, message
                                )
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                        })
                    }

//


                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })


        }

    }

    fun haversine(lat: Double, lon: Double, deslat: Double, deslon: Double): Double {
        val earthRadiusKm: Double = 6372.8
        val dLat = Math.toRadians(deslat - lat);
        val dLon = Math.toRadians(deslon - lon);
        val originLat = Math.toRadians(lat);
        val destinationLat = Math.toRadians(deslat);

        val a = Math.pow(Math.sin(dLat / 2), 2.toDouble()) + Math.pow(
            Math.sin(dLon / 2),
            2.toDouble()
        ) * Math.cos(originLat) * Math.cos(destinationLat);
        val c = 2 * Math.asin(Math.sqrt(a));
        return earthRadiusKm * c;
    }

    private fun verifyUserIsLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, UsersSelectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        else{
            UpdateToken()
        }
    }
//    private fun updateToken() {
//        var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
//        var refreshToken: String = FirebaseMessaging.getInstance().token.toString()
//        var token: Token = Token(refreshToken)
//        FirebaseDatabase.getInstance().getReference("Tokens")
//            .child(FirebaseAuth.getInstance().getCurrentUser()!!.getUid()).setValue(token)
//    }

    private fun sendNotification(usertoken: String, title: String, message: String) {
        val data = Data(title, message)
        val sender: NotificationSender = NotificationSender(data, usertoken)
        apiService.sendNotifcation(sender)!!.enqueue(object : Callback<MyResponse?> {

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

            override fun onFailure(call: Call<MyResponse?>, t: Throwable?) {

            }
        })
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location != null) {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val list: List<Address> =
                            geocoder.getFromLocation(
                                location.latitude,
                                location.longitude,
                                1
                            ) as List<Address>
                        latitude = list[0].latitude.toString()
                        longitude = list[0].longitude.toString()
                        locality = list[0].locality.toString()
                        countryName = list[0].countryName.toString()
                        getAddressLine = list[0].getAddressLine(0).toString()

                        mainBinding.apply {
                            tvLatitude.text = "Latitude\n${list[0].latitude}"
                            tvLongitude.text = "Longitude\n${list[0].longitude}"
                            tvCountryName.text = "Country Name\n${list[0].countryName}"
                            tvLocality.text = "Locality\n${list[0].locality}"
                            tvAddress.text = "Address\n${list[0].getAddressLine(0)}"

                        }
                        val uid = FirebaseAuth.getInstance().uid!!
                        createNotificationChannel()

                        if (currentUser != null) {
                            if (currentUser!!.typeOfUser != "client") {
                                val ref = FirebaseDatabase.getInstance()
                                    .getReference("Location/${currentUser!!.typeOfUser}/${list[0].countryName}/${list[0].locality}/$uid")

                                val user = DepLocation(
                                    uid,
                                    list[0].longitude.toString(),
                                    list[0].latitude.toString(),
                                    list[0].getAddressLine(0)
                                )
                                ref.setValue(user)
                                    .addOnSuccessListener {

                                    }
                                    .addOnFailureListener {
                                    }
                            } else {

                                sendNotificationToSpecificUser(currentUser!!.username!!, getAddressLine!!)
                            }


                        }


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

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                lightColor = Color.BLUE
                enableLights(true)
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                val user = p0.getValue(User::class.java)
                currentUser = user


            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })


    }
}
