package com.indie.mindfullauncher.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.indie.mindfullauncher.model.IconPackInfo
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.util.concurrent.ConcurrentHashMap

object IconPackHelper {
    private val mapCache = ConcurrentHashMap<String, Map<String, String>>()

    fun listInstalledIconPacks(context: Context): List<IconPackInfo> {
        val pm = context.packageManager
        val intents = listOf(
            Intent("org.adw.launcher.THEMES"),
            Intent("com.novalauncher.THEME")
        )
        return intents
            .flatMap { pm.queryIntentActivities(it, PackageManager.MATCH_ALL) }
            .mapNotNull { info ->
                val pkg = info.activityInfo?.packageName ?: return@mapNotNull null
                val label = info.loadLabel(pm)?.toString()?.trim().orEmpty()
                if (label.isEmpty()) null else IconPackInfo(label = label, packageName = pkg)
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    fun loadAppIconDrawable(
        context: Context,
        targetPackage: String,
        iconPackPackage: String?
    ): Drawable? {
        val pm = context.packageManager
        if (!iconPackPackage.isNullOrBlank()) {
            val packDrawable = runCatching {
                val packRes = pm.getResourcesForApplication(iconPackPackage)
                val mapping = getOrLoadMapping(pm, iconPackPackage)
                val drawableName = mapping[targetPackage]
                if (drawableName != null) {
                    val resId = packRes.getIdentifier(drawableName, "drawable", iconPackPackage)
                    if (resId != 0) packRes.getDrawable(resId, null) else null
                } else {
                    null
                }
            }.getOrNull()
            if (packDrawable != null) return packDrawable
        }
        return runCatching { pm.getApplicationIcon(targetPackage) }.getOrNull()
    }

    private fun getOrLoadMapping(pm: PackageManager, iconPackPackage: String): Map<String, String> {
        mapCache[iconPackPackage]?.let { return it }
        val result = HashMap<String, String>()
        runCatching {
            val res = pm.getResourcesForApplication(iconPackPackage)
            val xmlResId = res.getIdentifier("appfilter", "xml", iconPackPackage)
            val parser: XmlPullParser? = if (xmlResId != 0) {
                res.getXml(xmlResId)
            } else {
                val factory = XmlPullParserFactory.newInstance()
                val stream = res.assets.open("appfilter.xml")
                factory.newPullParser().apply { setInput(stream, "UTF-8") }
            }
            if (parser != null) {
                var eventType = parser.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG && parser.name == "item") {
                        val component = parser.getAttributeValue(null, "component")
                        val drawable = parser.getAttributeValue(null, "drawable")
                        if (!component.isNullOrEmpty() && !drawable.isNullOrEmpty()) {
                            // ComponentInfo{pkg/cls}
                            val start = component.indexOf('{')
                            val slash = component.indexOf('/')
                            if (start >= 0 && slash > start + 1) {
                                val pkg = component.substring(start + 1, slash)
                                result.putIfAbsent(pkg, drawable)
                            }
                        }
                    }
                    eventType = parser.next()
                }
            }
        }
        mapCache[iconPackPackage] = result
        return result
    }
}
