package com.gosuraksha.app.auth

import android.content.Context
import com.gosuraksha.app.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

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

    fun signOut(onComplete: (() -> Unit)? = null) {
        client.signOut().addOnCompleteListener {
            onComplete?.invoke()
        }
    }
}
