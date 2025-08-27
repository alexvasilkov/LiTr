/*
 * Copyright 2019 LinkedIn Corporation
 * All Rights Reserved.
 *
 * Licensed under the BSD 2-Clause License (the "License").  See License in the project root for
 * license information.
 */
package com.linkedin.android.litr.io;

import android.content.Context;
import android.media.MediaDataSource;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.linkedin.android.litr.exception.MediaSourceException;
import com.linkedin.android.litr.utils.TranscoderUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

import static com.linkedin.android.litr.exception.MediaSourceException.Error.DATA_SOURCE;

/**
 * An implementation of MediaSource, which wraps Android's {@link MediaExtractor}
 */
public class MediaExtractorMediaSource implements MediaSource {

    private final MediaExtractor mediaExtractor = new MediaExtractor();
    private final MediaRange mediaRange;

    private final long size;
    private int orientationHint;
    private long duration;

    public MediaExtractorMediaSource(@NonNull Context context, @NonNull Uri uri) throws MediaSourceException {
        this(context, uri, new MediaRange(0, Long.MAX_VALUE));
    }

    public MediaExtractorMediaSource(@NonNull Context context, @NonNull Uri uri, @NonNull MediaRange mediaRange) throws MediaSourceException {
        this.mediaRange = mediaRange;

        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try {
            mediaExtractor.setDataSource(context, uri, null);
            size = TranscoderUtils.getSize(context, uri);
            mediaMetadataRetriever.setDataSource(context, uri);
            setOrientationFrom(mediaMetadataRetriever);
            setDurationFrom(mediaMetadataRetriever);
        } catch (Throwable ex) {
            throw new MediaSourceException(DATA_SOURCE, uri, ex);
        } finally {
            releaseQuietly(mediaMetadataRetriever);
        }
    }

    @RequiresApi(23)
    public MediaExtractorMediaSource(@NonNull MediaDataSource source, @NonNull MediaRange mediaRange) throws MediaSourceException {
        this.mediaRange = mediaRange;

        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try {
            mediaExtractor.setDataSource(source);
            size = source.getSize();
            mediaMetadataRetriever.setDataSource(source);
            setOrientationFrom(mediaMetadataRetriever);
            setDurationFrom(mediaMetadataRetriever);
        } catch (Throwable ex) {
            try {
                source.close();
            } catch (IOException ignored) {
            }
            throw new MediaSourceException(DATA_SOURCE, null, ex);
        } finally {
            releaseQuietly(mediaMetadataRetriever);
        }
    }

    private void setOrientationFrom(MediaMetadataRetriever retriever) {
        String rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        if (rotation != null) {
            orientationHint = Integer.parseInt(rotation);
        }
    }

    private void setDurationFrom(MediaMetadataRetriever retriever) {
        String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        duration = (durationStr != null) ? Long.parseLong(durationStr) : -1L;
    }

    @Override
    public int getOrientationHint() {
        return orientationHint;
    }

    @Override
    public int getTrackCount() {
        return mediaExtractor.getTrackCount();
    }

    @Override
    @NonNull
    public MediaFormat getTrackFormat(int track) {
        return mediaExtractor.getTrackFormat(track);
    }

    @Override
    public void selectTrack(int track) {
        mediaExtractor.selectTrack(track);
    }

    @Override
    public void seekTo(long position, int mode) {
        mediaExtractor.seekTo(position, mode);
    }

    @Override
    public int getSampleTrackIndex() {
        return mediaExtractor.getSampleTrackIndex();
    }

    @Override
    public int readSampleData(@NonNull ByteBuffer buffer, int offset) {
        return mediaExtractor.readSampleData(buffer, offset);
    }

    @Override
    public long getSampleTime() {
        return mediaExtractor.getSampleTime();
    }

    @Override
    public int getSampleFlags() {
        return mediaExtractor.getSampleFlags();
    }

    @Override
    public void advance() {
        mediaExtractor.advance();
    }

    @Override
    public void release() {
        mediaExtractor.release();
    }

    @Override
    public long getSize() {
        return size;
    }

    @NonNull
    @Override
    public MediaRange getSelection() {
        return mediaRange;
    }

    @Override
    public long getDuration() {
        return duration;
    }

    private void releaseQuietly(MediaMetadataRetriever mediaMetadataRetriever) {
        try {
            mediaMetadataRetriever.release();
        } catch (IOException ex) {
            // Nothing to do.
        }
    }
}
