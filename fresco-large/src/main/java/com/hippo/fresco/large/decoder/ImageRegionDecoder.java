package com.hippo.fresco.large.decoder;

/*
 * Created by Hippo on 5/30/2017.
 */

import javax.annotation.Nullable;

import java.io.Closeable;

import android.graphics.Bitmap;
import android.graphics.Rect;

public interface ImageRegionDecoder extends Closeable {

  int getWidth();

  int getHeight();

  @Nullable
  Bitmap decode(Rect rect, int sample);

  @Override
  void close();
}
