package com.hippo.fresco.large.demo;

/*
 * Created by Hippo on 6/3/2017.
 */

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hippo.fresco.large.LargeDraweeView;

public class MyPageAdapter extends PagerAdapter {

  private static final String[] SAMPLE_URIS = {
      "https://www.gstatic.com/webp/gallery/1.jpg",
      "https://www.gstatic.com/webp/gallery/2.jpg",
      "https://www.gstatic.com/webp/gallery/3.jpg",
      "https://www.gstatic.com/webp/gallery/4.jpg",
      "https://www.gstatic.com/webp/gallery/5.jpg",
  };

  private final LayoutInflater inflater;

  public MyPageAdapter(Context context) {
    inflater = LayoutInflater.from(context);
  }

  @Override
  public Object instantiateItem(ViewGroup container, int position) {
    LargeDraweeView view = (LargeDraweeView) inflater.inflate(R.layout.pager, container, false);
    view.setImageURI(SAMPLE_URIS[position]);
    container.addView(view);
    return view;
  }

  @Override
  public void destroyItem(ViewGroup container, int position, Object object) {
    container.removeView((View) object);
  }

  @Override
  public int getCount() {
    return SAMPLE_URIS.length;
  }

  @Override
  public boolean isViewFromObject(View view, Object object) {
    return view == object;
  }
}
