package com.hippo.fresco.large;

/*
 * Created by Hippo on 5/30/2017.
 */

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Map;
import java.util.Set;

import android.content.Context;

import com.facebook.common.internal.ImmutableList;
import com.facebook.drawee.backends.pipeline.DrawableFactory;
import com.facebook.drawee.backends.pipeline.DraweeConfig;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilderSupplier;
import com.facebook.imageformat.DefaultImageFormats;
import com.facebook.imageformat.ImageFormat;
import com.facebook.imagepipeline.animated.factory.AnimatedFactory;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.decoder.DefaultImageDecoder;
import com.facebook.imagepipeline.decoder.ImageDecoder;
import com.facebook.imagepipeline.decoder.ImageDecoderConfig;

import com.hippo.fresco.large.decoder.ImageRegionDecoderFactory;
import com.hippo.fresco.large.decoder.SkiaImageRegionDecoderFactory;

public final class FrescoLarge {
  private FrescoLarge() {}

  private static final int THRESHOLD_SIZE = 1024;

  private static PipelineDraweeControllerBuilderSupplier sDraweeControllerBuilderSupplier;

  /** Initializes FrescoLarge with the default config. */
  public static void initialize(@Nonnull Context context) {
    initialize(context, null, null, null, null);
  }

  /** Initializes FrescoLarge with the default ImagePipeline config and the default Drawee config. */
  public static void initialize(
      @Nonnull Context context,
      @Nullable FrescoLargeConfig frescoLargeConfig) {
    initialize(context, frescoLargeConfig, null, null, null);
  }

  /** Initializes FrescoLarge with the default Drawee config. */
  public static void initialize(
      @Nonnull Context context,
      @Nullable FrescoLargeConfig frescoLargeConfig,
      @Nullable ImageDecoderConfig.Builder imageDecoderConfigBuilder,
      @Nullable ImagePipelineConfig.Builder imagePipelineConfigBuilder) {
    initialize(context, frescoLargeConfig, imageDecoderConfigBuilder, imagePipelineConfigBuilder, null);
  }

  /** Initializes Fresco with the specified config. */
  public static void initialize(
      @Nonnull Context context,
      @Nullable FrescoLargeConfig frescoLargeConfig,
      @Nullable ImageDecoderConfig.Builder imageDecoderConfigBuilder,
      @Nullable ImagePipelineConfig.Builder imagePipelineConfigBuilder,
      @Nullable DraweeConfig draweeConfig) {

    if (frescoLargeConfig == null) {
      ImageRegionDecoderFactory decoderFactory = new SkiaImageRegionDecoderFactory();
      frescoLargeConfig = FrescoLargeConfig.newBuilder()
          .addDecoder(DefaultImageFormats.JPEG, decoderFactory)
          .addDecoder(DefaultImageFormats.PNG, decoderFactory)
          .setThresholdSize(THRESHOLD_SIZE, THRESHOLD_SIZE)
          .build();
    }

    imageDecoderConfigBuilder = configImageDecoderConfigBuilder(frescoLargeConfig, imageDecoderConfigBuilder);

    final ImagePipelineConfig imagePipelineConfig;
    if (imageDecoderConfigBuilder != null && imagePipelineConfigBuilder != null) {
      imagePipelineConfig = imagePipelineConfigBuilder
          .setImageDecoderConfig(imageDecoderConfigBuilder.build())
          .build();
    } else if (imageDecoderConfigBuilder == null && imagePipelineConfigBuilder != null) {
      imagePipelineConfig = imagePipelineConfigBuilder.build();
    } else if (imageDecoderConfigBuilder != null) {
      imagePipelineConfig = ImagePipelineConfig.newBuilder(context)
          .setImageDecoderConfig(imageDecoderConfigBuilder.build())
          .build();
    } else {
      imagePipelineConfig = null;
    }

    Fresco.initialize(context, imagePipelineConfig, draweeConfig);

    DraweeConfig largeDraweeConfig = configLargeDraweeConfig(context, draweeConfig);
    initializeLargeDrawee(context, largeDraweeConfig);

    // Init LargeDrawableFactory
    AnimatedFactory factory = Fresco.getImagePipelineFactory().getAnimatedFactory();
    LargeDrawableFactory.initialize(
        Fresco.getImagePipelineFactory().getConfig().getExecutorSupplier().forDecode(),
        factory != null ? factory.getAnimatedDrawableFactory(context) : null);

    // Init LargeImageDecoder
    ImageDecoder decoder = Fresco.getImagePipelineFactory().getImageDecoder();
    if (decoder instanceof DefaultImageDecoder) {
      LargeImageDecoder.initialize((DefaultImageDecoder) decoder);
    }
  }


  @Nullable
  private static ImageDecoderConfig.Builder configImageDecoderConfigBuilder(
      @Nonnull FrescoLargeConfig frescoLargeConfig,
      @Nullable ImageDecoderConfig.Builder imageDecoderConfigBuilder) {
    Set<ImageFormat> imageFormatSet = frescoLargeConfig.getImageFormatSet();
    Map<ImageFormat, ImageRegionDecoderFactory> regionDecoderFactoryMap =
        frescoLargeConfig.getImageRegionDecoderFactoryMap();

    if (imageFormatSet == null || imageFormatSet.isEmpty() ||
        regionDecoderFactoryMap == null || regionDecoderFactoryMap.isEmpty()) {
      return imageDecoderConfigBuilder;
    }

    if (imageDecoderConfigBuilder == null) {
      imageDecoderConfigBuilder = ImageDecoderConfig.newBuilder();
    }

    Map<ImageFormat, ImageFormat.FormatChecker> imageFormatCheckerMap =
        frescoLargeConfig.getImageFormatCheckerMap();
    if (imageFormatCheckerMap != null) {
      for (Map.Entry<ImageFormat, ImageFormat.FormatChecker> entry : imageFormatCheckerMap.entrySet()) {
        // Apply image format checker.
        // Pass null for ImageDecoder, it should be override by LargeImageDecoder
        imageDecoderConfigBuilder.addDecodingCapability(entry.getKey(), entry.getValue(), null);
      }
    }

    LargeImageDecoder largeImageDecoder = new LargeImageDecoder(
        frescoLargeConfig.getImageSizeDecoderMap(),
        frescoLargeConfig.getImageRegionDecoderFactoryMap(),
        frescoLargeConfig.getImageDecoderMap(),
        frescoLargeConfig.getThresholdWidth(),
        frescoLargeConfig.getThresholdHeight());
    for (ImageFormat imageFormat : imageFormatSet) {
      imageDecoderConfigBuilder.overrideDecoder(imageFormat, largeImageDecoder);
    }

    return imageDecoderConfigBuilder;
  }

  @Nullable
  private static DraweeConfig configLargeDraweeConfig(
      @Nonnull Context context,
      @Nullable DraweeConfig draweeConfig) {
    ImmutableList<DrawableFactory> customDrawableFactories =
        draweeConfig != null ? draweeConfig.getCustomDrawableFactories() : null;
    LargeDrawableFactory largeDrawableFactory = new LargeDrawableFactory(context, customDrawableFactories);

    DraweeConfig.Builder builder = DraweeConfig.newBuilder()
        .addCustomDrawableFactory(largeDrawableFactory);
    if (draweeConfig != null) {
      builder.setDebugOverlayEnabledSupplier(draweeConfig.getDebugOverlayEnabledSupplier())
          .setPipelineDraweeControllerFactory(draweeConfig.getPipelineDraweeControllerFactory());
    }

    return builder.build();
  }

  /** Initializes Drawee with the specified config. */
  private static void initializeLargeDrawee(
      Context context,
      @Nullable DraweeConfig draweeConfig) {
    sDraweeControllerBuilderSupplier =
        new PipelineDraweeControllerBuilderSupplier(context, draweeConfig);
    SimpleLargeDraweeView.initialize(sDraweeControllerBuilderSupplier);
  }

  /** Gets the supplier of Fresco Drawee controller builders. */
  public static PipelineDraweeControllerBuilderSupplier getDraweeControllerBuilderSupplier() {
    return sDraweeControllerBuilderSupplier;
  }

  /** Returns a new instance of Fresco Drawee controller builder. */
  public static PipelineDraweeControllerBuilder newDraweeControllerBuilder() {
    return sDraweeControllerBuilderSupplier.get();
  }

  /** Shuts Fresco down. */
  public static void shutDown() {
    Fresco.shutDown();
    sDraweeControllerBuilderSupplier = null;
  }
}
