package com.example.carniceria_app

import android.content.Context
import android.net.Uri
import android.util.Log
import com.carniceria.shared.shared.models.utils.SupabaseProvider
import io.github.jan.supabase.storage.storage
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.UUID



object EnvioUtils {

    // 📦 Lista de códigos postales donde se acepta reparto
    private val codigosPermitidos = listOf(
        // Roquetas de Mar
        "04740", "04742", "04743", "04749",
        // La Mojonera
        "04745",
        // Aguadulce
        "04720", "04721",
        // Vícar
        "04738", "04739"
    )

    /**
     * Verifica si un código postal pertenece a las zonas de reparto
     */
    fun esZonaValida(codigoPostal: String): Boolean {
        return codigosPermitidos.contains(codigoPostal)
    }

    /**
     * Extrae un código postal (5 dígitos) de una dirección de texto
     */
    fun extraerCodigoPostal(direccion: String): String? {
        val regex = Regex("\\b\\d{5}\\b")
        return regex.find(direccion)?.value
    }

    /**
     * Texto amigable de las zonas cubiertas
     */
    fun obtenerZonasTexto(): String {
        return "Reparto disponible en: Roquetas de Mar, La Mojonera, Aguadulce y Vícar."
    }
}


fun copiarUriABytes(context: Context, uri: Uri): ByteArray? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val buffer = ByteArrayOutputStream()
        val data = ByteArray(1024)
        var n: Int
        while (inputStream?.read(data).also { n = it ?: -1 } != -1) {
            buffer.write(data, 0, n)
        }
        buffer.toByteArray()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// ✅ Subir imagen de PROMOCIÓN con URL pública
suspend fun subirImagenPromocion(context: Context, uri: Uri, nombreArchivo: String): String? {
    return try {
        val bytes = copiarUriABytes(context, uri) ?: return null
        val bucket = "promocion"
        val storage = SupabaseProvider.client.storage.from(bucket)

        // 📤 Subir la imagen
        storage.upload(nombreArchivo, bytes) { upsert = true }

        // 🌐 Construir manualmente la URL pública
        val baseUrl = SupabaseProvider.client.supabaseUrl
        val publicUrl = "$baseUrl/storage/v1/object/public/$bucket/$nombreArchivo"

        println("✅ Imagen de promoción subida correctamente: $publicUrl")
        publicUrl
    } catch (e: Exception) {
        println("❌ Error al subir imagen de promoción: ${e.message}")
        null
    }
}

// ✅ Subir imagen de PRODUCTO con URL pública
suspend fun subirImagenProducto(context: Context, uri: Uri): String? {
    return try {
        val bucket = "producto"
        val storage = SupabaseProvider.client.storage.from(bucket)
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val bytes = inputStream.use { it.readBytes() }

        // 📄 Nombre único
        val nombreArchivo = "img_${UUID.randomUUID()}.jpg"

        // 📤 Subir la imagen (reemplaza si ya existe)
        storage.upload(nombreArchivo, bytes) {
            upsert = true
        }

        // 🌐 Construir URL pública completa (con https)
        val baseUrl = SupabaseProvider.client.supabaseUrl
        val fullBaseUrl = if (baseUrl.startsWith("http")) baseUrl else "https://$baseUrl"
        val publicUrl = "$fullBaseUrl/storage/v1/object/public/$bucket/$nombreArchivo"

        Log.i("SubirImagen", "✅ Imagen subida: $publicUrl")
        publicUrl
    } catch (e: Exception) {
        Log.e("SubirImagen", "❌ Error al subir imagen: ${e.message}", e)
        null
    }
}
