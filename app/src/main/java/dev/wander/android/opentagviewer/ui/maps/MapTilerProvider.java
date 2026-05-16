package dev.wander.android.opentagviewer.ui.maps;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import org.maplibre.android.MapLibre;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.Style;

public class MapTilerProvider implements IMapProvider {
    private MapView mapView;

    @Override
    public void initialize(Activity activity, int containerViewId, OnMapReadyCallback callback) {
        String key = "MAPTILER_KEY_PLACEHOLDER";
        MapLibre.getInstance(activity);
        
        mapView = activity.findViewById(containerViewId);
        if (mapView != null) {
            // Kickstart MapLibre's lifecycle manually
            mapView.onCreate(null);
            String styleUrl = "https://api.maptiler.com/maps/streets-v2/style.json?key=" + key;
            mapView.getMapAsync(map -> {
                map.setStyle(new Style.Builder().fromUri(styleUrl));
                if (callback != null) {
                    callback.onMapReady(this);
                }
            });
        }
    }

    // --- INTERFACE CONTRACT SATISFACTION (Stubs) ---
    
    @Override public void setMapStyle(MapStyle mapStyle) {}
    @Override public String addMarker(MapMarker marker) { return ""; }
    @Override public void removeMarker(String markerId) {}
    @Override public void clearMarkers() {}
    @Override public String addPolyline(MapPolyline polyline) { return ""; }
    @Override public void removePolyline(String polylineId) {}
    @Override public void clearPolylines() {}
    @Override public void moveCamera(double latitude, double longitude, float zoom) {}
    
    @Override 
    public void animateCamera(double latitude, double longitude, float zoom, Runnable callback) {
        if (callback != null) callback.run();
    }

    @Override public void setOnMapClickListener(OnMapClickListener listener) {}
    @Override public void setOnMarkerClickListener(OnMarkerClickListener listener) {}
    @Override public void setPadding(int left, int top, int right, int bottom) {}

    @Override 
    public CameraPosition getCameraPosition() {
        return new CameraPosition(0, 0, 0);
    }

    @Override public void setMyLocationButtonEnabled(boolean enabled) {}
    @Override public void setRotateGesturesEnabled(boolean enabled) {}
    @Override public void setCompassEnabled(boolean enabled) {}
    @Override public void setMapToolbarEnabled(boolean enabled) {}

    @Override 
    public void clear() {
        if (mapView != null) {
            mapView.getMapAsync(map -> map.clear());
        }
    }

    @Override 
    public View getMapView() {
        return mapView;
    }

    // --- STANDARD LIFECYCLE HELPERS ---
    // Kept just in case the Activity attempts to trigger them directly
    public void onStart() { if(mapView != null) mapView.onStart(); }
    public void onResume() { if(mapView != null) mapView.onResume(); }
    public void onPause() { if(mapView != null) mapView.onPause(); }
    public void onStop() { if(mapView != null) mapView.onStop(); }
    public void onDestroy() { if(mapView != null) mapView.onDestroy(); }
}
