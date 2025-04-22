// shared/src/androidMain/kotlin/com/reflect/app/data/remote/api/FirebaseAuthFactory.kt
package com.reflect.app.auth.service

import com.reflect.app.auth.service.AndroidFirebaseAuth

actual object FirebaseAuthFactory {
    actual fun createFirebaseAuth(): FirebaseAuthInterface = AndroidFirebaseAuth()
}

