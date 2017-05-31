package com.hippo.fresco.large.decoder;

/*
 * Created by Hippo on 5/30/2017.
 */

import javax.annotation.Nullable;

import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.image.EncodedImage;

public interface ImageRegionDecoderFactory {

  @Nullable
  ImageRegionDecoder createImageRegionDecoder(EncodedImage encodedImage,
      ImageDecodeOptions options);
}
