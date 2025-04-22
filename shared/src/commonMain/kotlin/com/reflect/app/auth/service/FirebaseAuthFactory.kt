// FirebaseAuthFactory.kt
package com.reflect.app.auth.service

expect object FirebaseAuthFactory {
    fun createFirebaseAuth(): FirebaseAuthInterface
}