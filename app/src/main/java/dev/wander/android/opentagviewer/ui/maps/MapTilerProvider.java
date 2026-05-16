package dev.wander.android.opentagviewer.ui.maps;

import android.os.Bundle;
import android.view.View;
import org.maplibre.android.MapLibre;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.Style;
import dev.wander.android.opentagviewer.R;

public class MapTilerProvider implements IMapProvider {
    private MapView mapView;

    @Override
    public View getMapView() {
        return mapView;
    }

    @Override
    public void clear() {
        if (mapView != null) {
            mapView.getMapAsync(map -> {
                map.clear();
            });
        }
    }

    // NEW: Fulfill the MapToolbar interface requirement (No-op for MapLibre)
    @Override
    public void setMapToolbarEnabled(boolean enabled) {
        // MapLibre doesn't use the Google Maps toolbar, so we safely ignore this.
    }

    public void onCreate(View rootView, Bundle savedInstanceState) {
        String key = "MAPTILER_KEY_PLACEHOLDER";
        MapLibre.getInstance(rootView.getContext());
        
        mapView = rootView.findViewById(R.id.map);
        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
            String styleUrl = "https://api.maptiler.com/maps/streets-v2/style.json?key=" + key;
            mapView.getMapAsync(map -> map.setStyle(new Style.Builder().fromUri(styleUrl)));
        }
    }

    public void onStart() { if(mapView != null) mapView.onStart(); }
    public void onResume() { if(mapView != null) mapView.onResume(); }
    public void onPause() { if(mapView != null) mapView.onPause(); }
    public void onStop() { if(mapView != null) mapView.onStop(); }
    public void onDestroy() { if(mapView != null) mapView.onDestroy(); }
}
