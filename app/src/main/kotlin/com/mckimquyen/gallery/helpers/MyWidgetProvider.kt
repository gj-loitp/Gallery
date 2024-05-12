package com.mckimquyen.gallery.helpers

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import org.fossify.commons.extensions.applyColorFilter
import org.fossify.commons.extensions.getFileSignature
import org.fossify.commons.extensions.setText
import org.fossify.commons.extensions.setVisibleIf
import org.fossify.commons.helpers.ensureBackgroundThread
import com.mckimquyen.gallery.R
import com.mckimquyen.gallery.activities.MediaActivity
import com.mckimquyen.gallery.extensions.config
import com.mckimquyen.gallery.extensions.directoryDB
import com.mckimquyen.gallery.extensions.getFolderNameFromPath
import com.mckimquyen.gallery.extensions.widgetsDB
import com.mckimquyen.gallery.models.Widget

class MyWidgetProvider : AppWidgetProvider() {
    private fun setupAppOpenIntent(context: Context, views: RemoteViews, id: Int, widget: Widget) {
        val intent = Intent(context, MediaActivity::class.java).apply {
            putExtra(DIRECTORY, widget.folderPath)
        }

        val pendingIntent = PendingIntent.getActivity(context, widget.widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(id, pendingIntent)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        ensureBackgroundThread {
            val config = context.config
            context.widgetsDB.getWidgets().filter { appWidgetIds.contains(it.widgetId) }.forEach {
                val views = RemoteViews(context.packageName, R.layout.widget).apply {
                    applyColorFilter(R.id.widgetBackground, config.widgetBgColor)
                    setVisibleIf(R.id.widgetFolderName, config.showWidgetFolderName)
                    setTextColor(R.id.widgetFolderName, config.widgetTextColor)
                    setText(R.id.widgetFolderName, context.getFolderNameFromPath(it.folderPath))
                }

                val path = context.directoryDB.getDirectoryThumbnail(it.folderPath) ?: return@forEach
                val options = RequestOptions()
                    .signature(path.getFileSignature())
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)

                if (context.config.cropThumbnails) {
                    options.centerCrop()
                } else {
                    options.fitCenter()
                }

                val density = context.resources.displayMetrics.density
                val appWidgetOptions = appWidgetManager.getAppWidgetOptions(appWidgetIds.first())
                val width = appWidgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
                val height = appWidgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)

                val widgetSize = (Math.max(width, height) * density).toInt()
                try {
                    val image = Glide.with(context)
                        .asBitmap()
                        .load(path)
                        .apply(options)
                        .submit(widgetSize, widgetSize)
                        .get()
                    views.setImageViewBitmap(R.id.widgetImageview, image)
                } catch (e: Exception) {
                }

                setupAppOpenIntent(context, views, R.id.widgetHolder, it)

                try {
                    appWidgetManager.updateAppWidget(it.widgetId, views)
                } catch (ignored: Exception) {
                }
            }
        }
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        onUpdate(context, appWidgetManager, intArrayOf(appWidgetId))
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        ensureBackgroundThread {
            appWidgetIds.forEach {
                context.widgetsDB.deleteWidgetId(it)
            }
        }
    }
}
