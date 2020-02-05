/*
 * Created by  Mobile Dev Team  on 2/5/20 10:19 PM
 * Copyright (c) Resala Charity Organization. All rights reserved.
 */

package com.resala.mobile.qrregister.ui.barCode.utils

import android.content.Context
import android.graphics.RectF
import android.preference.PreferenceManager
import androidx.annotation.StringRes
import com.google.android.gms.common.images.Size
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.resala.mobile.qrregister.R
import com.resala.mobile.qrregister.ui.barCode.camera.CameraSizePair
import com.resala.mobile.qrregister.ui.barCode.camera.GraphicOverlay

/** Utility class to retrieve shared preferences.  */
object PreferenceUtils {


    fun saveStringPreference(context: Context, @StringRes prefKeyId: Int, value: String?) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(context.getString(prefKeyId), value)
                .apply()
    }



    fun getProgressToMeetBarcodeSizeRequirement(
        overlay: GraphicOverlay,
        barcode: FirebaseVisionBarcode
    ): Float {
        val context = overlay.context
        return if (getBooleanPref(
                context,
                R.string.pref_key_enable_barcode_size_check,
                false
            )
        ) {
            val reticleBoxWidth = getBarcodeReticleBox(
                overlay
            ).width()
            val barcodeWidth = overlay.translateX(barcode.boundingBox?.width()?.toFloat() ?: 0f)
            val requiredWidth = reticleBoxWidth * getIntPref(
                context,
                R.string.pref_key_minimum_barcode_width,
                50
            ) / 100
            (barcodeWidth / requiredWidth).coerceAtMost(1f)
        } else {
            1f
        }
    }

    fun getBarcodeReticleBox(overlay: GraphicOverlay): RectF {
        val context = overlay.context
        val overlayWidth = overlay.width.toFloat()
        val overlayHeight = overlay.height.toFloat()
        val boxWidth = overlayWidth * getIntPref(
            context,
            R.string.pref_key_barcode_reticle_width,
            80
        ) / 100
        val boxHeight = overlayHeight * getIntPref(
            context,
            R.string.pref_key_barcode_reticle_height,
            35
        ) / 100
        val cx = overlayWidth / 2
        val cy = overlayHeight / 2
        return RectF(cx - boxWidth / 2, cy - boxHeight / 2, cx + boxWidth / 2, cy + boxHeight / 2)
    }

    fun shouldDelayLoadingBarcodeResult(context: Context): Boolean =
        getBooleanPref(
            context,
            R.string.pref_key_delay_loading_barcode_result,
            true
        )

    private fun getIntPref(context: Context, @StringRes prefKeyId: Int, defaultValue: Int): Int {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val prefKey = context.getString(prefKeyId)
        return sharedPreferences.getInt(prefKey, defaultValue)
    }

    fun getUserSpecifiedPreviewSize(context: Context): CameraSizePair? {
        return try {
            val previewSizePrefKey = context.getString(R.string.pref_key_rear_camera_preview_size)
            val pictureSizePrefKey = context.getString(R.string.pref_key_rear_camera_picture_size)
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            CameraSizePair(
                    Size.parseSize(sharedPreferences.getString(previewSizePrefKey, null)),
                    Size.parseSize(sharedPreferences.getString(pictureSizePrefKey, null)))
        } catch (e: Exception) {
            null
        }
    }

    private fun getBooleanPref(context: Context, @StringRes prefKeyId: Int, defaultValue: Boolean): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(prefKeyId), defaultValue)
}