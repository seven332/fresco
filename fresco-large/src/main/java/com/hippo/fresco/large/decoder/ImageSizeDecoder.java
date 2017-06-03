package com.hippo.fresco.large.decoder;

/*
 * Created by Hippo on 5/30/2017.
 */

import javax.annotation.Nullable;

import android.util.Pair;

import com.facebook.imagepipeline.image.EncodedImage;

/**
 * {@code ImageSizeDecoder} gets size for given images.
 */
public interface ImageSizeDecoder {

  /**
   * Returns a pair of width and height.
   * Returns {@code null} if can't decode it.
   */
  @Nullable
  Pair<Integer, Integer> decode(EncodedImage encodedImage);
}
