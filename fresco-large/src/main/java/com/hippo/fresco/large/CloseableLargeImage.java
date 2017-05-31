package com.hippo.fresco.large;

/*
 * Created by Hippo on 5/30/2017.
 */

import javax.annotation.Nullable;

import com.facebook.common.references.CloseableReference;
import com.facebook.imagepipeline.image.CloseableImage;

import com.hippo.fresco.large.decoder.ImageRegionDecoder;

public class CloseableLargeImage extends CloseableImage {

  private CloseableReference<ImageRegionDecoder> decoderReference;
  private final int length;
  private final int width;
  private final int height;

  public CloseableLargeImage(ImageRegionDecoder decoder, int length) {
    this.decoderReference = CloseableReference.of(decoder);
    this.length = length >= 0 ? length : 0;
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
    return length;
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
