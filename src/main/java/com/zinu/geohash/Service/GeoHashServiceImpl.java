package com.zinu.geohash.Service;

import java.util.HashMap;

import org.springframework.stereotype.Service;

@Service
public class GeoHashServiceImpl implements GeoHashService{

    private final String BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";
    @Override
    public String encode(double lat, double lon, int precision) {
        int idx = 0; // index into base32 map
        int bit = 0; // each char holds 5 bits
        boolean evenBit = true;
        StringBuilder geohash = new StringBuilder();

        double latMin = -90, latMax = 90;
        double lonMin = -180, lonMax = 180;

        while (geohash.length() < precision) {
            if (evenBit) {
                double lonMid = (lonMin + lonMax) / 2;
                if (lon >= lonMid) {
                    idx = idx * 2 + 1;
                    lonMin = lonMid;
                } else {
                    idx = idx * 2;
                    lonMax = lonMid;
                }
            } else {
                double latMid = (latMin + latMax) / 2;
                if (lat >= latMid) {
                    idx = idx * 2 + 1;
                    latMin = latMid;
                } else {
                    idx = idx * 2;
                    latMax = latMid;
                }
            }
            evenBit = !evenBit;

            if (++bit == 5) {
                geohash.append(BASE32.charAt(idx));
                bit = 0;
                idx = 0;
            }
        }

        return geohash.toString();
    }

    @Override
    public HashMap<String, Double> decode(String geohash) {
        double latMin = -90, latMax = 90;
        double lonMin = -180, lonMax = 180;

        boolean evenBit = true;

        for (int i = 0; i < geohash.length(); i++) {
            int idx = BASE32.indexOf(geohash.charAt(i));
            if (idx == -1) {
                throw new IllegalArgumentException("Invalid geohash");
            }

            for (int n = 4; n >= 0; n--) {
                int bitN = (idx >> n) & 1;
                if (evenBit) {
                    double lonMid = (lonMin + lonMax) / 2;
                    if (bitN == 1) {
                        lonMin = lonMid;
                    } else {
                        lonMax = lonMid;
                    }
                } else {
                    double latMid = (latMin + latMax) / 2;
                    if (bitN == 1) {
                        latMin = latMid;
                    } else {
                        latMax = latMid;
                    }
                }
                evenBit = !evenBit;
            }
        }

        // Calculate center of the geohash cell
        double lat = (latMin + latMax) / 2;
        double lon = (lonMin + lonMax) / 2;

        HashMap<String, Double> location = new HashMap<>();
        location.put("lat", lat);
        location.put("lon", lon);

        return location;
    }

    @Override
    public HashMap<String, HashMap<String, Double>> bounds(String geohash) {
        double latMin = -90, latMax = 90;
        double lonMin = -180, lonMax = 180;

        boolean evenBit = true;

        for (int i = 0; i < geohash.length(); i++) {
            int idx = BASE32.indexOf(geohash.charAt(i));
            if (idx == -1) {
                throw new IllegalArgumentException("Invalid geohash");
            }

            for (int n = 4; n >= 0; n--) {
                int bitN = (idx >> n) & 1;
                if (evenBit) {
                    double lonMid = (lonMin + lonMax) / 2;
                    if (bitN == 1) {
                        lonMin = lonMid;
                    } else {
                        lonMax = lonMid;
                    }
                } else {
                    double latMid = (latMin + latMax) / 2;
                    if (bitN == 1) {
                        latMin = latMid;
                    } else {
                        latMax = latMid;
                    }
                }
                evenBit = !evenBit;
            }
        }

        // Store bounds in a HashMap
        HashMap<String, Double> sw = new HashMap<>();
        sw.put("lat", latMin);
        sw.put("lon", lonMin);

        HashMap<String, Double> ne = new HashMap<>();
        ne.put("lat", latMax);
        ne.put("lon", lonMax);

        HashMap<String, HashMap<String, Double>> bounds = new HashMap<>();
        bounds.put("sw", sw);
        bounds.put("ne", ne);

        return bounds;
    }

    @Override
    public String adjacent(String geohash, char direction) {
        geohash = geohash.toLowerCase();
        direction = Character.toLowerCase(direction);

        if (geohash.isEmpty()) {
            throw new IllegalArgumentException("Invalid geohash");
        }
        if ("nsew".indexOf(direction) == -1) {
            throw new IllegalArgumentException("Invalid direction");
        }

        HashMap<Character, String[]> neighbour = new HashMap<>();
        neighbour.put('n', new String[]{"p0r21436x8zb9dcf5h7kjnmqesgutwvy", "bc01fg45238967deuvhjyznpkmstqrwx"});
        neighbour.put('s', new String[]{"14365h7k9dcfesgujnmqp0r2twvyx8zb", "238967debc01fg45kmstqrwxuvhjyznp"});
        neighbour.put('e', new String[]{"bc01fg45238967deuvhjyznpkmstqrwx", "p0r21436x8zb9dcf5h7kjnmqesgutwvy"});
        neighbour.put('w', new String[]{"238967debc01fg45kmstqrwxuvhjyznp", "14365h7k9dcfesgujnmqp0r2twvyx8zb"});

        HashMap<Character, String[]> border = new HashMap<>();
        border.put('n', new String[]{"prxz", "bcfguvyz"});
        border.put('s', new String[]{"028b", "0145hjnp"});
        border.put('e', new String[]{"bcfguvyz", "prxz"});
        border.put('w', new String[]{"0145hjnp", "028b"});

        char lastCh = geohash.charAt(geohash.length() - 1); // last character of hash
        String parent = geohash.substring(0, geohash.length() - 1); // hash without last character

        int type = geohash.length() % 2;

        // check for edge-cases which don't share common prefix
        if (border.get(direction)[type].indexOf(lastCh) != -1 && !parent.isEmpty()) {
            parent = adjacent(parent, direction);
        }

        // append letter for direction to parent
        return parent + BASE32.charAt(neighbour.get(direction)[type].indexOf(lastCh));
    }

    @Override
    public HashMap<String, String> neighbours(String geohash) {
        HashMap<String, String> neighbours = new HashMap<>();
        neighbours.put("n", adjacent(geohash, 'n'));
        neighbours.put("ne", adjacent(adjacent(geohash, 'n'), 'e'));
        neighbours.put("e", adjacent(geohash, 'e'));
        neighbours.put("se", adjacent(adjacent(geohash, 's'), 'e'));
        neighbours.put("s", adjacent(geohash, 's'));
        neighbours.put("sw", adjacent(adjacent(geohash, 's'), 'w'));
        neighbours.put("w", adjacent(geohash, 'w'));
        neighbours.put("nw", adjacent(adjacent(geohash, 'n'), 'w'));
        return neighbours;
    }

}
