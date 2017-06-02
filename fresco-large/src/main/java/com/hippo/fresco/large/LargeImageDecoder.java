package com.hippo.fresco.large;

/*
 * Created by Hippo on 5/30/2017.
 */

import javax.annotation.Nullable;

import java.util.Map;

import android.util.Pair;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imageformat.DefaultImageFormats;
import com.facebook.imageformat.ImageFormat;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.decoder.DefaultImageDecoder;
import com.facebook.imagepipeline.decoder.ImageDecoder;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.image.QualityInfo;

import com.hippo.fresco.large.decoder.DefaultImageSizeDecoder;
import com.hippo.fresco.large.decoder.ImageRegionDecoder;
import com.hippo.fresco.large.decoder.ImageRegionDecoderFactory;
import com.hippo.fresco.large.decoder.ImageSizeDecoder;

class LargeImageDecoder implements ImageDecoder {

  private boolean hasDefaultImageDecoder;
  @Nullable
  private DefaultImageDecoder defaultImageDecoder;

  private final ImageSizeDecoder defaultSizeDecoder;

  @Nullable
  private final Map<ImageFormat, ImageSizeDecoder> sizeDecoderMap;
  private final Map<ImageFormat, ImageRegionDecoderFactory> regionDecoderFactoryMap;
  @Nullable
  private final Map<ImageFormat, ImageDecoder> imageDecoderMap;
  private final int thresholdWidth;
  private final int thresholdHeight;

  public LargeImageDecoder(
      Map<ImageFormat, ImageSizeDecoder> sizeDecoderMap,
      Map<ImageFormat, ImageRegionDecoderFactory> regionDecoderFactoryMap,
      Map<ImageFormat, ImageDecoder> imageDecoderMap,
      int thresholdWidth,
      int thresholdHeight) {
    this.sizeDecoderMap = sizeDecoderMap;
    this.regionDecoderFactoryMap = regionDecoderFactoryMap;
    this.imageDecoderMap = imageDecoderMap;
    this.thresholdWidth = thresholdWidth;
    this.thresholdHeight = thresholdHeight;

    defaultSizeDecoder = new DefaultImageSizeDecoder();
  }

  private DefaultImageDecoder getDefaultImageDecoder() {
    if (!hasDefaultImageDecoder) {
      hasDefaultImageDecoder = true;
      ImageDecoder decoder = Fresco.getImagePipelineFactory().getImageDecoder();
      if (decoder instanceof DefaultImageDecoder) {
        defaultImageDecoder = (DefaultImageDecoder) decoder;
      }
    }
    return defaultImageDecoder;
  }

  private boolean isLargeEnough(int width, int height) {
    return width > thresholdWidth || height > thresholdHeight;
  }

  @Override
  public CloseableImage decode(EncodedImage encodedImage, int length, QualityInfo qualityInfo,
      ImageDecodeOptions options) {
    ImageFormat imageFormat = encodedImage.getImageFormat();

    // Only support full quality
    if (qualityInfo.isOfFullQuality()) {
      // Get image size decoder
      ImageSizeDecoder sizeDecoder = null;
      if (sizeDecoderMap != null) {
        sizeDecoder = sizeDecoderMap.get(imageFormat);
      }
      if (sizeDecoder == null) {
        sizeDecoder = this.defaultSizeDecoder;
      }

      Pair<Integer, Integer> size = sizeDecoder.decode(encodedImage);
      if (size != null && isLargeEnough(size.first, size.second)) {
        ImageRegionDecoderFactory factory = regionDecoderFactoryMap.get(imageFormat);
        if (factory != null) {
          ImageRegionDecoder decoder =
              factory.createImageRegionDecoder(encodedImage, options);
          if (decoder != null) {
            decoder.generatePreview(thresholdWidth, thresholdHeight);
            return new CloseableLargeImage(decoder);
          }
        }
      }
    }

    if (imageDecoderMap != null) {
      ImageDecoder imageDecoder = imageDecoderMap.get(imageFormat);
      if (imageDecoder != null) {
        return imageDecoder.decode(encodedImage, length, qualityInfo, options);
      }
    }

    DefaultImageDecoder defaultImageDecoder = getDefaultImageDecoder();
    if (defaultImageDecoder != null) {
      if (imageFormat == DefaultImageFormats.JPEG) {
        return defaultImageDecoder.decodeJpeg(encodedImage, length, qualityInfo, options);
      } else if (imageFormat == DefaultImageFormats.GIF) {
        return defaultImageDecoder.decodeGif(encodedImage, options);
      } else if (imageFormat == DefaultImageFormats.WEBP_ANIMATED) {
        return defaultImageDecoder.decodeAnimatedWebp(encodedImage, options);
      }
      return defaultImageDecoder.decodeStaticImage(encodedImage, options);
    }

    return null;
  }
}
