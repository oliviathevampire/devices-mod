package com.ultreon.devices.fabric;

import com.ultreon.devices.api.app.Application;
import com.ultreon.devices.api.print.IPrint;
import com.ultreon.devices.api.print.PrintingManager;
import com.ultreon.devices.core.Laptop;

import java.util.List;
import java.util.Map;

public class DevicesImpl {
    public static List<Application> getAPPLICATIONS() {
        return Laptop.getApplicationsForFabric();
    }

    public static Map<String, IPrint.Renderer> getRegisteredRenders(){
        return PrintingManager.getRegisteredRenders();
    }

    public static void setRegisteredRenders(Map<String, IPrint.Renderer> map){
        PrintingManager.setRegisteredRenders(map);
    }
}
