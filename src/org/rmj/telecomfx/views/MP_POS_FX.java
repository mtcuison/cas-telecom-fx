
package org.rmj.telecomfx.views;

import javafx.application.Application;
import org.rmj.appdriver.GProperty;
import org.rmj.appdriver.GRider;


public class MP_POS_FX {
    public static void main(String [] args){       
        String lsProdctID = args[0];
        String lsUserIDxx = args[1];
        GRider poGRider = new GRider(lsProdctID);
        GProperty loProp = new GProperty("GhostRiderXP");
        if (!poGRider.loadEnv(lsProdctID)) System.exit(0);
        if (!poGRider.logUser(lsProdctID, lsUserIDxx)) System.exit(0);
        
        TelecomFX MP_POSFX = new TelecomFX();
        MP_POSFX.setGRider(poGRider);
        
        Application.launch(MP_POSFX.getClass());
    }
    
}