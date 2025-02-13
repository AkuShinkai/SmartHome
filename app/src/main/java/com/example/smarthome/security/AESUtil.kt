package com.example.smarthome.security

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AESUtil {
    private const val SECRET_KEY = "1234567890123456"  // 16 karakter untuk AES-128 (HARUS 16, 24, atau 32 karakter)

    fun encrypt(password: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKey = generateKey()

        // IV acak untuk setiap enkripsi
        val iv = ByteArray(16).apply { SecureRandom().nextBytes(this) }
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))

        val encrypted = cipher.doFinal(password.toByteArray(Charsets.UTF_8))

        // Gabungkan IV + data terenkripsi
        return Base64.encodeToString(iv + encrypted, Base64.DEFAULT)
    }

    fun decrypt(encrypted: String): String {
        val decoded = Base64.decode(encrypted, Base64.DEFAULT)

        // Ambil IV dari data
        val iv = decoded.copyOfRange(0, 16)
        val encryptedBytes = decoded.copyOfRange(16, decoded.size)

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKey = generateKey()
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))

        return String(cipher.doFinal(encryptedBytes), Charsets.UTF_8)
    }

    private fun generateKey(): SecretKey {
        // Menggunakan SECRET_KEY langsung untuk AES tanpa PBKDF2
        return SecretKeySpec(SECRET_KEY.toByteArray(Charsets.UTF_8), "AES")
    }
}
