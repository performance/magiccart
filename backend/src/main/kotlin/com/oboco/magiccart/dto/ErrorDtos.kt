package com.oboco.magiccart.dto

import java.time.LocalDateTime

data class ApiErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String?,
    val path: String
)

data class ValidationErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String?,
    val path: String,
    val fieldErrors: List<FieldError>?
)

data class FieldError(
    val field: String,
    val rejectedValue: Any?,
    val message: String?
)