package com.hippo.fresco.large.decoder;

/*
 * Created by Hippo on 5/30/2017.
 */

import javax.annotation.Nullable;

import java.io.Closeable;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.annotation.CallSuper;

public abstract class ImageRegionDecoder implements Closeable {

  private Bitmap preview;
  private int previewSample;

  /**
   * Returns the width of the image.
   */
  public abstract int getWidth();

  /**
   * Returns the height of the image.
   */
  public abstract int getHeight();

  /**
   * Decodes a region for the image.
   */
  @Nullable
  public abstract Bitmap decode(Rect rect, int sample);

  private int getSample(int maxWidth, int maxHeight) {
    int widthScale = (int) Math.ceil((float) getWidth() / (float) maxWidth);
    int heightScale = (int) Math.ceil((float) getHeight() / (float) maxHeight);
    return Math.max(1, Math.max(nextPow2(widthScale), nextPow2(heightScale)));
  }

  public void generatePreview(int maxWidth, int maxHeight) {
    Rect rect = new Rect(0, 0, getWidth(), getHeight());
    previewSample = getSample(maxWidth, maxHeight);
    preview = decode(rect, previewSample);
  }

  @Nullable
  public Bitmap getPreview() {
    return preview;
  }

  public int getPreviewSample() {
    return previewSample;
  }

  @CallSuper
  public int getSize() {
    return preview.getRowBytes() * preview.getHeight();
  }

  @CallSuper
  @Override
  public void close() {
    if (preview != null) {
      preview.recycle();
      preview = null;
    }
  }

  private static int nextPow2(int n) {
    if (n == 0) return 1;
    n -= 1;
    n |= n >> 1;
    n |= n >> 2;
    n |= n >> 4;
    n |= n >> 8;
    n |= n >> 16;
    return n + 1;
  }
}
