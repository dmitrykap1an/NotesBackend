package com.example.utils.dto.responses

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class JwtResponse(
    private val accessToken: String,
    private val refreshToken: String,
): JsonResponse()

    //private val type = "Bearer"


