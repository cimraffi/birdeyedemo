package com.airbnb.android.airmapview.MapUtils;

public class MarkerOptions {
    private LatLng position;
    private String title;
    private String snippet;
    private float alpha;
    private float anchorU;
    private float anchorV;
    private BitmapDescriptor icon;

    public float getzIndex() {
        return zIndex;
    }

    public void setzIndex(float zIndex) {
        this.zIndex = zIndex;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public boolean isFlat() {
        return flat;
    }

    public void setFlat(boolean flat) {
        this.flat = flat;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isDraggable() {
        return draggable;
    }

    public void setDraggable(boolean draggable) {
        this.draggable = draggable;
    }

    private float zIndex;

    private float rotation;
    private boolean flat;
    private boolean visible;
    private boolean draggable;

    public float getInfoWindowAnchorU() {
        return infoWindowAnchorU;
    }

    public void setInfoWindowAnchorU(float infoWindowAnchorU) {
        this.infoWindowAnchorU = infoWindowAnchorU;
    }

    public float getInfoWindowAnchorV() {
        return infoWindowAnchorV;
    }

    public void setInfoWindowAnchorV(float infoWindowAnchorV) {
        this.infoWindowAnchorV = infoWindowAnchorV;
    }

    private float infoWindowAnchorU, infoWindowAnchorV;

    public float getAnchorU() {
        return anchorU;
    }

    public void setAnchorU(float anchorU) {
        this.anchorU = anchorU;
    }

    public float getAnchorV() {
        return anchorV;
    }

    public void setAnchorV(float anchorV) {
        this.anchorV = anchorV;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public BitmapDescriptor getIcon() {
        return icon;
    }

    public void setIcon(BitmapDescriptor icon) {
        this.icon = icon;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public void  position(LatLng position){
        this.position = position;
    }
    public void  icon(BitmapDescriptor icon){
        this.icon = icon;
    }
    public void  zIndex(float zIndex){
        this.zIndex = zIndex;
    }
    public void  alpha(float alpha){
        this.alpha = alpha;
    }
    public void  rotation(float rotation){
        this.rotation = rotation;
    }
    public void  flat(boolean flat){
        this.flat = flat;
    }
    public void  visible(boolean visible){
        this.visible = visible;
    }
    public void  draggable(boolean draggable){
        this.draggable = draggable;
    }
    public void  snippet(String snippet){
        this.snippet = snippet;
    }
    public void  title(String title){
        this.title = title;
    }
    public void infoWindowAnchor(float u, float v){
        infoWindowAnchorU = u;
        infoWindowAnchorV = v;
    }
    public void anchor(float u, float v){
        infoWindowAnchorU = u;
        infoWindowAnchorV = v;
    }
}
