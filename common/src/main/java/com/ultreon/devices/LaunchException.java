package com.ultreon.devices;

public class LaunchException extends RuntimeException {
    @Override
    public String getMessage() {
        return "The developer version of the Device Mod has been detected and can only be run in a Forge development " +
                "environment. If you are not a developer, download the normal version (https://mrcrayfish.com/mods?id=devices)";
    }
}
