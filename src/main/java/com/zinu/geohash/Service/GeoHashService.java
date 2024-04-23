package com.zinu.geohash.Service;

import java.util.HashMap;

public interface GeoHashService {
    public String encode(double lat, double lon, int precision);
    public HashMap<String, Double> decode(String geohash);
    public HashMap<String, HashMap<String, Double>> bounds(String geohash);
    public String adjacent(String geohash, char direction);
    public HashMap<String, String> neighbours(String geohash);

}
