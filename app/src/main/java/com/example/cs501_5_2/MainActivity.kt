package com.example.cs501_5_2

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.cs501_5_2.ui.theme.CS501_5_2Theme
import kotlin.math.*

class MainActivity : ComponentActivity(), SensorEventListener
{
    private lateinit var sensorManager: SensorManager
    private var magnetometer: Sensor? = null
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var _accuracy by mutableStateOf("Unknown")

    private var gyro_x by mutableStateOf(0.0)
    private var gyro_y by mutableStateOf(0.0)
    private var mag_x by mutableStateOf(0.0)
    private var mag_y by mutableStateOf(0.0)
    private var mag_z by mutableStateOf(0.0)

    private var accel_x by mutableStateOf(0.0)
    private var accel_y by mutableStateOf(0.0)
    private var accel_z by mutableStateOf(0.0)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        setContent {
            CS501_5_2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Compass(
                        modifier = Modifier.padding(innerPadding),
                        mag_x,mag_y,mag_z,
                        accel_x,accel_y,accel_z,
                        gyro_x,gyro_y
                    )
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { sensorEvent ->
            when (sensorEvent.sensor.type) {
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    mag_x = sensorEvent.values[0].toDouble()
                    mag_y = sensorEvent.values[1].toDouble()
                    mag_z = sensorEvent.values[2].toDouble()
                }

                Sensor.TYPE_ACCELEROMETER -> {
                    accel_x = sensorEvent.values[0].toDouble()
                    accel_y = sensorEvent.values[1].toDouble()
                    accel_z = sensorEvent.values[2].toDouble()
                }

                Sensor.TYPE_GYROSCOPE -> {
                    gyro_x = sensorEvent.values[0].toDouble()
                    gyro_y = sensorEvent.values[1].toDouble()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        _accuracy = when (accuracy) {
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "High"
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Medium"
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "Low"
            SensorManager.SENSOR_STATUS_UNRELIABLE -> "Unreliable"
            else -> "Unknown"
        }
    }

}



@Composable
fun Compass(modifier : Modifier,
            magX : Double, magY : Double, magZ : Double,
            accelX: Double, accelY: Double, accelZ: Double,
            gyroX : Double, gyroY: Double
){
    val heading = calculateCompassDirection(magX,magY,magZ,accelX,accelY,accelZ)
    Column(
        modifier = modifier
    ) {

        Text(
            text = "mag_x : $magX"
        )
        Text(
            text = "mag_y : $magY"
        )
        Text(
            text = "mag_z : $magZ"
        )
        Text(
            text = "Heading : $heading"
        )
        Text(
            text = "accelX : $accelX"
        )
        Text(
            text = "accelY : $accelY"
        )
        Text(
            text = "accelZ : $accelZ"
        )
        Text(
            text = "Roll : $gyroX"
        )
        Text(
            text = "Pitch : $gyroY"
        )
    }
}

fun calculateCompassDirection(
    magX: Double, magY: Double, magZ: Double,
    accelX: Double, accelY: Double, accelZ: Double
): Double {
    val mag = floatArrayOf(magX.toFloat(), magY.toFloat(), magZ.toFloat())
    val accel = floatArrayOf(accelX.toFloat(), accelY.toFloat(), accelZ.toFloat())

    val rotation = FloatArray(9)
    val orientation = FloatArray(3)

    if (SensorManager.getRotationMatrix(rotation, null, accel, mag)) {
        SensorManager.getOrientation(rotation, orientation)

        return (Math.toDegrees(orientation[0].toDouble()) + 360) % 360
    }

    return -1.0
}