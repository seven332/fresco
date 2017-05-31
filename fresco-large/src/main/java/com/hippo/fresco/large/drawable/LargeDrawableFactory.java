package com.hippo.fresco.large.drawable;

/*
 * Created by Hippo on 5/30/2017.
 */

import javax.annotation.Nullable;

import android.graphics.drawable.Drawable;

import com.facebook.drawee.backends.pipeline.DrawableFactory;
import com.facebook.imagepipeline.image.CloseableImage;

import com.hippo.fresco.large.CloseableLargeImage;

public abstract class LargeDrawableFactory implements DrawableFactory {

  @Override
  public final boolean supportsImageType(CloseableImage image) {
    return image instanceof CloseableLargeImage;
  }

  @Nullable
  @Override
  public final Drawable createDrawable(CloseableImage image) {
    return createLargeDrawable((CloseableLargeImage) image);
  }

  public abstract Drawable createLargeDrawable(CloseableLargeImage image);
}
