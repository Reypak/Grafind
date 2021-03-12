package com.matt.firebasestorage.Objects;

public class Location {
    // Auto complete data
    String[] locations = {"Jinja", "Kampala", "Mukono", "Mbarara", "Wakiso", "Iganga", "Masaka", "Mbale", "Busia", "Hoima", "Arua", "Gulu"
            , "Tororo", "Kasese", "Kabale"};

    public Location(String[] locations) {
        this.locations = locations;
    }

    public String[] getLocations() {
        return locations;
    }

    public void setLocations(String[] locations) {
        this.locations = locations;
    }

    public Location() {
    }
}
