package com.abhibot.sevenone.data

import android.content.Context
import android.util.Base64
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class DhanCredentialsStore(
    private val context: Context
) {

    private val prefs =
        context.getSharedPreferences(
            "dhan_credentials",
            Context.MODE_PRIVATE
        )

    private val keyAlias = "abhibot_dhan_credentials_key"

    private fun getSecretKey(): SecretKey {

        val keyStore =
            java.security.KeyStore
                .getInstance("AndroidKeyStore")
                .apply {
                    load(null)
                }

        val existing =
            keyStore.getKey(keyAlias, null) as? SecretKey

        if (existing != null) {
            return existing
        }

        val generator =
            KeyGenerator.getInstance(
                "AES",
                "AndroidKeyStore"
            )

        generator.init(
            android.security.keystore.KeyGenParameterSpec.Builder(
                keyAlias,
                android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or
                    android.security.keystore.KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(
                    android.security.keystore.KeyProperties.BLOCK_MODE_GCM
                )
                .setEncryptionPaddings(
                    android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE
                )
                .build()
        )

        return generator.generateKey()
    }

    private fun encrypt(value: String): String {

        val cipher =
            Cipher.getInstance("AES/GCM/NoPadding")

        cipher.init(
            Cipher.ENCRYPT_MODE,
            getSecretKey()
        )

        val encrypted =
            cipher.doFinal(
                value.toByteArray(StandardCharsets.UTF_8)
            )

        val iv =
            Base64.encodeToString(
                cipher.iv,
                Base64.NO_WRAP
            )

        val data =
            Base64.encodeToString(
                encrypted,
                Base64.NO_WRAP
            )

        return "$iv:$data"
    }

    private fun decrypt(value: String): String? {

        return try {

            val parts = value.split(":")

            if (parts.size != 2) {
                return null
            }

            val iv =
                Base64.decode(
                    parts[0],
                    Base64.NO_WRAP
                )

            val encrypted =
                Base64.decode(
                    parts[1],
                    Base64.NO_WRAP
                )

            val cipher =
                Cipher.getInstance("AES/GCM/NoPadding")

            cipher.init(
                Cipher.DECRYPT_MODE,
                getSecretKey(),
                GCMParameterSpec(128, iv)
            )

            String(
                cipher.doFinal(encrypted),
                StandardCharsets.UTF_8
            )

        } catch (_: Exception) {
            null
        }
    }

    fun saveCredentials(
        clientId: String,
        accessToken: String
    ) {

        prefs.edit()
            .putString(
                "client_id",
                encrypt(clientId.trim())
            )
            .putString(
                "access_token",
                encrypt(accessToken.trim())
            )
            .apply()
    }

    fun getClientId(): String? {

        val encrypted =
            prefs.getString(
                "client_id",
                null
            ) ?: return null

        return decrypt(encrypted)
    }

    fun getAccessToken(): String? {

        val encrypted =
            prefs.getString(
                "access_token",
                null
            ) ?: return null

        return decrypt(encrypted)
    }

    fun isConfigured(): Boolean {

        return !getClientId().isNullOrBlank() &&
               !getAccessToken().isNullOrBlank()
    }

    fun clear() {

        prefs.edit()
            .clear()
            .apply()
    }
}
