package com.reflect.app.models


sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    sealed class Error : AuthResult() {
        object InvalidCredentials : Error()
        object NetworkError : Error()
        object UserCancelled : Error()
        data class Unknown(val message: String) : Error()
    }
}