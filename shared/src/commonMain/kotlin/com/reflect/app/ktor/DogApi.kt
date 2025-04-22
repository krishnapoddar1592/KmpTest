package com.reflect.app.ktor

import com.reflect.app.response.BreedResult

interface DogApi {
    suspend fun getJsonFromApi(): BreedResult
}
