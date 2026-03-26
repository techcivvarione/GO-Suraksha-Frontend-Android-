package com.gosuraksha.app.data.remote.dto.auth

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    val status: String,
    val data: T
)

data class LoginRequest(
    val identifier: String,
    val password: String
)

data class LoginResponse(
    @SerializedName("access_token")
    val access_token: String?,
    @SerializedName("token_type")
    val token_type: String?,
    @SerializedName("needs_phone_verification")
    val needs_phone_verification: Boolean? = null,
    @SerializedName("phone_verified")
    val phone_verified: Boolean? = null,
    @SerializedName("is_new_user")
    val is_new_user: Boolean? = null,
    @SerializedName("needs_terms_acceptance")
    val needs_terms_acceptance: Boolean? = null
)

data class GoogleAuthRequest(
    val google_id_token: String
)

data class SignupRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val confirm_password: String,
    val accepted_terms: Boolean,
    val terms_version: String = "v1",
    val privacy_version: String = "v1"
)

data class SignupResponse(
    val status: String
)

data class EmailRequest(val email: String)

data class PhoneOtpRequest(val phone: String)

data class VerifyPhoneOtpRequest(
    val phone: String,
    val otp: String
)

data class VerifyOtpRequest(
    val email: String,
    val otp: String
)

data class GenericResponse(val message: String)

data class VerifyOtpResponse(
    val access_token: String? = null,
    val token_type: String? = null,
    val needs_phone_verification: Boolean? = null,
    val phone_verified: Boolean? = null,
    val is_new_user: Boolean? = null
)

data class EmailOtpVerifyResponse(
    val success: Boolean? = null
)

data class RegisterDeviceRequest(
    val device_token: String,
    val device_type: String
)
