package com.hippo.fresco.large.demo;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.facebook.drawee.view.SimpleDraweeView;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ViewPager pager = (ViewPager) findViewById(R.id.pager);
    pager.setAdapter(new MyPageAdapter(this));
  }
}
