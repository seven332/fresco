package com.hippo.fresco.large.decoder;

/*
 * Created by Hippo on 5/30/2017.
 */

import javax.annotation.Nullable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;

public class SkiaImageRegionDecoder extends ImageRegionDecoder {

  private BitmapRegionDecoder decoder;

  public SkiaImageRegionDecoder(BitmapRegionDecoder decoder) {
    this.decoder = decoder;
  }

  @Override
  public int getWidth() {
    return decoder.getWidth();
  }

  @Override
  public int getHeight() {
    return decoder.getHeight();
  }

  @Nullable
  @Override
  public Bitmap decode(Rect rect, int sample) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inSampleSize = sample;
    return decoder.decodeRegion(rect, options);
  }

  @Override
  public void close() {
    super.close();
    decoder.recycle();
  }
}
