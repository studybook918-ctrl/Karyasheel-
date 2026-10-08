package com.example.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.model.UserEntity
import com.example.repository.MarketplaceRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

sealed class AuthResult {
    data class Success(val user: FirebaseUser) : AuthResult()
    data class Error(val message: String, val rawException: Exception? = null) : AuthResult()
    object Cancelled : AuthResult()
}

/**
 * Production Firebase Authentication Service
 * Implements:
 * 1. Google Sign-In via Jetpack CredentialManager + GetGoogleIdOption (Zero Mock, Real Firebase Auth)
 * 2. Real Email & Password Login (signInWithEmailAndPassword)
 * 3. Real Email & Password Account Registration (createUserWithEmailAndPassword)
 * 4. Password Reset (sendPasswordResetEmail)
 * 5. Sign Out (FirebaseAuth.signOut)
 * 6. User-friendly Hindi/English error translations
 */
class FirebaseAuthService(private val context: Context) {
    private val TAG = "FirebaseAuthService"
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val credentialManager: CredentialManager = CredentialManager.create(context)

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    /**
     * Sign In with Google using Jetpack Credential Manager
     * Server Client ID is dynamically resolved or uses default Web Client ID from Google Services.
     */
    suspend fun signInWithGoogle(activityContext: Context, webClientId: String? = null): AuthResult {
        return try {
            val serverClientId = webClientId?.ifBlank { null }
                ?: getWebClientIdFromResources(context)
                ?: "953201663798-apps.googleusercontent.com" // Project number based fallback

            // Hash a raw nonce for replay-protection
            val rawNonce = UUID.randomUUID().toString()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(rawNonce.toByteArray())
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .setNonce(hashedNonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            Log.d(TAG, "Requesting credentials with serverClientId: $serverClientId")
            val result = credentialManager.getCredential(
                request = request,
                context = activityContext
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                Log.d(TAG, "Google ID Token successfully obtained, signing in to Firebase...")

                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val user = authResult.user
                if (user != null) {
                    Log.d(TAG, "Firebase Google Login Successful: UID=${user.uid}, Email=${user.email}")
                    AuthResult.Success(user)
                } else {
                    AuthResult.Error("Google लॉगिन सफल नहीं हो सका। कृपया दोबारा प्रयास करें।")
                }
            } else {
                Log.w(TAG, "Unexpected credential type returned: ${credential.type}")
                AuthResult.Error("अमान्य क्रेडेंशियल प्राप्त हुआ।")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "User cancelled Google login flow.")
            AuthResult.Cancelled
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Credential Manager error: ${e.message}", e)
            val friendlyMsg = mapAuthError(e)
            AuthResult.Error(friendlyMsg, e)
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-in failed: ${e.message}", e)
            val friendlyMsg = mapAuthError(e)
            AuthResult.Error(friendlyMsg, e)
        }
    }

    /**
     * Real Email & Password Login
     */
    suspend fun signInWithEmail(email: String, pass: String): AuthResult {
        if (email.isBlank() || pass.isBlank()) {
            return AuthResult.Error("कृपया ईमेल और पासवर्ड दोनों दर्ज करें।")
        }
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), pass).await()
            val user = result.user
            if (user != null) {
                Log.d(TAG, "Firebase Email Login Successful: UID=${user.uid}")
                AuthResult.Success(user)
            } else {
                AuthResult.Error("लॉगिन असफल रहा।")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Email login exception: ${e.message}", e)
            AuthResult.Error(mapAuthError(e), e)
        }
    }

    /**
     * Real Email & Password Account Creation
     */
    suspend fun createAccountWithEmail(name: String, email: String, pass: String): AuthResult {
        if (email.isBlank() || pass.isBlank()) {
            return AuthResult.Error("कृपया ईमेल और पासवर्ड दर्ज करें।")
        }
        if (pass.length < 6) {
            return AuthResult.Error("पासवर्ड कम से कम 6 अक्षरों का होना चाहिए।")
        }
        return try {
            val result = auth.createUserWithEmailAndPassword(email.trim(), pass).await()
            val user = result.user
            if (user != null) {
                Log.d(TAG, "Firebase Account Created Successfully: UID=${user.uid}")
                AuthResult.Success(user)
            } else {
                AuthResult.Error("खाता नहीं बनाया जा सका।")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Create email account exception: ${e.message}", e)
            AuthResult.Error(mapAuthError(e), e)
        }
    }

    /**
     * Password Reset Email
     */
    suspend fun sendPasswordReset(email: String): Pair<Boolean, String> {
        if (email.isBlank()) {
            return Pair(false, "कृपया अपना ईमेल पता दर्ज करें।")
        }
        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            Pair(true, "पासवर्ड रीसेट लिंक आपके ईमेल पर भेज दिया गया है।")
        } catch (e: Exception) {
            Log.e(TAG, "Password reset error: ${e.message}", e)
            Pair(false, mapAuthError(e))
        }
    }

    /**
     * Real Logout
     */
    fun signOut() {
        try {
            auth.signOut()
            Log.d(TAG, "FirebaseAuth.signOut() executed successfully.")
        } catch (e: Exception) {
            Log.w(TAG, "Sign out warning: ${e.message}")
        }
    }

    private fun getWebClientIdFromResources(context: Context): String? {
        return try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) context.getString(resId) else null
        } catch (e: Exception) {
            null
        }
    }

    private fun mapAuthError(e: Exception): String {
        return when (e) {
            is FirebaseAuthInvalidUserException -> "इस ईमेल से कोई खाता नहीं मिला। कृपया नया खाता बनाएँ।"
            is FirebaseAuthInvalidCredentialsException -> "ईमेल या पासवर्ड सही नहीं है।"
            is FirebaseAuthUserCollisionException -> "इस ईमेल से पहले से अकाउंट बना हुआ है। कृपया लॉगिन करें।"
            is java.net.UnknownHostException, is java.io.IOException -> "इंटरनेट कनेक्शन जांचें और दोबारा प्रयास करें।"
            else -> {
                val msg = e.message ?: ""
                when {
                    msg.contains("network", ignoreCase = true) -> "इंटरनेट कनेक्शन जांचें और दोबारा प्रयास करें।"
                    msg.contains("password", ignoreCase = true) -> "पासवर्ड कम से कम 6 अक्षरों का होना चाहिए।"
                    msg.contains("email", ignoreCase = true) -> "कृपया सही ईमेल पता दर्ज करें।"
                    msg.contains("cancelled", ignoreCase = true) -> "Google Login रद्द किया गया।"
                    msg.contains("16:") || msg.contains("CANNOT_BIND_TO_SERVICE") -> "Google Play Services उपलब्ध नहीं है या रद्द की गई।"
                    else -> "लॉगिन में समस्या आई: ${e.localizedMessage ?: "कृपया दोबारा प्रयास करें।"}"
                }
            }
        }
    }
}
