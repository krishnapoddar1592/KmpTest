package com.reflect.app.android

// File: androidApp/src/main/java/com/emotionapp/android/GoogleSignInHelper.kt


import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status

class GoogleSignInHelper(private val context: Context) {

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("web_client_id") // Replace with your Web Client ID from Firebase Console
            .requestEmail()
            .build()

        GoogleSignIn.getClient(context, gso)
    }

    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    fun processResult(data: Intent?, onSuccess: (String) -> Unit, onError: (Exception) -> Unit) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            println(task.exception)
            val account = task.getResult(ApiException::class.java)
            println(account)
            val idToken = account.idToken ?: throw ApiException(Status.RESULT_INTERNAL_ERROR)
            println(account.idToken+" "+account.email)
            onSuccess(idToken)
        } catch (e: ApiException) {
            println(e.message)
            onError(e)
        } catch (e: Exception) {
            println(e.message)
            onError(e)
        }
    }

    fun signOut() {
        googleSignInClient.signOut().addOnCompleteListener {
            println("Google Sign-In client signed out: ${it.isSuccessful}")
        }

        // Also consider revoking access
        googleSignInClient.revokeAccess().addOnCompleteListener {
            println("Google Sign-In access revoked: ${it.isSuccessful}")
        }
    }
}
