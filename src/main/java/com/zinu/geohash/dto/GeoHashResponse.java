package com.zinu.geohash.dto;

import java.util.HashMap;

public record GeoHashResponse(String segment, HashMap<String, String> neighbours) {}
