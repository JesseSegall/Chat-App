package edu.stevens.cs522.chat.location;

import android.content.Context;
import android.location.Location;

import edu.stevens.cs522.chat.R;

public class CurrentLocation {

    private final Double latitude;

    private final Double longitude;

    public CurrentLocation(Context context) {
        this.latitude = Double.parseDouble(context.getString(R.string.latitude));
        this.longitude = Double.parseDouble(context.getString(R.string.longitude));
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
}
