package com.hippo.fresco.large.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.facebook.drawee.view.SimpleDraweeView;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    SimpleDraweeView image = (SimpleDraweeView) findViewById(R.id.image);
    image.setImageURI("res:///" + R.raw.jpeg_large);
  }
}
