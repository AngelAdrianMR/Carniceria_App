package com.example.carniceria_app

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.carniceria.shared.shared.models.utils.obtenerPerfilUsuarioActual
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class NotificationService : FirebaseMessagingService() {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // 🔹 Comprobamos primero si el usuario actual tiene notificaciones activadas
        val context = this

        val notificacionesActivas = runBlocking {
            NotificationPreferences.getNotificationsEnabled(context).first()
        }

        // 🔹 Obtenemos el rol del usuario actual (si es admin, no filtramos)
        val esAdmin = runBlocking {
            try {
                val perfil = obtenerPerfilUsuarioActual()
                perfil?.rol == "Administrador"
            } catch (e: Exception) {
                false
            }
        }

        // 🔕 Si no es admin y el usuario desactivó notificaciones, no mostramos nada
        if (!esAdmin && !notificacionesActivas) {
            println("🔕 Notificaciones desactivadas por el usuario (cliente)")
            return
        }

        val title = remoteMessage.notification?.title ?: "Nuevo pedido"
        val body = remoteMessage.notification?.body ?: "Tienes una actualización en tu pedido"

        // Crear canal (solo si no existe)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "pedidos_channel",
                "Notificaciones de pedidos",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        // Mostrar notificación
        val notification = NotificationCompat.Builder(this, "pedidos_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(this)
            .notify(System.currentTimeMillis().toInt(), notification)
    }
}

// 💾 DataStore para preferencias de usuario
val Context.dataStore by preferencesDataStore(name = "user_preferences")

object NotificationPreferences {
    private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")

    suspend fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    fun getNotificationsEnabled(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            prefs[NOTIFICATIONS_ENABLED] ?: true // Por defecto: activadas
        }
    }
}
