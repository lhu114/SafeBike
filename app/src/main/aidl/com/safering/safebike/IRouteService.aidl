// IRouteService.aidl
package com.safering.safebike;

import com.safering.safebike.IRouteCallback;
// Declare any non-default types here with import statements

interface IRouteService {
    boolean startRouting();
    boolean simulationStartRouting();
    boolean initialStartRouting();
    boolean activateWithinRouteLimitDistance();
    boolean activateAutoFinishNavigation();
    boolean registerCallback(IRouteCallback callback);
    boolean unregisterCallback(IRouteCallback callback);
}
