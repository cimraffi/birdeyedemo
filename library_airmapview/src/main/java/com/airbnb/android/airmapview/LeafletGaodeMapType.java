package com.airbnb.android.airmapview;

public class LeafletGaodeMapType extends LeafletMapType {

  public LeafletGaodeMapType() {
    super("Gaode");
  }

  public LeafletGaodeMapType(String mapUrl, String subdomains, String tms, String attribution) {
    super(mapUrl,subdomains,tms,attribution);
  }
}
