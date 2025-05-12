//// commonMain/src/.../HealthDataSource.kt
//package com.reflect.app.biometrics
//
//import kotlinx.datetime.Instant
//
//data class Sample<T>(val timestamp: Instant, val value: T)
//
//interface HealthDataSource {
//  suspend fun getHeartRate(): List<Sample<Double>>
//  suspend fun getHeartRateVariability(): List<Sample<Double>>
//  suspend fun getElectrocardiograms(): List<Sample<String>>      // ECG waveform as base64/JSON
//  suspend fun getSpO2(): List<Sample<Double>>
//  suspend fun getRespiratoryRate(): List<Sample<Double>>
//  suspend fun getBodyTemperature(): List<Sample<Double>>
//  suspend fun getBloodPressure(): List<Sample<Pair<Double,Double>>>  // systolic/diastolic
//  suspend fun getBloodGlucose(): List<Sample<Double>>
//  suspend fun getStepCount(): List<Sample<Int>>
//  suspend fun getDistanceWalkingRunning(): List<Sample<Double>>
//  suspend fun getFlightsClimbed(): List<Sample<Int>>
//  suspend fun getLocationSamples(): List<Sample<Pair<Double,Double>>> // lat/lon
//}
