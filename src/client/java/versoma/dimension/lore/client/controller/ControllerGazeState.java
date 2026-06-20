package versoma.dimension.lore.client.controller;

public class ControllerGazeState {
    public static boolean isControlled = false;
    public static float targetYaw = 0f;
    public static float targetPitch = 0f;
    public static float speedFactor = 0f;

    public static float currentFovMod = 1.0f;
    public static float targetFovMod = 1.0f;

    public static void reset() {
        isControlled = false;
        targetFovMod = 1.0f;
    }
}