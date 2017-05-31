package com.hippo.fresco.large.drawable;

/*
 * Created by Hippo on 5/30/2017.
 */

import android.graphics.drawable.Drawable;

import com.facebook.drawee.backends.pipeline.Fresco;

import com.hippo.fresco.large.CloseableLargeImage;

public class SubsamplingDrawableFactory extends LargeDrawableFactory {

  @Override
  public Drawable createLargeDrawable(CloseableLargeImage image) {
    return new SubsamplingDrawable(image.getDecoder(),
        Fresco.getImagePipelineFactory().getConfig().getExecutorSupplier().forDecode());
  }
}
