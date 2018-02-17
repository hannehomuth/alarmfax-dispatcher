package de.feuerwehr.kremmen.dispatcher.alarm;

/**
 *
 * @author jhomuth
 */
public class Adress {

    private String street;

    private String city;

    private String cityArea;

    private String housenumber;

    private String postalCode;

    private String livingarea;

    /**
     * Get the value of livingarea
     *
     * @return the value of livingarea
     */
    public String getLivingarea() {
        return livingarea;
    }

    /**
     * Set the value of livingarea
     *
     * @param livingarea new value of livingarea
     */
    public void setLivingarea(String livingarea) {
        this.livingarea = livingarea;
    }

    /**
     * Get the value of postalCode
     *
     * @return the value of postalCode
     */
    public String getPostalCode() {
        return postalCode;
    }

    /**
     * Set the value of postalCode
     *
     * @param postalCode new value of postalCode
     */
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    /**
     * Get the value of housenumber
     *
     * @return the value of housenumber
     */
    public String getHousenumber() {
        return housenumber;
    }

    /**
     * Set the value of housenumber
     *
     * @param housenumber new value of housenumber
     */
    public void setHousenumber(String housenumber) {
        this.housenumber = housenumber;
    }

    /**
     * Get the value of cityArea
     *
     * @return the value of cityArea
     */
    public String getCityArea() {
        return cityArea;
    }

    /**
     * Set the value of cityArea
     *
     * @param cityArea new value of cityArea
     */
    public void setCityArea(String cityArea) {
        this.cityArea = cityArea;
    }

    /**
     * Get the value of city
     *
     * @return the value of city
     */
    public String getCity() {
        return city;
    }

    /**
     * Set the value of city
     *
     * @param city new value of city
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Get the value of street
     *
     * @return the value of street
     */
    public String getStreet() {
        return street;
    }

    /**
     * Set the value of street
     *
     * @param street new value of street
     */
    public void setStreet(String street) {
        this.street = street;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("Street:").append(this.street).append("\n");
        b.append("Housenumber:").append(this.housenumber).append("\n");
        b.append("Postcode:").append(this.postalCode).append("\n");
        b.append("City:").append(this.city).append("\n");
        b.append("CityArea:").append(this.cityArea).append("\n");
        b.append("LivingArea:").append(this.livingarea).append("\n");
        return b.toString();
    }
    
    

}
