/*
 * Created by  Mobile Dev Team  on 2/5/20 8:51 PM
 * Copyright (c) Resala Charity Organization. All rights reserved.
 */

package com.resala.mobile.qrregister.ui.barCode.camera

import androidx.annotation.GuardedBy
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import java.nio.ByteBuffer

/** Abstract base class of [FrameProcessor].  */
abstract class FrameProcessorBase<T> : FrameProcessor {

    // To keep the latest frame and its metadata.
    @GuardedBy("this")
    private var latestFrame: ByteBuffer? = null

    @GuardedBy("this")
    private var latestFrameMetaData: FrameMetadata? = null

    // To keep the frame and metadata in process.
    @GuardedBy("this")
    private var processingFrame: ByteBuffer? = null

    @GuardedBy("this")
    private var processingFrameMetaData: FrameMetadata? = null

    @Synchronized
    override fun process(
        data: ByteBuffer,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay
    ) {
        latestFrame = data
        latestFrameMetaData = frameMetadata
        if (processingFrame == null && processingFrameMetaData == null) {
            processLatestFrame(graphicOverlay)
        }
    }

    @Synchronized
    private fun processLatestFrame(graphicOverlay: GraphicOverlay) {
        processingFrame = latestFrame
        processingFrameMetaData = latestFrameMetaData
        latestFrame = null
        latestFrameMetaData = null
        val frame = processingFrame ?: return
        val frameMetaData = processingFrameMetaData ?: return
        val metadata = FirebaseVisionImageMetadata.Builder()
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                .setWidth(frameMetaData.width)
                .setHeight(frameMetaData.height)
                .setRotation(frameMetaData.rotation)
                .build()
        val image = FirebaseVisionImage.fromByteBuffer(frame, metadata)
        detectInImage(image)
                .addOnSuccessListener { results ->
                    this@FrameProcessorBase.onSuccess(image, results, graphicOverlay)
                    processLatestFrame(graphicOverlay)
                }
                .addOnFailureListener { this@FrameProcessorBase.onFailure(it) }
    }

    protected abstract fun detectInImage(image: FirebaseVisionImage): Task<T>

    /** Be called when the detection succeeds.  */
    protected abstract fun onSuccess(
        image: FirebaseVisionImage,
        results: T,
        graphicOverlay: GraphicOverlay
    )

    protected abstract fun onFailure(e: Exception)

}
