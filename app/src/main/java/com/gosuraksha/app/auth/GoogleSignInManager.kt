package com.gosuraksha.app.auth

import android.content.Context
import android.util.Log
import com.gosuraksha.app.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

private const val GOOGLE_SIGN_IN_TAG = "GoogleSignInFlow"

class GoogleSignInManager(
    context: Context
) {
    private val appContext = context.applicationContext
    private val googleClientId = appContext.getString(R.string.google_client_id)

    private val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestIdToken(googleClientId)
        .build()

    val client: GoogleSignInClient = GoogleSignIn.getClient(appContext, googleSignInOptions)

    init {
        val placeholderConfigured = googleClientId == "YOUR_GOOGLE_CLIENT_ID"
        Log.d(
            GOOGLE_SIGN_IN_TAG,
            "GoogleSignInClient initialized. requestEmail=true requestIdToken=true " +
                "clientIdLength=${googleClientId.length} placeholderClientId=$placeholderConfigured"
        )
        if (placeholderConfigured) {
            Log.e(
                GOOGLE_SIGN_IN_TAG,
                "google_client_id is still placeholder. Replace it with Web client ID from Google Cloud."
            )
        }
    }

    fun signOut(onComplete: (() -> Unit)? = null) {
        client.signOut().addOnCompleteListener {
            onComplete?.invoke()
        }
    }
}
