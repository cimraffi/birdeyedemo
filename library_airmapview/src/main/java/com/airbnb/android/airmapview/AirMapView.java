package com.airbnb.android.airmapview;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.airbnb.android.airmapview.MapUtils.LatLng;
import com.airbnb.android.airmapview.MapUtils.LatLngBounds;
import com.airbnb.android.airmapview.MapUtils.Marker;
import com.airbnb.android.airmapview.listeners.InfoWindowCreator;
import com.airbnb.android.airmapview.listeners.OnCameraChangeListener;
import com.airbnb.android.airmapview.listeners.OnCameraMoveListener;
import com.airbnb.android.airmapview.listeners.OnInfoWindowClickListener;
import com.airbnb.android.airmapview.listeners.OnLatLngScreenLocationCallback;
import com.airbnb.android.airmapview.listeners.OnMapBoundsCallback;
import com.airbnb.android.airmapview.listeners.OnMapClickListener;
import com.airbnb.android.airmapview.listeners.OnMapInitializedListener;
import com.airbnb.android.airmapview.listeners.OnMapLoadedListener;
import com.airbnb.android.airmapview.listeners.OnMapMarkerClickListener;
import com.airbnb.android.airmapview.listeners.OnMapMarkerDragListener;
import com.airbnb.android.airmapview.listeners.OnPositionCallback;
import com.airbnb.android.airmapview.listeners.OnSetOptionsCallback;

import org.json.JSONException;

public class AirMapView extends FrameLayout
    implements OnCameraChangeListener, OnMapClickListener, OnMapMarkerDragListener,
    OnMapMarkerClickListener, OnMapLoadedListener {

  private static final int INVALID_ZOOM = -1;

  protected AirMapInterface mapInterface;
  private OnCameraMoveListener onCameraMoveListener;
  private OnCameraChangeListener onCameraChangeListener;
  private boolean mOnCameraMoveTriggered;
  private OnMapInitializedListener onMapInitializedListener;
  private OnMapMarkerClickListener onMapMarkerClickListener;
  private OnMapMarkerDragListener onMapMarkerDragListener;
  private OnMapClickListener onMapClickListener;
  private OnInfoWindowClickListener onInfoWindowClickListener;

  public AirMapView(Context context) {
    super(context);
    inflateView();
  }

  public AirMapView(Context context, AttributeSet attrs) {
    super(context, attrs);
    inflateView();
  }

  public AirMapView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    inflateView();
  }

  private void inflateView() {
    LayoutInflater.from(getContext()).inflate(R.layout.map_view, this);
  }

  public void initialize(FragmentManager fragmentManager, AirMapInterface mapInterface) {
    if (mapInterface == null || fragmentManager == null) {
      throw new IllegalArgumentException("Either mapInterface or fragmentManager is null");
    }

    this.mapInterface = mapInterface;
    this.mapInterface.setOnMapLoadedListener(this);
    try {
      fragmentManager.beginTransaction()
              .replace(getId(), (Fragment) this.mapInterface)
              .commitAllowingStateLoss();

      fragmentManager.executePendingTransactions();
    }catch (Exception e){
      e.printStackTrace();
    }
  }

  /**
   * Used for initialization of the underlying map provider.
   *
   * @param fragmentManager required for initialization
   */
  public void initialize(FragmentManager fragmentManager) {
    AirMapInterface mapInterface = (AirMapInterface)
        fragmentManager.findFragmentById(R.id.map_frame);

    if (mapInterface != null) {
      initialize(fragmentManager, mapInterface);
    } else {
      initialize(fragmentManager, new DefaultAirMapViewBuilder(getContext()).builder().build());
    }
  }

  public void setOnMapInitializedListener(OnMapInitializedListener mapInitializedListener) {
    onMapInitializedListener = mapInitializedListener;
  }

  @Override public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
    if (ev.getAction() == MotionEvent.ACTION_MOVE) {
      if (onCameraMoveListener != null && !mOnCameraMoveTriggered) {
        onCameraMoveListener.onCameraMove();
        mOnCameraMoveTriggered = true;
      }
    } else if (ev.getAction() == MotionEvent.ACTION_UP) {
      mOnCameraMoveTriggered = false;
    }

    return super.dispatchTouchEvent(ev);
  }

  public void setOnCameraChangeListener(OnCameraChangeListener onCameraChangeListener) {
    this.onCameraChangeListener = onCameraChangeListener;
  }

  /**
   * Sets the map {@link com.airbnb.android.airmapview.listeners.OnCameraMoveListener}
   *
   * @param onCameraMoveListener The OnCameraMoveListener to be set
   */
  public void setOnCameraMoveListener(OnCameraMoveListener onCameraMoveListener) {
    this.onCameraMoveListener = onCameraMoveListener;
  }

  public final AirMapInterface getMapInterface() {
    return mapInterface;
  }

  public void onDestroyView() {
    if (isInitialized()) {
      mapInterface.setMyLocationEnabled(false);
    }
  }

  public int getZoom() {
    if (isInitialized()) {
      return mapInterface.getZoom();
    }

    return INVALID_ZOOM;
  }

  public LatLng getCenter() {
    if (isInitialized()) {
      return mapInterface.getCenter();
    }
    return null;
  }

  public boolean setCenter(LatLng latLng) {
    if (isInitialized()) {
      mapInterface.setCenter(latLng);
      return true;
    }
    return false;
  }

  public boolean animateCenter(LatLng latLng) {
    if (isInitialized()) {
      mapInterface.animateCenter(latLng);
      return true;
    }
    return false;
  }

  public boolean setZoom(int zoom) {
    if (isInitialized()) {
      mapInterface.setZoom(zoom);
      return true;
    }
    return false;
  }

  public boolean setCenterZoom(LatLng latLng, int zoom) {
    if (isInitialized()) {
      mapInterface.setCenterZoom(latLng, zoom);
      return true;
    }
    return false;
  }

  public boolean animateCenterZoom(LatLng latLng, int zoom) {
    if (isInitialized()) {
      mapInterface.animateCenterZoom(latLng, zoom);
      return true;
    }
    return false;
  }

  public boolean setBounds(LatLngBounds latLngBounds, int boundsPadding) {
    if (isInitialized()) {
      mapInterface.setCenter(latLngBounds, boundsPadding);
      return true;
    }
    return false;
  }

  public void getScreenBounds(OnMapBoundsCallback callback) {
    if (isInitialized()) {
      mapInterface.getMapScreenBounds(callback);
    }
  }

  public void getMapMarkerScreenLocation(LatLng latLng, OnLatLngScreenLocationCallback callback) {
    if (isInitialized()) {
      mapInterface.getScreenLocation(latLng, callback);
    }
  }

  public <T> boolean setOptions(AirMapPolygon<T> polygon, int rotate, int space, OnSetOptionsCallback callback) {
    if (isInitialized()) {
      mapInterface.setOptions(polygon, rotate, space, callback);
      return true;
    }
    return false;
  }

  public void drawCircle(LatLng latLng, int radius) {
    if (isInitialized()) {
      mapInterface.drawCircle(latLng, radius);
    }
  }

  public void drawCircle(LatLng latLng, int radius, int strokeColor) {
    if (isInitialized()) {
      mapInterface.drawCircle(latLng, radius, strokeColor);
    }
  }

  public void drawCircle(LatLng latLng, int radius, int strokeColor, int strokeWidth) {
    if (isInitialized()) {
      mapInterface.drawCircle(latLng, radius, strokeColor, strokeWidth);
    }
  }

  public void drawCircle(LatLng latLng, int radius, int strokeColor, int strokeWidth,
      int fillColor) {
    if (isInitialized()) {
      mapInterface.drawCircle(latLng, radius, strokeColor, strokeWidth, fillColor);
    }
  }

  public void setPadding(int left, int top, int right, int bottom) {
    if (isInitialized()) {
      mapInterface.setPadding(left, top, right, bottom);
    }
  }

  public void setOnMarkerClickListener(OnMapMarkerClickListener listener) {
    onMapMarkerClickListener = listener;
  }

  public void setOnMarkerDragListener(OnMapMarkerDragListener listener) {
    onMapMarkerDragListener = listener;
  }

  public void setOnMapClickListener(OnMapClickListener listener) {
    onMapClickListener = listener;
  }

  public void setOnInfoWindowClickListener(OnInfoWindowClickListener listener) {
    onInfoWindowClickListener = listener;
  }

  public void clearMarkers() {
    if (isInitialized()) {
      mapInterface.clearMarkers();
    }
  }

  public <T> boolean addPolyline(AirMapPolyline<T> polyline) {
    if (isInitialized()) {
      mapInterface.addPolyline(polyline);
      return true;
    }
    return false;
  }

  public void setMapType(MapType mapType) {
    mapInterface.setMapType(mapType);
  }

  public <T> boolean removePolyline(AirMapPolyline<T> polyline) {
    if (isInitialized()) {
      mapInterface.removePolyline(polyline);
      return true;
    }
    return false;
  }

  public <T> boolean addPolygon(AirMapPolygon<T> polygon) {
    if (isInitialized()) {
      mapInterface.addPolygon(polygon);
      return true;
    }
    return false;
  }

  public <T> boolean removePolygon(AirMapPolygon<T> polygon) {
    if (isInitialized()) {
      mapInterface.removePolygon(polygon);
      return true;
    }
    return false;
  }

  public void setGeoJsonLayer(AirMapGeoJsonLayer layer) throws JSONException {
    if (!isInitialized()) {
      return;
    }
    mapInterface.setGeoJsonLayer(layer);
  }

  public void clearGeoJsonLayer() {
    if (!isInitialized()) {
      return;
    }
    mapInterface.clearGeoJsonLayer();
  }

  public boolean isInitialized() {
    return mapInterface != null && mapInterface.isInitialized();
  }

  public boolean addXVI(String xvi_id){
    if (isInitialized()) {
      mapInterface.addXVI(xvi_id);
      return true;
    }
    return false;
  }

  public boolean addTiff(String ws, String layer,long taskId){
    if (isInitialized()) {
      mapInterface.addTiff(ws, layer, taskId);
      return true;
    }
    return false;
  }
  public boolean addTiffNotClear(String ws, String layer,long taskId){
    if (isInitialized()) {
      mapInterface.addTiffNotClear(ws, layer, taskId);
      return true;
    }
    return false;
  }
  public boolean addBinaryTiff(String ws, String layer){
    if (isInitialized()) {
      mapInterface.addBinaryTiff(ws, layer);
      return true;
    }
    return false;
  }

  public boolean addMarker(AirMapMarker<?> marker) {
    if (isInitialized()) {
      mapInterface.addMarker(marker);
      return true;
    }
    return false;
  }

  public boolean setPhoneMarker(AirMapMarker<?> marker) {
    if (isInitialized()) {
      mapInterface.setPhoneMarker(marker);
      return true;
    }
    return false;
  }

  public boolean addOverlay(AirMapMarker<?> marker) {
    if (isInitialized()) {
      mapInterface.addOverlay(marker);
      return true;
    }
    return false;
  }

  public boolean moveOverlay(AirMapMarker<?> marker, LatLng to, double rotate) {
    if (isInitialized()) {
      mapInterface.moveOverlay(marker, to, rotate);
      return true;
    }
    return false;
  }

  public boolean rotateOverlay(AirMapMarker<?> marker, double rotate) {
    if (isInitialized()) {
      mapInterface.rotateOverlay(marker, rotate);
      return true;
    }
    return false;
  }

  public boolean addText(AirMapMarker<?> marker) {
    if (isInitialized()) {
      mapInterface.addText(marker);
      return true;
    }
    return false;
  }

  public boolean removeMarker(AirMapMarker<?> marker) {
    if (isInitialized()) {
      mapInterface.removeMarker(marker);
      return true;
    }
    return false;
  }

  public boolean moveMarker(AirMapMarker<?> marker, LatLng to) {
    if (isInitialized()) {
      mapInterface.moveMarker(marker, to);
      return true;
    }
    return false;
  }

  public void setMyLocationEnabled(boolean trackUserLocation) {
    mapInterface.setMyLocationEnabled(trackUserLocation);
  }

  public void setPositionCallback(OnPositionCallback callback) {
    if (isInitialized()) {
      mapInterface.setOnPositionCallback(callback);
    }
  }

  public void setPositionCallback(boolean modify,OnPositionCallback callback) {
    if (isInitialized()) {
      mapInterface.setOnPositionCallback(modify,callback);
    }
  }

  public void setMyLocationButtonEnabled(boolean enabled) {
    mapInterface.setMyLocationButtonEnabled(enabled);
  }

  @Override public void onCameraChanged(LatLng latLng, int zoom) {
    if (onCameraChangeListener != null) {
      onCameraChangeListener.onCameraChanged(latLng, zoom);
    }
  }

  @Override public void onMapClick(LatLng latLng) {
    if (onMapClickListener != null) {
      onMapClickListener.onMapClick(latLng);
    }
  }

  @Override public void onMapMarkerClick(AirMapMarker<?> airMarker) {
    if (onMapMarkerClickListener != null) {
      onMapMarkerClickListener.onMapMarkerClick(airMarker);
    }
  }

  @Override public void onMapMarkerDragStart(Marker marker) {
    if (onMapMarkerDragListener != null) {
      onMapMarkerDragListener.onMapMarkerDragStart(marker);
    }
  }

  @Override public void onMapMarkerDrag(Marker marker) {
    if (onMapMarkerDragListener != null) {
      onMapMarkerDragListener.onMapMarkerDrag(marker);
    }
  }

  @Override public void onMapMarkerDragEnd(Marker marker) {
    if (onMapMarkerDragListener != null) {
      onMapMarkerDragListener.onMapMarkerDragEnd(marker);
    }
  }

  @Override public void onMapMarkerDragStart(long id, LatLng latLng) {
    if (onMapMarkerDragListener != null) {
      onMapMarkerDragListener.onMapMarkerDragStart(id, latLng);
    }
  }

  @Override public void onMapMarkerDrag(long id, LatLng latLng) {
    if (onMapMarkerDragListener != null) {
      onMapMarkerDragListener.onMapMarkerDrag(id, latLng);
    }
  }

  @Override public void onMapMarkerDragEnd(long id, LatLng latLng) {
    if (onMapMarkerDragListener != null) {
      onMapMarkerDragListener.onMapMarkerDragEnd(id, latLng);
    }
  }

  @Override public void onMapLoaded() {
    if (isInitialized()) {
      mapInterface.setOnCameraChangeListener(this);
      mapInterface.setOnMapClickListener(this);
      mapInterface.setOnMarkerClickListener(this);
      mapInterface.setOnMarkerDragListener(this);

      if (onMapInitializedListener != null) {
        // only send map Initialized callback if map initialized successfully
        // initialization can fail if the map leaves the screen before it loads
        onMapInitializedListener.onMapInitialized();
      }
    }
  }

  public void clearTiff() {
    mapInterface.clearTiff();
  }
  public void clearBinary() {
    mapInterface.clearBinary();
  }
}
