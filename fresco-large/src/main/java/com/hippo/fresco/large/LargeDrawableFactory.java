package com.hippo.fresco.large;

/*
 * Created by Hippo on 5/30/2017.
 */

import javax.annotation.Nullable;

import java.util.concurrent.Executor;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.facebook.common.internal.ImmutableList;
import com.facebook.drawee.backends.pipeline.DrawableFactory;
import com.facebook.drawee.drawable.OrientedDrawable;
import com.facebook.imagepipeline.animated.factory.AnimatedDrawableFactory;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.CloseableStaticBitmap;
import com.facebook.imagepipeline.image.EncodedImage;

import com.hippo.fresco.large.drawable.AnimatableStandardizedTransformedDrawable;
import com.hippo.fresco.large.drawable.AnimatableTransformedDrawable;
import com.hippo.fresco.large.drawable.StandardizedTransformedDrawable;
import com.hippo.fresco.large.drawable.Transformed;
import com.hippo.fresco.large.drawable.TransformedDrawable;
import com.hippo.fresco.large.drawable.SubsamplingDrawable;

class LargeDrawableFactory implements DrawableFactory {

  private static Executor sDecodeExecutor;
  private static AnimatedDrawableFactory sAnimatedDrawableFactory;

  static void initialize(Executor decodeExecutor, AnimatedDrawableFactory animatedDrawableFactory) {
    sDecodeExecutor = decodeExecutor;
    sAnimatedDrawableFactory = animatedDrawableFactory;
  }


  private final Context context;
  private final ImmutableList<DrawableFactory> customDrawableFactories;

  public LargeDrawableFactory(
      Context context,
      ImmutableList<DrawableFactory> customDrawableFactories) {
    this.context = context;
    this.customDrawableFactories = customDrawableFactories;
  }

  @Override
  public final boolean supportsImageType(CloseableImage image) {
    return true;
  }

  @Nullable
  @Override
  public final Drawable createDrawable(CloseableImage image) {
    Drawable drawable = null;

    if (image instanceof CloseableLargeImage) {
      drawable = new SubsamplingDrawable(((CloseableLargeImage) image).getDecoder(), sDecodeExecutor);
    } else {
      drawable = createNormalDrawable(image);
      if (drawable instanceof Animatable) {
        drawable = new AnimatableTransformedDrawable(drawable);
      } else if (drawable != null) {
        drawable = new TransformedDrawable(drawable);
      }
    }

    if (drawable instanceof Transformed) {
      if (drawable instanceof Animatable) {
        drawable = new AnimatableStandardizedTransformedDrawable(context, drawable);
      } else {
        drawable = new StandardizedTransformedDrawable(context, drawable);
      }
    }

    return drawable;
  }

  private Drawable createNormalDrawable(CloseableImage image) {
    if (customDrawableFactories != null) {
      for (DrawableFactory factory : customDrawableFactories) {
        if (factory.supportsImageType(image)) {
          Drawable drawable = factory.createDrawable(image);
          if (drawable != null) {
            return drawable;
          }
        }
      }
    }

    if (image instanceof CloseableStaticBitmap) {
      CloseableStaticBitmap closeableStaticBitmap = (CloseableStaticBitmap) image;
      Drawable bitmapDrawable = new BitmapDrawable(
          context.getResources(),
          closeableStaticBitmap.getUnderlyingBitmap());
      if (closeableStaticBitmap.getRotationAngle() == 0 ||
          closeableStaticBitmap.getRotationAngle() == EncodedImage.UNKNOWN_ROTATION_ANGLE) {
        return bitmapDrawable;
      } else {
        return new OrientedDrawable(bitmapDrawable, closeableStaticBitmap.getRotationAngle());
      }
    } else {
      AnimatedDrawableFactory factory = sAnimatedDrawableFactory;
      if (factory != null) {
        return factory.create(image);
      }
    }
    return null;
  }
}
