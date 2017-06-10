package com.hippo.fresco.large.demo;

/*
 * Created by Hippo on 5/30/2017.
 */

import android.app.Application;

import com.hippo.fresco.large.FrescoLarge;

public class DemoApp extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    FrescoLarge.initialize(this);
  }
}
