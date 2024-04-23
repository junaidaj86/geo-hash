package com.zinu.geohash.dto;

public record GeoHashRequest(
     Double latitude, 
     Double longitude, 
     int precision){}