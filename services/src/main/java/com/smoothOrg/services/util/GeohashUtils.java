package com.smoothOrg.services.util;

/**
 * Utility class for converting latitude/longitude to geohash.
 * Using a simple geohash implementation.
 */
public class GeohashUtils {

    private static final String BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";
    private static final int[] BITS = {16, 8, 4, 2, 1};

    /**
     * Encode latitude and longitude into a geohash string.
     *
     * @param latitude  the latitude (-90 to 90)
     * @param longitude the longitude (-180 to 180)
     * @param precision the desired geohash length (typically 5-12)
     * @return the geohash string
     */
    public static String encode(double latitude, double longitude, int precision) {
        double[] latRange = {-90.0, 90.0};
        double[] lonRange = {-180.0, 180.0};
        
        StringBuilder geohash = new StringBuilder();
        boolean isEven = true;
        int bit = 0;
        int ch = 0;

        while (geohash.length() < precision) {
            double mid;
            if (isEven) {
                mid = (lonRange[0] + lonRange[1]) / 2;
                if (longitude > mid) {
                    ch |= BITS[bit];
                    lonRange[0] = mid;
                } else {
                    lonRange[1] = mid;
                }
            } else {
                mid = (latRange[0] + latRange[1]) / 2;
                if (latitude > mid) {
                    ch |= BITS[bit];
                    latRange[0] = mid;
                } else {
                    latRange[1] = mid;
                }
            }

            isEven = !isEven;

            if (bit < 4) {
                bit++;
            } else {
                geohash.append(BASE32.charAt(ch));
                bit = 0;
                ch = 0;
            }
        }

        return geohash.toString();
    }

    /**
     * Encode with default precision of 7 characters (~150m area).
     * Perfect for quick-commerce delivery zones.
     */
    public static String encode(double latitude, double longitude) {
        return encode(latitude, longitude, 7);
    }
}
