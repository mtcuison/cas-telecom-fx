/*
    iMac 2018-08-04 03:00pm
        Started creating this class.
*/
package org.rmj.telecomfx.views.child;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.json.simple.JSONObject;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.CommonUtils;
import org.rmj.appdriver.agentfx.ui.showFXDialog;


public class ModifySPDetailController implements Initializable {

    @FXML private TextField txtField01;
    @FXML private TextField txtField02;
    @FXML private TextField txtField03;
    @FXML private TextField txtField04;
    @FXML private TextField txtField05;
    @FXML private TextField txtField06;
    @FXML private TextField txtField07;
    @FXML private TextField txtField08;
    @FXML private Button btnOkay;
    @FXML private Button btnCancel;
    @FXML private AnchorPane acRoot;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        txtField01.setText(sStockIDx);
        txtField02.setText(sBarCodex);
        txtField03.setText(sDescript);
        txtField04.setText(CommonUtils.NumberFormat(nUnitPrce, "#,##0.00"));
        txtField05.setText("-"); //String.valueOf(nQtyOnHnd)
        txtField06.setText(String.valueOf(nQuantity));
        txtField07.setText(CommonUtils.NumberFormat(nDiscRate*100, "0.00"));
        txtField08.setText(CommonUtils.NumberFormat(nAddDiscx, "#,##0.00"));
        
        txtField06.setDisable(cSerialze.equals("1"));
        btnOkay.setOnAction(this::cmdButton_Click);
        btnCancel.setOnAction(this::cmdButton_Click);
        
        txtField01.setOnKeyPressed(this::txtField_KeyPressed);
        txtField02.setOnKeyPressed(this::txtField_KeyPressed);
        txtField03.setOnKeyPressed(this::txtField_KeyPressed);
        txtField04.setOnKeyPressed(this::txtField_KeyPressed);
        txtField05.setOnKeyPressed(this::txtField_KeyPressed);
        txtField06.setOnKeyPressed(this::txtField_KeyPressed);
        txtField07.setOnKeyPressed(this::txtField_KeyPressed);
        txtField08.setOnKeyPressed(this::txtField_KeyPressed);
        
        txtField04.focusedProperty().addListener(masterFocus);
        txtField05.focusedProperty().addListener(masterFocus);
        txtField06.focusedProperty().addListener(masterFocus);
        txtField07.focusedProperty().addListener(masterFocus);
        txtField08.focusedProperty().addListener(masterFocus);
    }
    
    public void txtField_KeyPressed(KeyEvent event) {
        TextField txtField = (TextField)event.getSource();
        int lnIndex = Integer.parseInt(txtField.getId().substring(8, 10));
        String lsValue = txtField.getText();
            
        if(event.getCode() == KeyCode.F3){
            switch(lnIndex){
                case 7:
                case 8:
                    searchDiscount(lnIndex == 7);
                    txtField07.setText(String.valueOf(nDiscRate));
                    txtField08.setText(String.valueOf(nAddDiscx));
            }
        }
        
        switch (event.getCode()) {
                case ENTER:
                case DOWN:
                    CommonUtils.SetNextFocus(txtField);
                    break;
                case UP:
                    CommonUtils.SetPreviousFocus(txtField);
                    break;
                default:
                    break;
            }
    }
    
    public boolean searchDiscount(boolean fbRateDisc){
        String lsHeader = "Code»Description»Rate»Additional»From»Thru";
        String lsColName = "sDiscIDxx»sDescript»nDiscRate»nAddDiscx»dDateFrom»dDateThru";
        String lsColCrit = "sDiscIDxx»sDescript»nDiscRate»nAddDiscx»dDateFrom»dDateThru";
        String lsSQL = "SELECT" +
                            "  sDiscIDxx" +
                            ", sDescript" +
                            ", nDiscRate" +
                            ", nAddDiscx" +
                            ", dDateFrom" +
                            ", dDateThru" + 
                        " FROM Promo_Discount" +
                        " WHERE cRecdStat = '1'" +
                            " AND " + SQLUtil.toSQL(oApp.getServerDate()) + " BETWEEN dDateFrom AND dDateThru";
        
        String lsCondition = "";
        
        if (fbRateDisc)
            lsCondition  = "nDiscRate > 0.00 AND nDiscRate <= 1.00";
        else
            lsCondition  = "nAddDiscx > 0.00";
        
        
        lsSQL = MiscUtil.addCondition(lsSQL, lsCondition);
        System.out.println(lsSQL);
        
        JSONObject loJSON = showFXDialog.jsonSearch(oApp, 
                                            lsSQL, 
                                            "", 
                                            lsHeader, 
                                            lsColName, 
                                            lsColCrit, 
                                            1);
        
        if (loJSON != null){
            nDiscRate = Double.parseDouble(String.valueOf(loJSON.get("nDiscRate")));
            nAddDiscx = Double.parseDouble(String.valueOf(loJSON.get("nAddDiscx")));            
            return true;
        }
        
        //reset the discounts
        nDiscRate = 0.00;
        nAddDiscx  = 0.00;
        
        return false;
    }
    
    public void cmdButton_Click(ActionEvent event) {
        String lsButton = ((Button)event.getSource()).getId();
        
        switch (lsButton){
            case "btnOkay":
                bCancelled = false;
                break;
            case "btnCancel":
                bCancelled = true;
        }
        unloadScene(event);
    }
    
    private void unloadScene(ActionEvent event){
        Node source = (Node)  event.getSource(); 
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
    
    final ChangeListener<? super Boolean> masterFocus = (o,ov,nv)->{
        if(!nv){
            TextField txtField = (TextField)((ReadOnlyBooleanPropertyBase)o).getBean();
            int lnIndex = Integer.parseInt(txtField.getId().substring(8, 10));
            String lsValue = txtField.getText();
            double lnValue = 0;
            int lnVal = 0;
            switch (lnIndex){
                case 4: //unit price
                    try {
                        lnValue = Double.parseDouble(lsValue);
                        nUnitPrce = lnValue;
                    } catch (NumberFormatException e) {
                        nUnitPrce = 0.0;
                    }
                    
                    txtField.setText(String.valueOf(CommonUtils.NumberFormat(nUnitPrce, "#,##0.00")));
                    break;
                case 5: //on hand
                    try {
                        lnVal = Integer.parseInt(lsValue);
                        nQtyOnHnd = lnVal;
                    } catch (NumberFormatException e) {
                        nQtyOnHnd = 1;
                    }
                    break;
                case 6: //order
                    try {
                        lnVal = Integer.parseInt(lsValue);
                        nQuantity = lnVal; // > nQtyOnHnd ? nQtyOnHnd : lnVal; 
                    } catch (NumberFormatException e) {
                        nQuantity = 1;
                    }
                    txtField.setText(String.valueOf(nQuantity));
                    break;
                /*
                case 7: //disc rate
                    try {
                        lnValue = Double.parseDouble(lsValue);
                        if (lnValue > 100) lnValue = 100;
                        
                        nDiscRate = lnValue/100;
                    } catch (NumberFormatException e) {
                        nDiscRate = 0.0;
                    }
                    txtField.setText(CommonUtils.NumberFormat(lnValue, "#,##0.00"));
                    break;
                case 8: //add disc
                    try {
                        lnValue = Double.parseDouble(lsValue);
                        nAddDiscx = lnValue; 
                    } catch (NumberFormatException e) {
                        nAddDiscx = 0.0;
                    }
                    txtField.setText(CommonUtils.NumberFormat(nAddDiscx, "#,##0.00"));
                    break;
                */
                default:
            }
        }
    };
    
    //todo: textfield validations
    
    public void setStockIDx(String fsStockIDx){sStockIDx = fsStockIDx;}
    public void setBarCodex(String fsBarCodex){sBarCodex = fsBarCodex;}
    public void setDescript(String fsDescript){sDescript = fsDescript;}
    public void setUnitPrce(double fsUnitPrce){nUnitPrce = fsUnitPrce;}
    public void setQtyOnHnd(int fnQtyOnHnd){nQtyOnHnd = fnQtyOnHnd;}
    public void setQuantity(int fnQuantity){nQuantity = fnQuantity;}
    public void setDiscRate(double fnDiscRate){nDiscRate = fnDiscRate;}
    public void setAddDiscx(double fnAddDiscx){nAddDiscx = fnAddDiscx;}
    public void IsSerialized(String fsValue){cSerialze = fsValue;}
    
    public void setGRider(GRider foValue){
        oApp = foValue;
    }
    
    public void setEditMode(int fnValue){
        nEditMode = fnValue;
    }
    
    public int getEditMode(){
    return nEditMode;
    }
    
    public String getStockIDx(){return sStockIDx;}
    public String getBarCodex(){return sBarCodex;}
    public String getDescript(){return sDescript;}
    public double getUnitPrce(){return nUnitPrce;}
    public int getQtyOnHnd(){return nQtyOnHnd;}
    public int getQuantity(){return nQuantity;}
    public double getDiscRate(){return nDiscRate;}
    public double getAddDiscx(){return nAddDiscx;}
    public String IsSerialized(){return cSerialze;}
    
    public boolean isCancelled(){return bCancelled;}
    
    private int nEditMode ;
    private static String sStockIDx = "";
    private static String sBarCodex = "";
    private static String sDescript = "";
    private static String cSerialze = "0";
    private static double nUnitPrce = 0.0;
    private static int nQtyOnHnd = 0;
    private static int nQuantity = 0;
    private static double nDiscRate = 0.0;
    private static double nAddDiscx = 0.0;
    private static GRider oApp;
    
    private boolean bCancelled = false;
    private final String pxeModuleName = ModifySPDetailController.class.getName();
}
