package com.zinu.geohash.controller;

import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zinu.geohash.Service.GeoHashService;
import com.zinu.geohash.dto.GeoHashRequest;
import com.zinu.geohash.dto.GeoHashResponse;


@RestController
@RequestMapping("geohash")
public class GeoHashController {
    
    GeoHashService geoHashService;
    
    public GeoHashController(GeoHashService geoHashService) {
        this.geoHashService = geoHashService;
    }

    @PostMapping
    public ResponseEntity<GeoHashResponse> getGeohashAndNeighbours( @RequestBody GeoHashRequest geoHashRequest){
        String geohash = geoHashService.encode(geoHashRequest.latitude(), geoHashRequest.longitude(), geoHashRequest.precision());
        HashMap<String, String> neighbours = geoHashService.neighbours(geohash);
        GeoHashResponse geoHashResponse = new GeoHashResponse(geohash, neighbours);
        return new ResponseEntity<>(geoHashResponse, HttpStatus.OK);
    }

}
