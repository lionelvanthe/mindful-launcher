package com.indie.mindfullauncher.data

import android.app.Notification
import android.app.RemoteInput
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.indie.mindfullauncher.model.MediaPlaybackState
import com.indie.mindfullauncher.model.NotificationPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

object NotificationRepository {
    private val _previews = MutableStateFlow<Map<String, NotificationPreview>>(emptyMap())
    val previews: StateFlow<Map<String, NotificationPreview>> = _previews.asStateFlow()

    private val _mediaState = MutableStateFlow<MediaPlaybackState?>(null)
    val mediaState: StateFlow<MediaPlaybackState?> = _mediaState.asStateFlow()

    private var listenerRef: WeakReference<NiagaraNotificationListener>? = null
    private var activeController: WeakReference<MediaController>? = null

    fun attachListener(listener: NiagaraNotificationListener) {
        listenerRef = WeakReference(listener)
    }

    fun detachListener() {
        listenerRef = null
    }

    fun updateFromStatusBar(context: Context, notifications: Array<StatusBarNotification>?) {
        if (notifications == null) {
            _previews.value = emptyMap()
        } else {
            val map = linkedMapOf<String, NotificationPreview>()
            notifications
                .sortedByDescending { it.postTime }
                .forEach { sbn ->
                    val pkg = sbn.packageName ?: return@forEach
                    if (map.containsKey(pkg)) return@forEach
                    if (sbn.isOngoing) return@forEach
                    val notification = sbn.notification ?: return@forEach
                    val extras = notification.extras ?: return@forEach
                    val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim().orEmpty()
                    val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim().orEmpty()
                    val summary = when {
                        title.isNotEmpty() && text.isNotEmpty() -> "$title: $text"
                        title.isNotEmpty() -> title
                        text.isNotEmpty() -> text
                        else -> ""
                    }
                    if (summary.isNotEmpty()) {
                        var replyAction: Notification.Action? = null
                        var replyKey: String? = null
                        notification.actions?.forEach { action ->
                            val remote = action.remoteInputs?.firstOrNull()
                            if (remote != null && replyAction == null) {
                                replyAction = action
                                replyKey = remote.resultKey
                            }
                        }
                        map[pkg] = NotificationPreview(
                            key = sbn.key,
                            packageName = pkg,
                            title = title,
                            text = text,
                            summary = summary,
                            postTime = sbn.postTime,
                            contentIntent = notification.contentIntent,
                            replyAction = replyAction,
                            replyResultKey = replyKey
                        )
                    }
                }
            _previews.value = map
        }
        refreshMediaSession(context)
    }

    fun refreshMediaSession(context: Context) {
        runCatching {
            val msm = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
            val cn = ComponentName(context, NiagaraNotificationListener::class.java)
            val controllers = msm.getActiveSessions(cn)
            val chosen = controllers.firstOrNull {
                val state = it.playbackState?.state
                state == PlaybackState.STATE_PLAYING || state == PlaybackState.STATE_PAUSED
            } ?: controllers.firstOrNull()

            if (chosen == null) {
                activeController = null
                _mediaState.value = null
                return
            }
            activeController = WeakReference(chosen)
            val meta = chosen.metadata
            val title = meta?.getString(MediaMetadata.METADATA_KEY_TITLE)?.trim().orEmpty()
            val artist = meta?.getString(MediaMetadata.METADATA_KEY_ARTIST)?.trim().orEmpty()
            val isPlaying = chosen.playbackState?.state == PlaybackState.STATE_PLAYING
            if (title.isEmpty() && artist.isEmpty()) {
                _mediaState.value = null
            } else {
                _mediaState.value = MediaPlaybackState(
                    packageName = chosen.packageName,
                    title = title.ifEmpty { "Đang phát" },
                    artist = artist,
                    isPlaying = isPlaying
                )
            }
        }
    }

    fun toggleMediaPlayPause(context: Context) {
        val controller = activeController?.get() ?: return
        val controls = controller.transportControls
        if (controller.playbackState?.state == PlaybackState.STATE_PLAYING) {
            controls.pause()
        } else {
            controls.play()
        }
        refreshMediaSession(context)
    }

    fun skipMediaNext(context: Context) {
        activeController?.get()?.transportControls?.skipToNext()
        refreshMediaSession(context)
    }

    fun skipMediaPrevious(context: Context) {
        activeController?.get()?.transportControls?.skipToPrevious()
        refreshMediaSession(context)
    }

    fun dismissNotification(key: String) {
        runCatching { listenerRef?.get()?.cancelNotification(key) }
    }

    fun sendQuickReply(context: Context, preview: NotificationPreview, replyText: String): Boolean {
        val action = preview.replyAction ?: return false
        val resultKey = preview.replyResultKey ?: return false
        val clean = replyText.trim()
        if (clean.isEmpty()) return false
        return runCatching {
            val intent = Intent()
            val bundle = Bundle().apply { putCharSequence(resultKey, clean) }
            RemoteInput.addResultsToIntent(action.remoteInputs, intent, bundle)
            action.actionIntent.send(context, 0, intent)
            true
        }.getOrDefault(false)
    }
}

class NiagaraNotificationListener : NotificationListenerService() {
    override fun onListenerConnected() {
        super.onListenerConnected()
        NotificationRepository.attachListener(this)
        runCatching { NotificationRepository.updateFromStatusBar(this, activeNotifications) }
    }

    override fun onListenerDisconnected() {
        NotificationRepository.detachListener()
        super.onListenerDisconnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        runCatching { NotificationRepository.updateFromStatusBar(this, activeNotifications) }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        runCatching { NotificationRepository.updateFromStatusBar(this, activeNotifications) }
    }
}
