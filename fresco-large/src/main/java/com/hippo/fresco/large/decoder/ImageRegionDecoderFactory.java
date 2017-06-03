package com.hippo.fresco.large.decoder;

/*
 * Created by Hippo on 5/30/2017.
 */

import javax.annotation.Nullable;

import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.image.EncodedImage;

/**
 * {@code ImageRegionDecoderFactory} creates {@link ImageRegionDecoder} for given images.
 */
public interface ImageRegionDecoderFactory {

  /**
   * Creates a {@link ImageRegionDecoder} for the given image.
   * Returns {@code null} if can't create it.
   */
  @Nullable
  ImageRegionDecoder createImageRegionDecoder(EncodedImage encodedImage,
      ImageDecodeOptions options);
}
