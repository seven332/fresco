package com.hippo.fresco.large.decoder;

/*
 * Created by Hippo on 5/30/2017.
 */

import javax.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;

import android.graphics.BitmapRegionDecoder;

import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.image.EncodedImage;

public class SkiaImageRegionDecoderFactory implements ImageRegionDecoderFactory {

  @Nullable
  @Override
  public ImageRegionDecoder createImageRegionDecoder(EncodedImage encodedImage,
      ImageDecodeOptions options) {
    InputStream is = encodedImage.getInputStream();
    if (is != null) {
      try {
        BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(is, false);
        if (decoder != null) {
          return new SkiaImageRegionDecoder(decoder);
        }
      } catch (IOException e) {
        // Ignore
      }
    }
    return null;
  }
}
