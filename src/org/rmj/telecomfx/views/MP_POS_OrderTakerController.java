/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.telecomfx.views;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import org.rmj.appdriver.GRider;

/**
 * FXML Controller class
 *
 * @author user
 */
public class MP_POS_OrderTakerController implements Initializable {

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }
    
    public void setGRider(GRider foGRider){
         this.poGRider = foGRider;
    }
    
    private final String pxeModuleName = "MainController";
    public static GRider poGRider;    
    
}
