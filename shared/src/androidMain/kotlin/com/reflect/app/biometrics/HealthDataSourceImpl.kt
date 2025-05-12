//// androidMain/src/.../HealthDataSourceImpl.kt
//package com.example.health
//
//import android.app.Activity
//import android.util.Log
//import com.google.android.gms.auth.api.signin.GoogleSignIn
//import com.google.android.gms.fitness.Fitness
//import com.google.android.gms.fitness.FitnessOptions
//import com.google.android.gms.fitness.data.DataPoint
//import com.google.android.gms.fitness.data.DataReadRequest
//import com.google.android.gms.fitness.data.DataType
//import com.google.android.gms.fitness.data.Field
//import com.google.android.gms.tasks.Tasks
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import kotlinx.datetime.Instant
//import java.util.concurrent.TimeUnit
//
//class HealthDataSourceImpl(private val activity: Activity) {
//  private val oneHourMillis = TimeUnit.HOURS.toMillis(1)
//
//  // ← All guaranteed DataType constants in play-services-fitness v21.x
//  private val FIT_TYPES = listOf(
//    DataType.TYPE_STEP_COUNT_DELTA           to Field.FIELD_STEPS,
//    DataType.TYPE_DISTANCE_DELTA             to Field.FIELD_DISTANCE,
//    DataType.TYPE_CALORIES_EXPENDED          to Field.FIELD_CALORIES,
//    DataType.TYPE_MOVE_MINUTES               to Field.FIELD_DURATION,
//    DataType.TYPE_ACTIVITY_SEGMENT           to Field.FIELD_ACTIVITY,
//    DataType.TYPE_HEART_RATE_BPM             to Field.FIELD_BPM,
//    DataType.TYPE_POWER_SAMPLE               to Field.FIELD_WATTS,
//    DataType.TYPE_LOCATION_SAMPLE            to Field.FIELD_LATITUDE,    // paired with LONGITUDE below
//    DataType.TYPE_BODY_TEMPERATURE           to Field.FIELD_BODY_TEMPERATURE,
//    DataType.TYPE_BASAL_BODY_TEMPERATURE     to Field.FIELD_BODY_TEMPERATURE,
//    DataType.TYPE_BLOOD_PRESSURE             to Field.FIELD_BLOOD_PRESSURE_SYSTOLIC, // also read DIASTOLIC
//    DataType.TYPE_BODY_FAT_PERCENTAGE        to Field.FIELD_PERCENTAGE,
//    DataType.TYPE_BMI                        to Field.FIELD_BMI,
//    DataType.TYPE_WEIGHT                     to Field.FIELD_WEIGHT,
//    DataType.TYPE_HEIGHT                     to Field.FIELD_HEIGHT,
//    DataType.TYPE_OXYGEN_SATURATION          to Field.FIELD_OXYGEN_SATURATION,
//    DataType.TYPE_BLOOD_GLUCOSE              to Field.FIELD_BLOOD_GLUCOSE,
//    DataType.TYPE_HYDRATION                  to Field.FIELD_VOLUME,
//    DataType.TYPE_NUTRITION                  to Field.FIELD_NUTRIENTS,
//    DataType.TYPE_SLEEP_SEGMENT              to Field.FIELD_SLEEP_SEGMENT_TYPE,
//    DataType.TYPE_WATER                      to Field.FIELD_VOLUME
//  )
//
//  private val fitnessOptions = FitnessOptions.builder().apply {
//    FIT_TYPES.forEach { (dt, _) -> addDataType(dt, FitnessOptions.ACCESS_READ) }
//  }.build()
//
//  /** Call this on start to show the Fit consent dialog */
//  suspend fun ensurePermissions() = withContext(Dispatchers.Main) {
//    val acct = GoogleSignIn.getLastSignedInAccount(activity)
//    if (acct == null || !GoogleSignIn.hasPermissions(acct, fitnessOptions)) {
//      GoogleSignIn.requestPermissions(activity, 1001, acct, fitnessOptions)
//    }
//  }
//
//  /** Reads & logs every supported type over the last hour */
//  suspend fun logAllFitData() = withContext(Dispatchers.IO) {
//    val now = System.currentTimeMillis()
//    val client = Fitness.getHistoryClient(activity, GoogleSignIn.getLastSignedInAccount(activity)!!)
//    for ((dataType, primaryField) in FIT_TYPES) {
//      val req = DataReadRequest.Builder()
//        .read(dataType)
//        .setTimeRange(now - oneHourMillis, now, TimeUnit.MILLISECONDS)
//        .build()
//
//      val result = Tasks.await(client.readData(req))
//      for (ds in result.dataSets) {
//        for (dp in ds.dataPoints) {
//          val ts = Instant.fromEpochMilliseconds(dp.getStartTime(TimeUnit.MILLISECONDS))
//          val primary = dp.getValue(primaryField)
//          val primaryVal = when (primary.format) {
//            Field.FORMAT_FLOAT -> primary.asFloat()
//            Field.FORMAT_INT32 -> primary.asInt().toFloat()
//            else -> Float.NaN
//          }
//          val details = when (dataType) {
//            DataType.TYPE_BLOOD_PRESSURE -> {
//              val dia = dp.getValue(Field.FIELD_BLOOD_PRESSURE_DIASTOLIC).asFloat()
//              "SYS=$primaryVal, DIA=$dia"
//            }
//            DataType.TYPE_LOCATION_SAMPLE -> {
//              val lon = dp.getValue(Field.FIELD_LONGITUDE).asFloat()
//              "LAT=$primaryVal, LON=$lon"
//            }
//            else -> primaryVal.toString()
//          }
//          Log.d("FitCheck", "${dataType.name} @ $ts → $details")
//        }
//      }
//    }
//  }
//}
