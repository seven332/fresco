package com.hippo.fresco.large.decoder;

/*
 * Created by Hippo on 5/30/2017.
 */

import javax.annotation.Nullable;

import android.util.Pair;

import com.facebook.imagepipeline.image.EncodedImage;

public interface ImageSizeDecoder {

  @Nullable
  Pair<Integer, Integer> decode(EncodedImage encodedImage);
}
