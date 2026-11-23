package com.example.data.remote

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed interface AuthResult {
    data class Success(val user: FirebaseUser) : AuthResult
    data object Cancelled : AuthResult
    data class Error(val message: String) : AuthResult
}

data class AuthenticatedUser(
    val uid: String,
    val displayName: String,
    val email: String,
    val photoUrl: String
)

class AuthRepository(private val context: Context) {
    private val credentialManager: CredentialManager by lazy { CredentialManager.create(context) }

    private fun isFirebaseConfigured(): Boolean = FirebaseApp.getApps(context).isNotEmpty()

    private fun getAuthOrNull(): FirebaseAuth? {
        if (!isFirebaseConfigured()) return null
        return runCatching { FirebaseAuth.getInstance() }.getOrNull()
    }

    private fun resolveWebClientId(): String? {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (resId == 0) return null
        return context.getString(resId).takeIf { it.isNotBlank() }
    }

    val authStateFlow: Flow<AuthenticatedUser?> = callbackFlow {
        val firebaseAuth = getAuthOrNull()
        if (firebaseAuth == null) {
            trySend(null)
            awaitClose {}
            return@callbackFlow
        }
        val listener = FirebaseAuth.AuthStateListener { activeAuth ->
            trySend(activeAuth.currentUser?.toAuthenticatedUser())
        }
        firebaseAuth.addAuthStateListener(listener)
        trySend(firebaseAuth.currentUser?.toAuthenticatedUser())
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    fun currentUser(): AuthenticatedUser? = getAuthOrNull()?.currentUser?.toAuthenticatedUser()

    suspend fun attemptSilentSignIn(): AuthenticatedUser? {
        val firebaseAuth = getAuthOrNull() ?: return null
        firebaseAuth.currentUser?.let { return it.toAuthenticatedUser() }
        val webClientId = resolveWebClientId() ?: return null
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            val response = credentialManager.getCredential(context, request)
            val res = handleGoogleCredentialResponse(firebaseAuth, response)
            (res as? AuthResult.Success)?.user?.toAuthenticatedUser()
        } catch (e: CancellationException) {
            throw e
        } catch (_: NoCredentialException) {
            null
        } catch (e: Exception) {
            Log.d("AuthRepository", "Silent sign-in unavailable: ${e.message}")
            null
        }
    }

    suspend fun signInWithGoogle(activityContext: Context): AuthResult {
        val firebaseAuth = getAuthOrNull()
            ?: return AuthResult.Error("Cloud sign-in is offline until Firebase google-services.json is configured.")
        val webClientId = resolveWebClientId()
            ?: return AuthResult.Error("Google Sign-In client ID is unavailable in this preview build.")
        return try {
            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(webClientId)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()
            val cm = CredentialManager.create(activityContext)
            val response = cm.getCredential(activityContext, request)
            handleGoogleCredentialResponse(firebaseAuth, response)
        } catch (e: CancellationException) {
            throw e
        } catch (_: GetCredentialCancellationException) {
            AuthResult.Cancelled
        } catch (e: GetCredentialException) {
            AuthResult.Error(
                e.localizedMessage ?: "Google Sign-In could not find an account on this device."
            )
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Google Sign-In failed.")
        }
    }

    private suspend fun handleGoogleCredentialResponse(
        firebaseAuth: FirebaseAuth,
        response: GetCredentialResponse
    ): AuthResult {
        val credential = response.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleCred = GoogleIdTokenCredential.createFrom(credential.data)
            val authCred = GoogleAuthProvider.getCredential(googleCred.idToken, null)
            val result = firebaseAuth.signInWithCredential(authCred).await()
            val user = result.user
            return if (user != null) {
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Firebase sign-in returned no user.")
            }
        }
        return AuthResult.Error("Unsupported credential type returned.")
    }

    suspend fun signOut() {
        withContext(NonCancellable) {
            runCatching {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            }
            getAuthOrNull()?.signOut()
        }
    }

    private fun FirebaseUser.toAuthenticatedUser(): AuthenticatedUser = AuthenticatedUser(
        uid = uid,
        displayName = displayName?.takeIf { it.isNotBlank() } ?: "Dawn Collector",
        email = email.orEmpty(),
        photoUrl = photoUrl?.toString().orEmpty()
    )
}
