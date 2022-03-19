package com.airbnb.android.airmapview;

import android.graphics.Color;

import com.airbnb.android.airmapview.MapUtils.LatLng;
import com.airbnb.android.airmapview.MapUtils.Polyline;

import java.util.List;

/**
 * Helper class for keeping record of data needed to display a polyline, as well as an optional
 * object T associated with the polyline.
 */
public class AirMapPolyline<T> {

  private static final int STROKE_WIDTH = 1;
  private static final int STROKE_COLOR = Color.BLUE;

  private T object;
  private int strokeWidth;
  private long id;
  private List<LatLng> points;
  private String title;
  private int strokeColor;
  private Polyline googlePolyline;

  public AirMapPolyline(List<LatLng> points, long id) {
    this(null, points, id);
  }

  public AirMapPolyline(T object, List<LatLng> points, long id) {
    this(object, points, id, STROKE_WIDTH, STROKE_COLOR);
  }

  public AirMapPolyline(T object, List<LatLng> points, long id, int strokeWidth, int strokeColor) {
    this.object = object;
    this.points = points;
    this.id = id;
    this.strokeWidth = strokeWidth;
    this.strokeColor = strokeColor;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public List<LatLng> getPoints() {
    return points;
  }

  public void setPoints(List<LatLng> points) {
    this.points = points;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public T getObject() {
    return object;
  }

  public void setObject(T object) {
    this.object = object;
  }

  public int getStrokeWidth() {
    return strokeWidth;
  }

  public int getStrokeColor() {
    return strokeColor;
  }
}
