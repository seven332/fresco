package com.hippo.fresco.large;

/*
 * Created by Hippo on 5/30/2017.
 */

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.facebook.imageformat.ImageFormat;
import com.facebook.imagepipeline.decoder.ImageDecoder;

import com.hippo.fresco.large.decoder.ImageRegionDecoderFactory;
import com.hippo.fresco.large.decoder.ImageSizeDecoder;

public class FrescoLargeConfig {

  private final Set<ImageFormat> imageFormatSet;
  private final Map<ImageFormat, ImageFormat.FormatChecker> formatCheckerMap;
  private final Map<ImageFormat, ImageSizeDecoder> sizeDecoderMap;
  private final Map<ImageFormat, ImageRegionDecoderFactory> regionDecoderFactoryMap;
  private final Map<ImageFormat, ImageDecoder> imageDecoderMap;
  private final int thresholdWidth;
  private final int thresholdHeight;

  private FrescoLargeConfig(Builder builder) {
    imageFormatSet = builder.imageFormatSet;
    formatCheckerMap = builder.formatCheckerMap;
    sizeDecoderMap = builder.sizeDecoderMap;
    regionDecoderFactoryMap = builder.regionDecoderFactoryMap;
    imageDecoderMap = builder.imageDecoderMap;
    thresholdWidth = builder.thresholdWidth;
    thresholdHeight = builder.thresholdHeight;
  }

  Set<ImageFormat> getImageFormatSet() {
    return imageFormatSet;
  }

  Map<ImageFormat, ImageFormat.FormatChecker> getImageFormatCheckerMap() {
    return formatCheckerMap;
  }

  Map<ImageFormat, ImageSizeDecoder> getImageSizeDecoderMap() {
    return sizeDecoderMap;
  }

  Map<ImageFormat, ImageRegionDecoderFactory> getImageRegionDecoderFactoryMap() {
    return regionDecoderFactoryMap;
  }

  Map<ImageFormat, ImageDecoder> getImageDecoderMap() {
    return imageDecoderMap;
  }

  int getThresholdWidth() {
    return thresholdWidth;
  }

  int getThresholdHeight() {
    return thresholdHeight;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private Set<ImageFormat> imageFormatSet;
    private Map<ImageFormat, ImageFormat.FormatChecker> formatCheckerMap;
    private Map<ImageFormat, ImageSizeDecoder> sizeDecoderMap;
    private Map<ImageFormat, ImageRegionDecoderFactory> regionDecoderFactoryMap;
    private Map<ImageFormat, ImageDecoder> imageDecoderMap;
    private int thresholdWidth;
    private int thresholdHeight;

    /**
     * Add a decoder for the image format.
     */
    public FrescoLargeConfig.Builder addDecoder(@Nonnull ImageFormat imageFormat,
        @Nonnull ImageRegionDecoderFactory imageRegionDecoderFactory) {
      return addDecoder(imageFormat, null, null, imageRegionDecoderFactory, null);
    }

    /**
     * Add a decoder for the image format.
     *
     * @param imageFormat the image format
     * @param imageFormatChecker the checker to check whether the image format,
     *                           {@code null} to use default checker
     * @param imageSizeDecoder the decoder to decode image size,
     *                         {@code null} to use default decoder
     * @param imageRegionDecoderFactory the factory to create image region decoder
     * @param imageDecoder the decoder to decode the image if the image isn't large,
     *                     {@code null} to use default decoder
     */
    public FrescoLargeConfig.Builder addDecoder(@Nonnull ImageFormat imageFormat,
        @Nullable ImageFormat.FormatChecker imageFormatChecker,
        @Nullable ImageSizeDecoder imageSizeDecoder,
        @Nonnull ImageRegionDecoderFactory imageRegionDecoderFactory,
        @Nullable ImageDecoder imageDecoder) {
      if (imageFormatSet == null) {
        imageFormatSet = new HashSet<>();
      }
      imageFormatSet.add(imageFormat);

      if (imageFormatChecker != null) {
        if (formatCheckerMap == null) {
          formatCheckerMap = new HashMap<>();
        }
        formatCheckerMap.put(imageFormat, imageFormatChecker);
      }

      if (imageSizeDecoder != null) {
        if (sizeDecoderMap == null) {
          sizeDecoderMap = new HashMap<>();
        }
        sizeDecoderMap.put(imageFormat, imageSizeDecoder);
      }

      if (regionDecoderFactoryMap == null) {
        regionDecoderFactoryMap = new HashMap<>();
      }
      regionDecoderFactoryMap.put(imageFormat, imageRegionDecoderFactory);

      if (imageDecoder != null) {
        if (imageDecoderMap == null) {
          imageDecoderMap = new HashMap<>();
        }
        imageDecoderMap.put(imageFormat, imageDecoder);
      }

      return this;
    }

    /**
     * Set the threshold to check whether the image is large.
     */
    public FrescoLargeConfig.Builder setThresholdSize(int width, int height) {
      thresholdWidth = width;
      thresholdHeight = height;
      return this;
    }

    public FrescoLargeConfig build() {
      return new FrescoLargeConfig(this);
    }
  }
}
