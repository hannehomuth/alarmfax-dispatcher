package de.feuerwehr.kremmen.dispatcher.alarm;

/**
 *
 * @author jhomuth
 */
public class Coordinates {

    private String longitude;

    //52
    private String latitude;
    
        private boolean found = Boolean.FALSE;

    /**
     * Get the value of found
     *
     * @return the value of found
     */
    public boolean isFound() {
        return found;
    }

    /**
     * Set the value of found
     *
     * @param found new value of found
     */
    public void setFound(boolean found) {
        this.found = found;
    }


    /**
     * Get the value of latitude
     *
     * @return the value of latitude
     */
    public String getLatitude() {
        return latitude;
    }

    /**
     * Set the value of latitude
     *
     * @param latitude new value of latitude
     */
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    /**
     * Get the value of longitude
     *
     * @return the value of longitude
     */
    public String getLongitude() {
        return longitude;
    }

    /**
     * Set the value of longitude
     *
     * @param longitude new value of longitude
     */
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

}
