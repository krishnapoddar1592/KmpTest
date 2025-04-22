// shared/src/iosMain/kotlin/com/reflect/app/auth/service/FirebaseAuthFactory.kt
package com.reflect.app.auth.service

/**
 * iOS implementation of FirebaseAuthFactory.
 * This is mainly here to satisfy the expect/actual pattern.
 */
actual object FirebaseAuthFactory {
    /**
     * Creates an instance of the iOS Firebase Auth interface.
     * Note: The actual Firebase auth operations are handled directly in Swift.
     */
    actual fun createFirebaseAuth(): FirebaseAuthInterface = IosFirebaseAuth()
}