package com.hippo.fresco.large.demo;

/*
 * Created by Hippo on 5/30/2017.
 */

import android.app.Application;

import com.facebook.drawee.backends.pipeline.DraweeConfig;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imageformat.DefaultImageFormats;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.decoder.ImageDecoderConfig;

import com.hippo.fresco.large.FrescoLarge;
import com.hippo.fresco.large.FrescoLargeConfig;
import com.hippo.fresco.large.decoder.ImageRegionDecoderFactory;
import com.hippo.fresco.large.decoder.SkiaImageRegionDecoderFactory;

public class DemoApp extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    FrescoLargeConfig.Builder builder = FrescoLargeConfig.newBuilder();
    builder.setThresholdSize(1024, 1024);
    ImageRegionDecoderFactory decoderFactory = new SkiaImageRegionDecoderFactory();
    builder.addDecoder(DefaultImageFormats.JPEG, decoderFactory);
    builder.addDecoder(DefaultImageFormats.PNG, decoderFactory);

    ImageDecoderConfig.Builder decoderConfigBuilder = ImageDecoderConfig.newBuilder();
    DraweeConfig.Builder draweeConfigBuilder = DraweeConfig.newBuilder();
    FrescoLarge.config(this, builder.build(), decoderConfigBuilder, draweeConfigBuilder);

    ImagePipelineConfig imagePipelineConfig = ImagePipelineConfig
        .newBuilder(this)
        .setImageDecoderConfig(decoderConfigBuilder.build())
        .build();

    Fresco.initialize(this, imagePipelineConfig);
    FrescoLarge.initialize(this, draweeConfigBuilder.build());
  }
}
