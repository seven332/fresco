package com.hippo.fresco.large;

/*
 * Created by Hippo on 5/30/2017.
 */

import javax.annotation.Nonnull;

import java.util.Map;
import java.util.Set;

import android.content.Context;

import com.facebook.common.logging.FLog;
import com.facebook.drawee.backends.pipeline.DraweeConfig;
import com.facebook.imageformat.ImageFormat;
import com.facebook.imagepipeline.decoder.ImageDecoderConfig;

import com.hippo.fresco.large.decoder.ImageRegionDecoderFactory;

public final class FrescoLarge {
  private FrescoLarge() {}

  private static final Class<?> TAG = FrescoLarge.class;

  public static void config(
      @Nonnull Context context,
      @Nonnull ImageDecoderConfig.Builder decoderConfigBuilder,
      @Nonnull DraweeConfig.Builder draweeConfigBuilder,
      @Nonnull FrescoLargeConfig config) {
    Set<ImageFormat> imageFormatSet = config.getImageFormatSet();
    if (imageFormatSet == null || imageFormatSet.isEmpty()) {
      FLog.w(TAG, "No ImageFormat");
      return;
    }

    Map<ImageFormat, ImageRegionDecoderFactory> regionDecoderFactoryMap =
        config.getImageRegionDecoderFactoryMap();
    if (regionDecoderFactoryMap == null || regionDecoderFactoryMap.isEmpty()) {
      FLog.w(TAG, "No ImageRegionDecoderFactory");
      return;
    }

    Map<ImageFormat, ImageFormat.FormatChecker> imageFormatCheckerMap =
        config.getImageFormatCheckerMap();
    if (imageFormatCheckerMap != null) {
      for (Map.Entry<ImageFormat, ImageFormat.FormatChecker> entry :
          config.getImageFormatCheckerMap().entrySet()) {
        // Apply image format checker.
        // Pass null for ImageDecoder, it should be override by LargeImageDecoder
        decoderConfigBuilder.addDecodingCapability(entry.getKey(), entry.getValue(), null);
      }
    }

    LargeImageDecoder largeImageDecoder = new LargeImageDecoder(config.getImageSizeDecoderMap(),
        config.getImageRegionDecoderFactoryMap(), config.getImageDecoderMap(),
        config.getThresholdWidth(), config.getThresholdHeight());
    for (ImageFormat imageFormat : config.getImageFormatSet()) {
      decoderConfigBuilder.overrideDecoder(imageFormat, largeImageDecoder);
    }

    draweeConfigBuilder.addCustomDrawableFactory(new LargeDrawableFactory(context));
  }
}
