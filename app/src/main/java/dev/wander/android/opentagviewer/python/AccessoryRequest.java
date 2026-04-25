package dev.wander.android.opentagviewer.python;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Java-to-Python carrier for a single beacon fetch input.
 *
 * <p>Chaquopy exposes the getter methods to Python as attribute access, so the
 * Python side can read the beacon ID and persisted accessory JSON without
 * importing any Java types.
 */
@AllArgsConstructor
@Getter
public class AccessoryRequest {
    private final String beaconId;
    private final String accessoryJson;
}
