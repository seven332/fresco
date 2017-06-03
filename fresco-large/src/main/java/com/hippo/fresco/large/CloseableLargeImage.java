package com.hippo.fresco.large;

/*
 * Created by Hippo on 5/30/2017.
 */

import javax.annotation.Nullable;

import com.facebook.common.references.CloseableReference;
import com.facebook.imagepipeline.image.CloseableImage;

import com.hippo.fresco.large.decoder.ImageRegionDecoder;

class CloseableLargeImage extends CloseableImage {

  private CloseableReference<ImageRegionDecoder> decoderReference;
  private final int width;
  private final int height;

  public CloseableLargeImage(ImageRegionDecoder decoder) {
    this.decoderReference = CloseableReference.of(decoder);
    this.width = decoder.getWidth();
    this.height = decoder.getHeight();
  }

  @Nullable
  public synchronized CloseableReference<ImageRegionDecoder> getDecoder() {
    return decoderReference.cloneOrNull();
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getHeight() {
    return height;
  }

  @Override
  public int getSizeInBytes() {
    return decoderReference.get().getSize();
  }

  @Override
  public boolean isStateful() {
    // Don't keep it in memory cache
    return true;
  }

  @Override
  public void close() {
    CloseableReference<ImageRegionDecoder> reference = detachDecoderReference();
    if (reference != null) {
      reference.close();
    }
  }

  private synchronized CloseableReference<ImageRegionDecoder> detachDecoderReference() {
    CloseableReference<ImageRegionDecoder> reference = decoderReference;
    decoderReference = null;
    return reference;
  }

  @Override
  public synchronized boolean isClosed() {
    return decoderReference == null;
  }
}
