package com.airbnb.android.airmapview;

import android.content.res.Resources;
import android.os.Bundle;

import java.util.Locale;

/** Defines maps to be used with {@link com.airbnb.android.airmapview.WebViewMapFragment} */
public class AirMapType {

  private static final String ARG_MAP_DOMAIN = "map_domain";
  private static final String ARG_FILE_NAME = "map_file_name";
  private static final String ARG_MAP_URL = "map_url";
  private static final String ARG_MAP= "map";
  private static final String ARG_SUBDOMAINS = "subdomains";
  private static final String ARG_TMS = "tms";
  private static final String ARG_ATTRIBUTION = "attribution";
  private final String fileName;
  private final String mapUrl;
  private final String domain;
  private String map ;
  private String subdomains ;
  private String tms ;
  private String attribution ;

  public AirMapType(boolean inCN){
    this.fileName = "";
    if (inCN){
      this.mapUrl = "google_map.html";
    }else{
      this.mapUrl = "google_map_us.html";
    }
    this.domain = "";
  }

  public AirMapType(String fileName, String mapUrl, String domain) {
    this.fileName = fileName;
    this.mapUrl = mapUrl;
    this.domain = domain;
  }

  public AirMapType(String mapUrl, String subdomains, String tms, String attribution) {
    this.fileName = "leaflet_map.html";
    this.mapUrl = "GoogleChina";
    this.domain = "";
    this.map = mapUrl;
    this.subdomains = subdomains;
    this.tms = tms;
    this.attribution = attribution;
  }

  public String getMap() {
    return map;
  }

  public void setMap(String map) {
    this.map = map;
  }

  public String getSubdomains() {
    return subdomains;
  }

  public void setSubdomains(String subdomains) {
    this.subdomains = subdomains;
  }

  public String getTms() {
    return tms;
  }

  public void setTms(String tms) {
    this.tms = tms;
  }

  public String getAttribution() {
    return attribution;
  }

  public void setAttribution(String attribution) {
    this.attribution = attribution;
  }

  /** @return the name of the HTML file in /assets */
  String getFileName() {
    return fileName;
  }

  /** @return the base URL for a maps API */
  String getMapUrl() {
    return mapUrl;
  }

  /** @return domain of the maps API to use */
  String getDomain() {
    return domain;
  }

  public Bundle toBundle() {
    return toBundle(new Bundle());
  }

  public Bundle toBundle(Bundle bundle) {
    bundle.putString(ARG_MAP_DOMAIN, getDomain());
    bundle.putString(ARG_MAP_URL, getMapUrl());
    bundle.putString(ARG_FILE_NAME, getFileName());
    bundle.putString(ARG_MAP, getMap());
    bundle.putString(ARG_SUBDOMAINS, getSubdomains());
    bundle.putString(ARG_TMS, getTms());
    bundle.putString(ARG_ATTRIBUTION, getAttribution());
    return bundle;
  }

  public static AirMapType fromBundle(Bundle bundle) {
    AirMapType t =  new AirMapType(
        bundle.getString(ARG_FILE_NAME, ""),
        bundle.getString(ARG_MAP_URL, ""),
        bundle.getString(ARG_MAP_DOMAIN, ""));
    t.setMap(bundle.getString(ARG_MAP, ""));
    t.setSubdomains(bundle.getString(ARG_SUBDOMAINS, ""));
    t.setTms(bundle.getString(ARG_TMS, ""));
    t.setAttribution(bundle.getString(ARG_ATTRIBUTION, ""));
    return t;
  }

  public String getMapData(Resources resources) {
    return AirMapUtils.getStringFromFile(resources, fileName)
        .replace("MAPURL", mapUrl)
        .replace("LANGTOKEN", Locale.getDefault().getLanguage())
        .replace("REGIONTOKEN", Locale.getDefault().getCountry());
  }

  @SuppressWarnings("RedundantIfStatement")
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || !(o instanceof AirMapType)) {
      return false;
    }

    AirMapType that = (AirMapType) o;

    if (domain != null ? !domain.equals(that.domain) : that.domain != null) {
      return false;
    }

    if (fileName != null ? !fileName.equals(that.fileName) : that.fileName != null) {
      return false;
    }

    if (mapUrl != null ? !mapUrl.equals(that.mapUrl) : that.mapUrl != null) {
      return false;
    }

    return true;
  }

  @Override public int hashCode() {
    int result = fileName != null ? fileName.hashCode() : 0;
    result = 31 * result + (mapUrl != null ? mapUrl.hashCode() : 0);
    result = 31 * result + (domain != null ? domain.hashCode() : 0);
    return result;
  }
}
