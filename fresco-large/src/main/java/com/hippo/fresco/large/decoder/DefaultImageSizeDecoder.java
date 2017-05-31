package com.hippo.fresco.large.decoder;

/*
 * Created by Hippo on 5/30/2017.
 */

import javax.annotation.Nullable;

import android.util.Pair;

import com.facebook.imagepipeline.image.EncodedImage;

public class DefaultImageSizeDecoder implements ImageSizeDecoder {

  @Nullable
  @Override
  public Pair<Integer, Integer> decode(EncodedImage encodedImage) {
    encodedImage.parseMetaData();

    int width = encodedImage.getWidth();
    int height = encodedImage.getHeight();

    if (width >= 0 && height >= 0) {
      return new Pair<>(width, height);
    } else {
      return null;
    }
  }
}
