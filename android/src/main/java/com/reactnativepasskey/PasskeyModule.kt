package com.reactnativepasskey

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.exceptions.*
import androidx.credentials.exceptions.publickeycredential.CreatePublicKeyCredentialDomException
import androidx.credentials.exceptions.publickeycredential.GetPublicKeyCredentialDomException
import expo.modules.kotlin.Promise
import expo.modules.kotlin.exception.Exceptions
import expo.modules.kotlin.functions.Coroutine
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope

class PasskeyModule : Module() {
  private val mainScope = CoroutineScope(Dispatchers.Default)

  override fun definition() = ModuleDefinition {
    Name("Passkey")

    AsyncFunction("register") { requestJson: String, promise: Promise ->
      val credentialManager = CredentialManager.create(appContext.reactContext.applicationContext)
      val createPublicKeyCredentialRequest = CreatePublicKeyCredentialRequest(requestJson)

      mainScope.launch {
        try {
          val result = appContext.activityProvider?.currentActivity?.let {
            credentialManager.createCredential(it, createPublicKeyCredentialRequest)
          }

          val response = result?.data?.getString("androidx.credentials.BUNDLE_KEY_REGISTRATION_RESPONSE_JSON")
          promise.resolve(response)
        } catch (e: CreateCredentialException) {
          promise.reject("Passkey", handleRegistrationException(e))
        }
      }
    }

    AsyncFunction("authenticate") { requestJson: String, promise: Promise ->
      val credentialManager = CredentialManager.create(appContext.reactContext.applicationContext)
      val getCredentialRequest = GetCredentialRequest(listOf(GetPublicKeyCredentialOption(requestJson)))

      mainScope.launch {
        try {
          val result = appContext.activityProvider?.currentActivity?.let {
            credentialManager.getCredential(it, getCredentialRequest)
          }

          val response = result?.credential?.data?.getString("androidx.credentials.BUNDLE_KEY_AUTHENTICATION_RESPONSE_JSON")
          promise.resolve(response)
        } catch (e: GetCredentialException) {
          promise.reject("Passkey", handleAuthenticationException(e))
        }
      }
    }
  }

  private fun handleRegistrationException(e: CreateCredentialException): String {
    e.printStackTrace()
    return when (e) {
      is CreatePublicKeyCredentialDomException -> e.errorMessage.toString()
      is CreateCredentialCancellationException -> "UserCancelled"
      is CreateCredentialInterruptedException -> "Interrupted"
      is CreateCredentialProviderConfigurationException -> "NotConfigured"
      is CreateCredentialUnknownException -> "UnknownError"
      is CreateCredentialUnsupportedException -> "NotSupported"
      else -> e.errorMessage.toString()
    }
  }

  private fun handleAuthenticationException(e: GetCredentialException): String {
    e.printStackTrace()
    return when (e) {
      is GetPublicKeyCredentialDomException -> e.errorMessage.toString()
      is GetCredentialCancellationException -> "UserCancelled"
      is GetCredentialInterruptedException -> "Interrupted"
      is GetCredentialProviderConfigurationException -> "NotConfigured"
      is GetCredentialUnknownException -> "UnknownError"
      is GetCredentialUnsupportedException -> "NotSupported"
      is NoCredentialException -> "NoCredentials"
      else -> e.errorMessage.toString()
    }
  }
}
