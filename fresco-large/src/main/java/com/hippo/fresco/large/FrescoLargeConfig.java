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

  public FrescoLargeConfig(Builder builder) {
    imageFormatSet = builder.imageFormatSet;
    formatCheckerMap = builder.formatCheckerMap;
    sizeDecoderMap = builder.sizeDecoderMap;
    regionDecoderFactoryMap = builder.regionDecoderFactoryMap;
    imageDecoderMap = builder.imageDecoderMap;
    thresholdWidth = builder.thresholdWidth;
    thresholdHeight = builder.thresholdHeight;
  }

  public Set<ImageFormat> getImageFormatSet() {
    return imageFormatSet;
  }

  public Map<ImageFormat, ImageFormat.FormatChecker> getImageFormatCheckerMap() {
    return formatCheckerMap;
  }

  public Map<ImageFormat, ImageSizeDecoder> getImageSizeDecoderMap() {
    return sizeDecoderMap;
  }

  public Map<ImageFormat, ImageRegionDecoderFactory> getImageRegionDecoderFactoryMap() {
    return regionDecoderFactoryMap;
  }

  public Map<ImageFormat, ImageDecoder> getImageDecoderMap() {
    return imageDecoderMap;
  }

  public int getThresholdWidth() {
    return thresholdWidth;
  }

  public int getThresholdHeight() {
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

    public FrescoLargeConfig.Builder addDecoder(@Nonnull ImageFormat imageFormat,
        @Nonnull ImageRegionDecoderFactory imageRegionDecoderFactory) {
      return addDecoder(imageFormat, null, null, imageRegionDecoderFactory, null);
    }

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
