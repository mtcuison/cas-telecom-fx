/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.telecomfx.views;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

/**
 * FXML Controller class
 *
 * @author user
 */
public class MP_POS_CashierController implements Initializable {

    @FXML
    private Button btnCancel;
    @FXML
    private Button btnSavePayment;
    @FXML
    private Button btnPay;
    @FXML
    private Button btnVoid;
    @FXML
    private Button btnReprint;
    @FXML
    private Button btnCancelTrans;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}
