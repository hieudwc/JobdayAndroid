package vn.jobday.core.datastore.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenCipher @Inject constructor() {

    fun encrypt(
        plainText: String,
    ): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)

        cipher.init(
            Cipher.ENCRYPT_MODE,
            getOrCreateKey(),
        )

        val initializationVector = cipher.iv
        val encryptedBytes = cipher.doFinal(
            plainText.toByteArray(Charsets.UTF_8),
        )

        val encodedIv = Base64.encodeToString(
            initializationVector,
            Base64.NO_WRAP,
        )

        val encodedValue = Base64.encodeToString(
            encryptedBytes,
            Base64.NO_WRAP,
        )

        return "$encodedIv$SEPARATOR$encodedValue"
    }

    fun decrypt(
        encryptedValue: String,
    ): String {
        val parts = encryptedValue.split(SEPARATOR, limit = 2)

        require(parts.size == 2) {
            "Encrypted token has an invalid format"
        }

        val initializationVector = Base64.decode(
            parts[0],
            Base64.NO_WRAP,
        )

        val encryptedBytes = Base64.decode(
            parts[1],
            Base64.NO_WRAP,
        )

        val cipher = Cipher.getInstance(TRANSFORMATION)

        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateKey(),
            GCMParameterSpec(
                GCM_TAG_LENGTH_BITS,
                initializationVector,
            ),
        )

        return cipher.doFinal(encryptedBytes)
            .toString(Charsets.UTF_8)
    }

    @Synchronized
    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(
            ANDROID_KEYSTORE,
        ).apply {
            load(null)
        }

        val existingKey = keyStore.getKey(
            KEY_ALIAS,
            null,
        ) as? SecretKey

        if (existingKey != null) {
            return existingKey
        }

        return KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE,
        ).apply {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or
                            KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(
                        KeyProperties.BLOCK_MODE_GCM,
                    )
                    .setEncryptionPaddings(
                        KeyProperties.ENCRYPTION_PADDING_NONE,
                    )
                    .setRandomizedEncryptionRequired(true)
                    .build(),
            )
        }.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "jobday_session_token_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_LENGTH_BITS = 128
        const val SEPARATOR = ":"
    }
}