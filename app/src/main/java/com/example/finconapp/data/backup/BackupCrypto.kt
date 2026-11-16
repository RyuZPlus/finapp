
package com.example.finconapp.data.backup

import android.util.Base64
import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object BackupCrypto {

    private const val FORMAT_VERSION = 1
    private const val KDF_NAME = "PBKDF2-HMAC-SHA256"
    private const val CIPHER_NAME = "AES-256-GCM"

    private const val ITERATIONS = 600_000
    private const val KEY_SIZE = 32
    private const val SALT_SIZE = 16
    private const val IV_SIZE = 12
    private const val TAG_SIZE = 128

    private const val MIN_ITERATIONS = 100_000
    private const val MAX_ITERATIONS = 2_000_000

    private const val MIN_PASSWORD_LENGTH = 8
    private const val NEW_PASSWORD_LENGTH = 12
    private const val MAX_PASSWORD_LENGTH = 1024

    // Límite del archivo cifrado: 64 MiB.
    private const val MAX_ENCRYPTED_SIZE = 64 * 1024 * 1024

    // Límite del contenido original: 48 MiB.
    private const val MAX_PLAIN_SIZE = 48 * 1024 * 1024

    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    private val secureRandom = SecureRandom()

    /**
     * Cifra el contenido utilizando una contraseña independiente
     * del dispositivo y de la cuenta de Google.
     */
    fun encrypt(data: ByteArray, password: CharArray): ByteArray {

        require(data.isNotEmpty()) {
            "No hay información para cifrar."
        }

        require(data.size <= MAX_PLAIN_SIZE) {
            "El respaldo supera el tamaño máximo permitido."
        }

        validatePassword(password, NEW_PASSWORD_LENGTH)

        val salt = ByteArray(SALT_SIZE).also {
            secureRandom.nextBytes(it)
        }

        val iv = ByteArray(IV_SIZE).also {
            secureRandom.nextBytes(it)
        }

        val passwordBytes = passwordToBytes(password)
        var derivedKey: ByteArray? = null
        var encryptedData: ByteArray? = null

        try {
            derivedKey = deriveKey(
                password = passwordBytes,
                salt = salt,
                iterations = ITERATIONS
            )

            val cipher = Cipher.getInstance(TRANSFORMATION)

            cipher.init(
                Cipher.ENCRYPT_MODE,
                SecretKeySpec(derivedKey, "AES"),
                GCMParameterSpec(TAG_SIZE, iv)
            )

            val aad = createAad(
                version = FORMAT_VERSION,
                kdf = KDF_NAME,
                iterations = ITERATIONS,
                cipherName = CIPHER_NAME,
                salt = salt
            )

            cipher.updateAAD(aad)

            encryptedData = cipher.doFinal(data)

            val envelope = JSONObject().apply {
                put("formatVersion", FORMAT_VERSION)
                put("kdf", KDF_NAME)
                put("iterations", ITERATIONS)
                put("cipher", CIPHER_NAME)
                put("salt", encodeBase64(salt))
                put("iv", encodeBase64(iv))
                put("data", encodeBase64(encryptedData))
            }

            val result = envelope.toString()
                .toByteArray(StandardCharsets.UTF_8)

            require(result.size <= MAX_ENCRYPTED_SIZE) {
                "El respaldo cifrado supera el tamaño máximo permitido."
            }

            return result

        } finally {
            passwordBytes.fill(0)
            derivedKey?.fill(0)
            encryptedData?.fill(0)
            salt.fill(0)
            iv.fill(0)
        }
    }

    /**
     * Descifra un respaldo utilizando la contraseña proporcionada.
     */
    fun decrypt(
        encryptedBackup: ByteArray,
        password: CharArray
    ): ByteArray {

        require(encryptedBackup.isNotEmpty()) {
            "El archivo de respaldo está vacío."
        }

        require(encryptedBackup.size <= MAX_ENCRYPTED_SIZE) {
            "El archivo de respaldo supera el tamaño permitido."
        }

        // Se conserva el mínimo anterior para poder abrir
        // respaldos creados con contraseñas de 8 caracteres.
        validatePassword(password, MIN_PASSWORD_LENGTH)

        val envelope = try {
            JSONObject(
                String(encryptedBackup, StandardCharsets.UTF_8)
            )
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "El archivo de respaldo no tiene un formato válido.",
                e
            )
        }

        val version = envelope.optInt("formatVersion", -1)

        require(version == FORMAT_VERSION) {
            "La versión del respaldo no es compatible."
        }

        val kdf = envelope.optString("kdf", "")
        val cipherName = envelope.optString("cipher", "")
        val iterations = envelope.optInt("iterations", -1)

        require(kdf == KDF_NAME && cipherName == CIPHER_NAME) {
            "El método de cifrado del respaldo no es compatible."
        }

        require(iterations in MIN_ITERATIONS..MAX_ITERATIONS) {
            "Los parámetros de cifrado del respaldo no son válidos."
        }

        val salt = decodeBase64(
            getRequiredString(envelope, "salt")
        )

        val iv = decodeBase64(
            getRequiredString(envelope, "iv")
        )

        val encryptedData = decodeBase64(
            getRequiredString(envelope, "data")
        )

        require(salt.size == SALT_SIZE) {
            "El salt del respaldo no es válido."
        }

        require(iv.size == IV_SIZE) {
            "El IV del respaldo no es válido."
        }

        require(encryptedData.size > TAG_SIZE / 8) {
            "El contenido cifrado está incompleto."
        }

        val passwordBytes = passwordToBytes(password)
        var derivedKey: ByteArray? = null

        try {
            derivedKey = deriveKey(
                password = passwordBytes,
                salt = salt,
                iterations = iterations
            )

            val cipher = Cipher.getInstance(TRANSFORMATION)

            cipher.init(
                Cipher.DECRYPT_MODE,
                SecretKeySpec(derivedKey, "AES"),
                GCMParameterSpec(TAG_SIZE, iv)
            )

            val aad = createAad(
                version = version,
                kdf = kdf,
                iterations = iterations,
                cipherName = cipherName,
                salt = salt
            )

            cipher.updateAAD(aad)

            val decryptedData = try {
                cipher.doFinal(encryptedData)
            } catch (e: AEADBadTagException) {
                throw IllegalArgumentException(
                    "Contraseña incorrecta o respaldo dañado.",
                    e
                )
            }

            require(decryptedData.size <= MAX_PLAIN_SIZE) {
                decryptedData.fill(0)
                "El contenido del respaldo supera el tamaño permitido."
            }

            return decryptedData

        } finally {
            passwordBytes.fill(0)
            derivedKey?.fill(0)
            salt.fill(0)
            iv.fill(0)
            encryptedData.fill(0)
        }
    }

    /**
     * Convierte CharArray a UTF-8 sin crear un String temporal.
     */
    private fun passwordToBytes(password: CharArray): ByteArray {

        val encoded = StandardCharsets.UTF_8.newEncoder()
            .encode(CharBuffer.wrap(password))

        try {
            val result = ByteArray(encoded.remaining())
            encoded.get(result)
            return result
        } finally {
            if (encoded.hasArray()) {
                encoded.array().fill(0)
            }
        }
    }

    /**
     * PBKDF2-HMAC-SHA256.
     *
     * Se conserva esta implementación para mantener
     * compatibilidad con los respaldos de formato 1.
     */
    private fun deriveKey(
        password: ByteArray,
        salt: ByteArray,
        iterations: Int
    ): ByteArray {

        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(password, "HmacSHA256"))

        // SHA-256 produce 32 bytes. Para KEY_SIZE = 32
        // solamente se necesita un bloque PBKDF2.
        val block = ByteBuffer.allocate(4)
            .putInt(1)
            .array()

        val saltBlock = salt + block

        var u = mac.doFinal(saltBlock)
        val result = u.copyOf()

        try {
            for (i in 2..iterations) {

                val nextU = mac.doFinal(u)

                u.fill(0)
                u = nextU

                for (j in result.indices) {
                    result[j] = (
                            result[j].toInt() xor u[j].toInt()
                            ).toByte()
                }
            }

            return result.copyOf(KEY_SIZE)

        } finally {
            u.fill(0)
            result.fill(0)
            block.fill(0)
            saltBlock.fill(0)
        }
    }

    /**
     * Datos autenticados adicionales de AES-GCM.
     */
    private fun createAad(
        version: Int,
        kdf: String,
        iterations: Int,
        cipherName: String,
        salt: ByteArray
    ): ByteArray {

        val saltText = encodeBase64(salt)

        return "Finconapp|$version|$kdf|$iterations|$cipherName|$saltText"
            .toByteArray(StandardCharsets.UTF_8)
    }

    private fun validatePassword(
        password: CharArray,
        minimumLength: Int
    ) {
        require(password.size in minimumLength..MAX_PASSWORD_LENGTH) {
            "La contraseña debe tener entre $minimumLength y $MAX_PASSWORD_LENGTH caracteres."
        }
    }

    private fun getRequiredString(
        json: JSONObject,
        key: String
    ): String {

        require(json.has(key) && !json.isNull(key)) {
            "Falta un dato necesario en el respaldo."
        }

        val value = json.get(key)

        require(value is String && value.isNotEmpty()) {
            "El campo del respaldo no tiene un formato válido."
        }

        return value
    }

    private fun encodeBase64(data: ByteArray): String {
        return Base64.encodeToString(data, Base64.NO_WRAP)
    }

    private fun decodeBase64(data: String): ByteArray {
        return try {
            Base64.decode(data, Base64.NO_WRAP)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException(
                "El respaldo contiene datos codificados no válidos.",
                e
            )
        }
    }
}