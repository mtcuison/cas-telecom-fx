package org.rmj.telecomfx.views;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.F3;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.controlsfx.control.textfield.CustomTextField;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.ShowMessageFX;
import org.rmj.appdriver.agentfx.CommonUtils;
import org.rmj.appdriver.constants.CRMEvent;
import org.rmj.appdriver.constants.EditMode;
import org.rmj.appdriver.constants.TransactionStatus;
import org.rmj.cas.client.base.XMClient;
import org.rmj.cas.inventory.base.InvMaster;
import org.rmj.cas.parameter.agent.XMBrand;
import org.rmj.cas.parameter.agent.XMInventoryType;
import org.rmj.sales.agentfx.XMSales;
import org.rmj.telecomfx.views.child.ModifySPDetailController;

public class MP_POS_SalesController implements Initializable {
    
    @FXML private TableView tblSalesMaster;
    @FXML private TableColumn index01;
    @FXML private TableColumn index02;
    @FXML private TableColumn index03;
    @FXML private TableColumn index04;
    @FXML private TableColumn index05;
    @FXML private Label lblTransNox;
    @FXML private TextField txtField03;
    @FXML private TextField txtField06;
    @FXML private CustomTextField txtField07;
    @FXML private Label lblSubTotal;
    @FXML private Button btnReset;
    @FXML private Label lblNetSales;
    @FXML private Label lblAddVat;
    @FXML private TextField txtField12;    
    @FXML private Label lblDate;
    @FXML private Label lblBranch;
    @FXML private CustomTextField txtSeeks50;
    @FXML private CustomTextField txtSeeks51;
    @FXML private TableColumn index06;
    @FXML private TableColumn index07;
    @FXML private TableColumn index08;
    @FXML private TableColumn index09;
    @FXML private Label lblUser;
    @FXML private Label lblDiscount;
    @FXML private MenuBar menuBar;
    @FXML private Button btnPay;
    @FXML private Button btnVoid;
    @FXML private AnchorPane acDetail;
    @FXML private AnchorPane acMasterField;
    @FXML private AnchorPane acMasterLable;
    @FXML private AnchorPane acItem;
    @FXML private Label lblTranStat;
    @FXML private TextField txtField13;
    @FXML private TextField txtField11;
    @FXML private CustomTextField txtField04;
    @FXML private TextArea txtAddress;
    @FXML private Label lblAmountDue;
    @FXML private Label lblVatExclsv;
    @FXML private Button btnClearFilter;
    @FXML private Button btnSave;
    @FXML private Button btnSearch;
    @FXML private Button btnDelItem;
    @FXML private Button btnUpdate;
    @FXML private Button btnNew;
    @FXML private Button btnBrowse;
    @FXML private AnchorPane acFilter;
    @FXML private Label lblFreight;
    @FXML private TableView tblOrders;
    @FXML private TableColumn order01;
    @FXML private TableColumn order02;
    @FXML private TableColumn order03;
    @FXML private TableColumn order04;
    @FXML private TableColumn order05;
    @FXML private MenuItem mnuEndShift;
    @FXML private MenuItem mnuNewDay;
    @FXML private MenuItem mnuLogout;
    @FXML private MenuItem mnuCashDrawer;
    @FXML private MenuItem mnuCancelInvoice;
    @FXML private Label lblDay;
    @FXML private Label lblTime;
    @FXML private Label lblPosDate;
    @FXML private Label lblDate11;
    @FXML private Label lblSeconds;
    @FXML private MenuItem mnuReprintInvoice;
    @FXML private Label lblField00;
    @FXML private Label lblField02;
    @FXML private Label lblField01;
    
    public static final Image search = new Image("org/rmj/telecomfx/images/search.png");
    private final String pxeModuleName = this.getClass().getSimpleName();
    private final String pxeDefaultDte = java.time.LocalDate.now().toString();
    private final String pxeDateFormat = "yyyy-MM-dd";
    private final String pxeInvTypCd = "FsGd";
    private static GRider poGRider;
    private XMSales poTrans;
    private TablePosition pos = null;
    
    private int pnEditMode;
    private String psBranchCd = "";
    private String psOldRec = "";
    private boolean pbLoaded = false;
    private int pnIndex = -1;
    private int pnRow = 0;
    private int pnOldRow = 0;
    
    private double xOffset = 0; 
    private double yOffset = 0;
    
    final MenuItem Exit = new MenuItem("Exit");
    final MenuItem View = new MenuItem("View Profile");
    final MenuItem delete = new MenuItem ("Delete item");
    
    final ContextMenu contextMenu = new ContextMenu(View, Exit);
    final ContextMenu deleteDetail = new ContextMenu(delete);
    
    private ObservableList<TableModel> data = FXCollections.observableArrayList();    
    private ObservableList<TableModel> orders = FXCollections.observableArrayList();    

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (psBranchCd.equals("")) psBranchCd = poGRider.getBranchCode();
  
        poTrans = new XMSales(poGRider, psBranchCd, false);
        
        btnSave.setOnAction(this::cmdButton_Click);
        btnBrowse.setOnAction(this::cmdButton_Click);
        btnNew.setOnAction(this::cmdButton_Click);
        btnSearch.setOnAction(this::cmdButton_Click);
        btnUpdate.setOnAction(this::cmdButton_Click);
        btnDelItem.setOnAction(this::cmdButton_Click);
        btnReset.setOnAction(this::cmdButton_Click);
        btnPay.setOnAction(this::cmdButton_Click);
        btnVoid.setOnAction(this::cmdButton_Click);
        btnClearFilter.setOnAction(this::cmdButton_Click);
        
        txtField04.setOnKeyPressed(this::txtField_KeyPressed);
        txtField06.setOnKeyPressed(this::txtField_KeyPressed);        
        txtField07.setOnKeyPressed(this::txtField_KeyPressed);        
        txtField11.setOnKeyPressed(this::txtField_KeyPressed);        
        txtField12.setOnKeyPressed(this::txtField_KeyPressed);        
        txtField13.setOnKeyPressed(this::txtField_KeyPressed);        
        txtSeeks50.setOnKeyPressed(this::txtField_KeyPressed);
        txtSeeks51.setOnKeyPressed(this::txtField_KeyPressed);
        
        txtField04.focusedProperty().addListener(txtField_Focus);
        txtField06.focusedProperty().addListener(txtField_Focus);
        txtField07.focusedProperty().addListener(txtField_Focus);
        txtField11.focusedProperty().addListener(txtField_Focus);        
        txtField12.focusedProperty().addListener(txtField_Focus);
        txtField13.focusedProperty().addListener(txtField_Focus);
        txtSeeks50.focusedProperty().addListener(txtField_Focus);
        txtSeeks51.focusedProperty().addListener(txtField_Focus);
        
        mnuCancelInvoice.setOnAction(this::mnuItem_Click);
        mnuCashDrawer.setOnAction(this::mnuItem_Click);
        mnuEndShift.setOnAction(this::mnuItem_Click);
        mnuLogout.setOnAction(this::mnuItem_Click);
        mnuNewDay.setOnAction(this::mnuItem_Click);
        mnuReprintInvoice.setOnAction(this::mnuItem_Click);
        
        txtSeeks50.setLeft(new ImageView(search));
        txtSeeks51.setLeft(new ImageView(search));
        txtField04.setLeft(new ImageView(search));
        txtField07.setLeft(new ImageView(search));
        
        String lsDate = poTrans.getDailySales().DailySummary().getTransactionDate();
        lsDate = lsDate.substring(0, 4) + "-" + lsDate.substring(4, 6) + "-" + lsDate.substring(6, 8);
        lblPosDate.setText(lsDate);
        
        String lsTranMode = "";
        if (!System.getProperty("pos.clt.tran.mode").equalsIgnoreCase("a")) lsTranMode = " (Training Mode)";
        
        //set the pos date on system properties
        System.setProperty("pos.clt.date", SQLUtil.dateFormat(poGRider.getServerDate(), SQLUtil.FORMAT_SHORT_DATEX));
        
        lblField00.setText("TelecomFX POS System v1.0" + lsTranMode);
        lblField01.setText("Accreditation No.: " + System.getProperty("pos.clt.accrd.no"));
        lblField02.setText("Machine No.: " + System.getProperty("pos.clt.crm.no"));

        clearFields();
        initRecord();
        
        pnEditMode = poTrans.getEditMode();
        initButton(pnEditMode);
        pbLoaded = true;
    }
    
    private void computeTotal(){
        double lnSubTotal = Double.valueOf(poTrans.getMaster("nTranTotl").toString()) + Double.valueOf(poTrans.getMaster("nFreightx").toString());
        double lnVATRatex = Double.valueOf(poTrans.getMaster("nVATRatex").toString());
        double lnVATExcls = lnSubTotal / lnVATRatex;
        double lnDiscount = lnVATExcls * Double.valueOf(poTrans.getMaster("nDiscount").toString());
        double lnAddDiscx = Double.valueOf(poTrans.getMaster("nAddDiscx").toString()) / lnVATRatex;
        double lnVATSales = lnVATExcls - (lnDiscount + lnAddDiscx);
        double lnVATAmntx = lnVATSales * 0.12;
        double lnAmntDuex = lnVATSales + lnVATAmntx;        
        
        lblSubTotal.setText(String.valueOf(CommonUtils.NumberFormat(lnSubTotal, "#,##0.00")));
        lblVatExclsv.setText(String.valueOf(CommonUtils.NumberFormat(lnVATExcls, "#,##0.00")));
        lblDiscount.setText(String.valueOf(CommonUtils.NumberFormat(lnDiscount + lnAddDiscx, "#,##0.00")));
        lblNetSales.setText(String.valueOf(CommonUtils.NumberFormat(lnVATSales, "#,##0.00")));
        lblAddVat.setText(String.valueOf(CommonUtils.NumberFormat(lnVATAmntx, "#,##0.00")));        
        lblAmountDue.setText(String.valueOf(CommonUtils.NumberFormat(lnAmntDuex, "#,##0.00")));
    }
    
    public void loadDetail2Grid() {
        int lnCtr;
        int lnRow = poTrans.getDetailCount();
        
        JSONObject loJSON;
        String lsBrandNme;
        String lsInvTypNm;
        
        String lsStockIDx;
        String lsSerialID;
        double lnSelPrice;
        double lnDiscount;
        double lnAddDiscx;
        double lnRowTotal;
        double lnTotlOrdr;
        int lnQuantity;

        data.clear();
        lnTotlOrdr = 0;
        
        for(lnCtr = 0; lnCtr <= lnRow -1; lnCtr++){
            lsStockIDx = (String) poTrans.getDetail(lnCtr, "sStockIDx");
            lsSerialID = (String) poTrans.getDetail(lnCtr, "sSerialID");
            lnSelPrice = Double.valueOf(poTrans.getDetail(lnCtr, "nUnitPrce").toString());
            lnDiscount = Double.valueOf(poTrans.getDetail(lnCtr, "nDiscount").toString());
            lnAddDiscx = Double.valueOf(poTrans.getDetail(lnCtr, "nAddDiscx").toString());
            lnQuantity = (int) poTrans.getDetail(lnCtr, "nQuantity");

            lnRowTotal = lnQuantity * lnSelPrice;
            lnRowTotal = lnRowTotal - (lnRowTotal * lnDiscount);
            lnRowTotal = lnRowTotal - lnAddDiscx;
            
            lnTotlOrdr = lnTotlOrdr + lnRowTotal;
            
            InvMaster loInv = new InvMaster(poGRider, psBranchCd, true);
            
            if (poTrans.getMaster("cTranStat").equals(TransactionStatus.STATE_OPEN)){
                if (loInv.SearchStock(lsStockIDx, lsSerialID, true, true)){
                    lsBrandNme = "";
                    lsInvTypNm = "";

                    if (!"".equals((String) loInv.getInventory("sBrandCde"))){
                        XMBrand loBrand = new XMBrand(poGRider, psBranchCd, true);
                        loJSON = loBrand.searchBrand((String) loInv.getInventory("sBrandCde"), true);
                        if (loJSON != null)
                            lsBrandNme = (String) loJSON.get("sDescript");
                    }

                    if (!"".equals((String) loInv.getInventory("sInvTypCd"))){
                        XMInventoryType loType = new XMInventoryType(poGRider, psBranchCd, true);
                        loJSON = loType.searchInvType((String) loInv.getInventory("sInvTypCd"), true);
                        if (loJSON != null)
                            lsInvTypNm = (String) loJSON.get("sDescript");;
                    }

                    if (!lsSerialID.equals(""))
                        data.add(new TableModel(String.valueOf(lnCtr + 1), 
                                                (String) loInv.getSerial("sSerial01"), 
                                                (String) loInv.getInventory("sDescript"), 
                                                CommonUtils.NumberFormat(lnSelPrice, "#,##0.00"),
                                                "-", //1
                                                String.valueOf(lnQuantity),
                                                CommonUtils.NumberFormat(lnDiscount*100, "#,##0.00"),
                                                CommonUtils.NumberFormat(lnAddDiscx, "#,##0.00"),
                                                CommonUtils.NumberFormat(lnRowTotal, "#,##0.00"),
                                                ""));
                    else
                        data.add(new TableModel(String.valueOf(lnCtr + 1), 
                                                (String) loInv.getInventory("sBarCodex"), 
                                                (String) loInv.getInventory("sDescript"), 
                                                CommonUtils.NumberFormat(lnSelPrice, "#,##0.00"),
                                                "-", //String.valueOf(loInv.getMaster("nQtyOnHnd"))
                                                String.valueOf(lnQuantity),
                                                CommonUtils.NumberFormat(lnDiscount*100, "#,##0.00"),
                                                CommonUtils.NumberFormat(lnAddDiscx, "#,##0.00"),
                                                CommonUtils.NumberFormat(lnRowTotal, "#,##0.00"),
                                                ""));
                } else {
                    data.add(new TableModel(String.valueOf(lnCtr + 1), 
                                            "", 
                                            "", 
                                            "0.00",
                                            "0",
                                            "0",
                                            "0.00",
                                            "0.00",
                                            "0.00",
                                            ""));
                }   
            } else{
                if (loInv.SearchSoldStock(lsStockIDx, lsSerialID, true, true)){
                    lsBrandNme = "";
                    lsInvTypNm = "";

                    if (!"".equals((String) loInv.getInventory("sBrandCde"))){
                        XMBrand loBrand = new XMBrand(poGRider, psBranchCd, true);
                        loJSON = loBrand.searchBrand((String) loInv.getInventory("sBrandCde"), true);
                        if (loJSON != null)
                            lsBrandNme = (String) loJSON.get("sDescript");
                    }

                    if (!"".equals((String) loInv.getInventory("sInvTypCd"))){
                        XMInventoryType loType = new XMInventoryType(poGRider, psBranchCd, true);
                        loJSON = loType.searchInvType((String) loInv.getInventory("sInvTypCd"), true);
                        if (loJSON != null)
                            lsInvTypNm = (String) loJSON.get("sDescript");;
                    }

                    if (!lsSerialID.equals(""))
                        data.add(new TableModel(String.valueOf(lnCtr + 1), 
                                                (String) loInv.getSerial("sSerial01"), 
                                                (String) loInv.getInventory("sDescript"), 
                                                CommonUtils.NumberFormat(lnSelPrice, "#,##0.00"),
                                                "-", //1
                                                String.valueOf(lnQuantity),
                                                CommonUtils.NumberFormat(lnDiscount*100, "#,##0.00"),
                                                CommonUtils.NumberFormat(lnAddDiscx, "#,##0.00"),
                                                CommonUtils.NumberFormat(lnRowTotal, "#,##0.00"),
                                                ""));
                    else
                        data.add(new TableModel(String.valueOf(lnCtr + 1), 
                                                (String) loInv.getInventory("sBarCodex"), 
                                                (String) loInv.getInventory("sDescript"), 
                                                CommonUtils.NumberFormat(lnSelPrice, "#,##0.00"),
                                                "-", //String.valueOf(loInv.getMaster("nQtyOnHnd"))
                                                String.valueOf(lnQuantity),
                                                CommonUtils.NumberFormat(lnDiscount*100, "#,##0.00"),
                                                CommonUtils.NumberFormat(lnAddDiscx, "#,##0.00"),
                                                CommonUtils.NumberFormat(lnRowTotal, "#,##0.00"),
                                                ""));
                } else {
                    data.add(new TableModel(String.valueOf(lnCtr + 1), 
                                            "", 
                                            "", 
                                            "0.00",
                                            "0",
                                            "0",
                                            "0.00",
                                            "0.00",
                                            "0.00",
                                            ""));
                }  
            }         
        }
        
        computeTotal();
        pnRow = lnRow -1;
    }
    
    private void setTranStat(String fsValue){
        switch (fsValue){
            case "0":
                lblTranStat.setText("OPEN");break;
            case "1":
                lblTranStat.setText("PAID");break;
            case "2":
                lblTranStat.setText("POSTED");break;
            case "3":
                lblTranStat.setText("CANCELLED");break;
            case "4":
               lblTranStat.setText("VOID");break;
            default:
               lblTranStat.setText("UNKNOWN");
        }    
    }
       
    public void loadTransaction() {
        lblTransNox.setText((String) poTrans.getMaster("sTransNox"));        
        txtField03.setText(CommonUtils.xsDateMedium((Date) poTrans.getMaster("dTransact")));
        
        txtField11.setText(CommonUtils.NumberFormat(Double.valueOf(poTrans.getMaster("nDiscount").toString())*100, "###0.00"));
        txtField12.setText(CommonUtils.NumberFormat(Double.valueOf(poTrans.getMaster("nAddDiscx").toString()), "###0.00"));
        txtField07.setText((String) poTrans.getMaster("sSalesman")); //salesman        
        txtField06.setText((String) poTrans.getMaster("sRemarksx"));
        
        if (Double.valueOf(poTrans.getMaster("nFreightx").toString()) > 0.00){
            txtField13.setText(CommonUtils.NumberFormat(Double.valueOf(poTrans.getMaster("nFreightx").toString()), "###0.00"));
            txtField13.setVisible(true);
            lblFreight.setVisible(true);
        }
        
        JSONObject loJSON = new JSONObject();
        
        //load client info
        if (!poTrans.getMaster("sClientID").toString().equals("")){
            XMClient loClient = new XMClient(poGRider, psBranchCd, true);
            loJSON = loClient.SearchClient((String) poTrans.getMaster("sClientID"), true);
            if (loJSON != null){
                txtField04.setText((String) loJSON.get("sClientNm"));
                txtAddress.setText((String) loJSON.get("xAddressx"));
            }
        }
        
        loadDetail2Grid();
        setTranStat((String) poTrans.getMaster("cTranStat"));
        pnOldRow = 0;
        psOldRec = lblTransNox.getText();
    }
    
    public void clearFields() {     
        lblFreight.setVisible(false);
        txtField13.setVisible(false);
        
        txtSeeks50.setText("");
        txtSeeks51.setText("");
        txtSeeks50.setPromptText("Press F3 to search by CLIENT NAME or Enter for SALES INVOICE");
        txtSeeks51.setPromptText("Press F3 to search by DESCRIPTION or Enter for BAR CODE");

        setTranStat("-1");
        lblTransNox.setText("");
        
        txtField03.setText("");
        txtField04.setText("");
        txtAddress.setText("");
        txtField06.setText("");
        txtField07.setText("");
        txtField11.setText("0.00");
        txtField12.setText("0.00");
        txtField13.setText("0.00");
      
        lblSubTotal.setText("0.00");
        lblVatExclsv.setText("0.00");
        lblDiscount.setText("0.00");
        lblNetSales.setText("0.00");
        lblAddVat.setText("0.00");
        
        lblAmountDue.setText("0.00");
        
        initGrid();
        initOrder();
        loadOrders("", false);
        
        pnRow = 0;
        pnOldRow = 0;
        psOldRec = "";
    }     
    
    public void initRecord(){
        lblBranch.setText((String) poGRider.getBranchName());
        String lsSQL = "SELECT" +
                            " IFNULL(b.sClientNm, 'UNSET ID') xCashierx" + 
                        " FROM xxxSysUser a" + 
                            " LEFT JOIN Client_Master b" +
                                " ON a.sEmployNo = b.sClientID" + 
                        " WHERE a.sUserIDxx = " + SQLUtil.toSQL(poGRider.getUserID());
        
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        try {
            if (loRS.next())
                lblUser.setText("Cashier: " + loRS.getString("xCashierx"));
        } catch (SQLException ex) {
            Logger.getLogger(MP_POS_SalesController.class.getName()).log(Level.SEVERE, null, ex);
        }
        getTime();
    }
    
    public void txtFieldArea_KeyPressed(KeyEvent event) {
        TextArea txtArea = (TextArea)event.getSource();
             
        if (null != event.getCode())switch (event.getCode()) {
            case ENTER:
            case DOWN:
                event.consume();
                CommonUtils.SetNextFocus(txtArea);
                break;
            case UP:
                CommonUtils.SetPreviousFocus(txtArea);
                break;
            default:
                break;
        } 
    }
    
    public void txtField_KeyPressed(KeyEvent event) {        
        TextField txtField = (TextField)event.getSource();
        int lnIndex = Integer.parseInt(txtField.getId().substring(8, 10));
        String lsValue = txtField.getText();
        JSONObject loJSON;
             
        if (event.getCode() == F3 || event.getCode() == ENTER){                
            switch (lnIndex){                
                case 50: //client name  
                    initOrder();
                    loadOrders(lsValue, false);
                    break;
                case 51: //search for item                    
                    if (poTrans.SearchDetail(pnRow, event.getCode() == ENTER ? 80 : 81, lsValue)){
                        poTrans.addDetail();
                        loadDetail2Grid(); 
                        txtField.setText(""); 
                    }else
                        ShowMessageFX.Warning(poTrans.getErrMsg(), pxeModuleName, poTrans.getWarnMsg());
                    break;
                case 4: //client
                    if (lsValue.equals("")) return;
                    loJSON = poTrans.SearchMaster(lnIndex, lsValue);
                    if (loJSON != null){
                        poTrans.setMaster(lnIndex, (String) loJSON.get("sClientID"));
                        txtField.setText((String) loJSON.get("sClientNm"));
                        txtAddress.setText((String) loJSON.get("xAddressx"));
                    } else {
                        poTrans.setMaster(lnIndex, "");
                        txtField.setText("");
                        txtAddress.setText("");
                    }
                    break; 
                case 7: //sales person
                    if (lsValue.equals("")) return;
                    loJSON = poTrans.SearchMaster(lnIndex, lsValue);
                    if (loJSON != null){
                        poTrans.setMaster(lnIndex, (String) loJSON.get("sEmployID"));
                        txtField.setText((String) loJSON.get("sClientNm"));
                    } else {
                        poTrans.setMaster(lnIndex, "");
                        txtField.setText("");
                    }
                    break;
                case 11:
                case 12:
                    if (poTrans.searchDiscount(lnIndex == 11)){
                        txtField11.setText(CommonUtils.NumberFormat(Double.valueOf(poTrans.getMaster("nDiscount").toString()), "0.00"));
                        txtField12.setText(CommonUtils.NumberFormat(Double.valueOf(poTrans.getMaster("nAddDiscx").toString()), "0.00"));
                        loadDetail2Grid();
                    }
                    break;
            }
        } else if (event.getCode() == ENTER){
            switch (lnIndex){
                case 50:
                    initOrder();
                    loadOrders(lsValue, true);
            }
        }
        switch (event.getCode()) {
            case ENTER:
            case DOWN:
                if (lnIndex == 50 || lnIndex == 51) return;
                CommonUtils.SetNextFocus(txtField);
                break;
            case UP:
                CommonUtils.SetPreviousFocus(txtField);
                break;
            default:
                break;
        }
    }
    
    public void ComboBox_KeyPressed(KeyEvent event) {
        ComboBox combo = (ComboBox)event.getSource();
             
        if (null != event.getCode())switch (event.getCode()) {
            case ENTER:
            case DOWN:
                CommonUtils.SetNextFocus(combo);
                break;
            case UP:
                CommonUtils.SetPreviousFocus(combo);
                break;
            default:
                break;
        }
    }
    
    final ChangeListener<? super Boolean> txtField_Focus = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
            if (!pbLoaded) return;
            
            TextField txtField = (TextField)((ReadOnlyBooleanPropertyBase)o).getBean();
            int lnIndex = Integer.parseInt(txtField.getId().substring(8, 10));
            String lsValue = txtField.getText();
            double lnValue = 0.0;
            int lnVal = 0;
            
            if (lsValue == null) return;
            
            if(!nv){ /*Lost Focus*/
                 switch (lnIndex){
                    case 7:
                    case 51:
                         break;
                    case 3:
                        if (CommonUtils.isDate(lsValue, pxeDateFormat)){
                            poTrans.setMaster("dTransact", CommonUtils.toDate(lsValue));
                        } else{
                            ShowMessageFX.Warning("Invalid date entry.", pxeModuleName, "Date format must be yyyy-MM-dd (e.g. 1991-07-07)");
                            poTrans.setMaster(lnIndex, CommonUtils.toDate(pxeDefaultDte));
                        }
                        /*get the value from the class*/
                        txtField.setText(CommonUtils.xsDateLong((Date)poTrans.getMaster("dTransact")));
                        return;
                    /*case 11:
                        try {
                            lnValue = Double.parseDouble(lsValue);
                        } catch (Exception e) {
                            lnValue = 0.0;
                        }
                        
                        poTrans.setMaster("nDiscount", (lnValue > 100 ? 1 : lnValue/100));
                        txtField.setText(CommonUtils.NumberFormat(Double.valueOf(poTrans.getMaster("nDiscount").toString())*100, "0.00"));
                        loadDetail2Grid();
                        break;
                        
                    case 12:
                        try {
                            lnValue = Double.parseDouble(lsValue);
                        } catch (Exception e) {
                            lnValue = 0.0;
                        }
                        poTrans.setMaster("nAddDiscx", lnValue);
                        txtField.setText(CommonUtils.NumberFormat(Double.valueOf(poTrans.getMaster("nAddDiscx").toString()), "0.00"));
                        loadDetail2Grid();
                        break;
                    */    
                    case 6:
                        poTrans.setMaster("sRemarksx", lsValue);
                        txtField.setText((String) poTrans.getMaster("sRemarksx"));
                        break;
                }
            } else{
                switch (lnIndex){
                case 3: /*dTransact*/
                    try{
                        txtField.setText(CommonUtils.xsDateShort(lsValue));
                    }catch(ParseException e){
                        ShowMessageFX.Error(e.getMessage(), pxeModuleName, null);
                    }
                    break;
                default:
                }
                if (lnIndex == 4 || lnIndex == 7 || lnIndex == 50 || lnIndex == 51)
                    pnIndex = lnIndex;
                else pnIndex = -1;
            
                txtField.requestFocus();
                txtField.selectAll();
            }
        }
    }; 
   
    private void mnuItem_Click(ActionEvent event){
        String mnuItem = ((MenuItem)event.getSource()).getId().toLowerCase();
        switch(mnuItem){
            case "mnuendshift":
                if (poTrans.PrintXReading()){
                    ShowMessageFX.Information("Shift successfully closed.", "Notice", "Shift Ended");
                    System.exit(0);
                }
                break;
            case "mnunewday":
                if (!ShowMessageFX.YesNo("Declaring End-Of-Day will finalize the POS for this day.\nDo you want to continue?", "Confirm", "Declaring End-Of-Day Transaction"))
                return;
                    if (poTrans.PrintXReading()){
                        if (poTrans.PrintZReading()){
                            ShowMessageFX.Information("You have successfully declared End-Of-Day.", "Notice", "End-Of-Day");
                            System.exit(0);
                        }
                    }
                break;
            case "mnulogout":
                if (ShowMessageFX.YesNo("Do you want to print cashier sales report?", "Confirm", "Please confirm...")==true) {
                    poTrans.getDailySales().PrintCashierSales(poTrans.getDailySales().DailySummary().getTransactionDate(), 
                                                                poTrans.getDailySales().DailySummary().getMachineNo(), 
                                                                poTrans.getDailySales().DailySummary().getCashier());
                }
                
                CommonUtils.createEventLog(poGRider, poGRider.getBranchCode() + System.getProperty("pos.clt.trmnl.no")
                        , CRMEvent.CASHIER_LOGOUT, "Date: " + poTrans.getDailySales().DailySummary().getTransactionDate() + "; " + "Cashier: " + poTrans.getDailySales().DailySummary().getCashier()
                        , System.getProperty("pos.clt.crm.no"));
                
                System.exit(0);
                break;
            case "mnucashdrawer":
                if (!poTrans.OpenCashDrawer())
                    ShowMessageFX.Warning(poTrans.getErrMsg(), "Warning", null);
                break;
            case "mnucancelinvoice":
                if (!psOldRec.equals("")){
                    if (ShowMessageFX.YesNo("Cancelling paid transaction will also cancel the invoice.", "Confirm", "Do you want to continue?")){
                        if (poTrans.cancelTransaction(psOldRec)){
                            ShowMessageFX.Information(null, pxeModuleName, "Transaction cancelled successfully.");
                            clearFields();
                            initGrid();
                            pnEditMode = EditMode.UNKNOWN;
                            initButton(pnEditMode);
                        } else
                            ShowMessageFX.Warning(poTrans.getWarnMsg(), pxeModuleName, poTrans.getErrMsg());
                    }
                }else
                    ShowMessageFX.Warning("Please select a record to update!", pxeModuleName, "No record to update.");
                break;     
            case "mnureprintinvoice":
                if (!psOldRec.equals("")){
                    if (ShowMessageFX.YesNo(null, "Confirm", "Do you want to re-print invoice?")){
                        if (poTrans.getMaster("cTranStat").equals("3")){
                            if (poTrans.PrintCancelledInvoice(psOldRec)){
                                ShowMessageFX.Information(null, pxeModuleName, "Transaction printed successfully.");
                                clearFields();
                                initGrid();
                                pnEditMode = EditMode.UNKNOWN;
                                initButton(pnEditMode);
                            } else
                                ShowMessageFX.Warning(poTrans.getWarnMsg(), pxeModuleName, poTrans.getErrMsg());
                        } else {
                            if (poTrans.PrintInvoice(psOldRec)){
                                ShowMessageFX.Information(null, pxeModuleName, "Transaction printed successfully.");
                                clearFields();
                                initGrid();
                                pnEditMode = EditMode.UNKNOWN;
                                initButton(pnEditMode);
                            } else
                                ShowMessageFX.Warning(poTrans.getWarnMsg(), pxeModuleName, poTrans.getErrMsg());
                        }
                    }
                }else
                    ShowMessageFX.Warning("Please select a record to update!", pxeModuleName, "No record to update.");
                break;  
            default:
                 ShowMessageFX.Warning("Button"+ mnuItem+ "is not registered...", "Warning", null);
                break;
        }
    
    }
     
    private void cmdButton_Click(ActionEvent event) {
        String lsButton = ((Button)event.getSource()).getId();
        JSONObject loJson;
        switch (lsButton){ 
            case "btnNew":
                if (poTrans.newTransaction()== true){
                    clearFields();
                    
                    poTrans.setMaster("sBranchCd", psBranchCd);
                    poTrans.setMaster("sInvTypCd", pxeInvTypCd);
                            
                    loadTransaction();
                    pnEditMode = poTrans.getEditMode();
                } else  ShowMessageFX.Warning(poTrans.getWarnMsg(), pxeModuleName, poTrans.getErrMsg());
                break;
            case "btnUpdate":
                if (!psOldRec.equals("")){
                    if (poTrans.updateTransaction()){
                        loadDetail2Grid();
                        pnEditMode = poTrans.getEditMode();
                    } else
                        ShowMessageFX.Warning(poTrans.getWarnMsg(), pxeModuleName, poTrans.getErrMsg());
                }else
                    ShowMessageFX.Warning("Please select a record to update!", pxeModuleName, "No record to update.");
                break;
            case "btnSave":
                if (poTrans.saveTransaction()){
                    ShowMessageFX.Information(null, pxeModuleName, "Transaction saved successfully.");
                    clearFields();
                    initGrid();
                    pnEditMode = EditMode.UNKNOWN;
                } else{
                    if (!poTrans.getErrMsg().isEmpty()){
                        if (!poTrans.getWarnMsg().isEmpty())
                            ShowMessageFX.Error(poTrans.getErrMsg(), pxeModuleName, poTrans.getWarnMsg());
                        else 
                            ShowMessageFX.Error(null, pxeModuleName, poTrans.getErrMsg());
                    } else 
                        ShowMessageFX.Information(poTrans.getWarnMsg(), pxeModuleName, null);		
                }
                break;
            case "btnReset":
                if(ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to discard changes?") == true){
                    clearFields();
                    CommonUtils.createEventLog(poGRider, poGRider.getBranchCode() + System.getProperty("pos.clt.trmnl.no")
                            , CRMEvent.CLEAR_ORDER, "Order No.: " + poTrans.getMaster("sTransNox")
                            , System.getProperty("pos.clt.crm.no"));
                    pnEditMode = EditMode.UNKNOWN;
                } 
                break;
            case "btnBrowse":
                if (poTrans.SearchTransaction("", false)){
                    loadTransaction();
                    pnEditMode = poTrans.getEditMode();
                }
                break;
            case "btnPay":
                if (!psOldRec.equals("")){
                    if (psOldRec.equals("")){
                        ShowMessageFX.Warning(null, pxeModuleName, "Please select a transaction to pay.");
                        return;
                    }else if(!poTrans.getMaster("cTranStat").equals("0")){
                        ShowMessageFX.Warning("Transaction already "+ lblTranStat.getText()+ "..!", pxeModuleName, "Unable to pay transaction..");
                        return;
                    }
                    
                    if (poTrans.closeTransaction(psOldRec)){
                        ShowMessageFX.Information(null, pxeModuleName, "Transaction PAYED successfully.");
                        clearFields();
                        initGrid();
                        pnEditMode = EditMode.UNKNOWN;
                    }
                } else 
                    ShowMessageFX.Warning(null, pxeModuleName, "Please select a record to pay!");
                break;
            case "btnVoid":
                if (!psOldRec.equals("")){
                    if (psOldRec.equals("")){
                        ShowMessageFX.Warning(null, pxeModuleName, "Please select a transaction to void.");
                        return;
                    }else if(!poTrans.getMaster("cTranStat").equals("0")){
                        ShowMessageFX.Warning("Transaction already "+ lblTranStat.getText()+ "..!", pxeModuleName, "Unable to void transaction..");
                        return;
                    }
                    
                    if (ShowMessageFX.YesNo(null, pxeModuleName, "Do you want to void this transaction?") == true){
                        if (poTrans.voidTransaction((String) poTrans.getMaster("sTransNox"))){
                            ShowMessageFX.Information(null, pxeModuleName, "Transaction voided successfully.");
                            if (poTrans.openTransaction(psOldRec)) {
                                loadTransaction(); 
                                pnEditMode = poTrans.getEditMode();
                            }
                        } else 
                            ShowMessageFX.Warning(poTrans.getWarnMsg(), pxeModuleName, poTrans.getErrMsg());
                    }
                }else{
                    ShowMessageFX.Warning(null, pxeModuleName, "Please select a transaction to void.");
                }
                break;
            case "btnClearFilter":
                initOrder();
                loadOrders("", false);
                txtSeeks50.setText("");
                return;
            case "btnSearch":
                switch(pnIndex){
                    case 4:
                        if (txtField04.getText().equals("")) return;
                            loJson = poTrans.SearchMaster(pnIndex, txtField04.getText());
                        if (loJson != null){
                            poTrans.setMaster(pnIndex, (String) loJson.get("sClientID"));
                            txtField04.setText((String) loJson.get("sClientNm"));
                            txtAddress.setText((String) loJson.get("xAddressx"));
                        } else {
                            poTrans.setMaster(pnIndex, "");
                            txtField04.setText("");
                            txtAddress.setText("");
                        }
                        break; 
                    case 7:
                        if (txtField07.getText().equals("")) return;
                        loJson = poTrans.SearchMaster(pnIndex, txtField07.getText());
                        if (loJson != null){
                            poTrans.setMaster(pnIndex, (String) loJson.get("sEmployID"));
                            txtField07.setText((String) loJson.get("sClientNm"));
                        } else {
                            poTrans.setMaster(pnIndex, "");
                            txtField07.setText("");
                        }
                        break;
                    case 51:
                        if (poTrans.SearchDetail(pnRow, 81, txtSeeks51.getText())){
                            poTrans.addDetail();
                            loadDetail2Grid(); 
                            txtSeeks51.setText(""); 
                        }else
                            ShowMessageFX.Warning(poTrans.getErrMsg(), pxeModuleName, poTrans.getWarnMsg());
                        break;
                }
                break;
            case "btnDelItem":
                int lnIndex = tblSalesMaster.getSelectionModel().getFocusedIndex();    
                if(tblSalesMaster.getSelectionModel().getSelectedItem() == null){
                     ShowMessageFX.Warning(null, pxeModuleName, "Please select item to remove!");
                     break;
                }
                if(ShowMessageFX.OkayCancel(null, pxeModuleName, "Do you want to remove this item?") == true){
                    if(tblSalesMaster.getItems().size() == 1){
                        poTrans.deleteDetail(lnIndex);
                        poTrans.addDetail();
                        loadDetail2Grid();
                        break;
                    }                  
                                      
                    if (lnIndex >= 0) {                         
                        if (poTrans.deleteDetail(lnIndex) == true) {
                            loadDetail2Grid();
                        }  
                    }
                }                  
                break;
            default:
               ShowMessageFX.Warning(null, pxeModuleName, "Button with name " + lsButton + " not registered.");   
               return;
        }
        initButton(pnEditMode);
    }

    public void setGRider(GRider foGRider) {
        this.poGRider = foGRider;
    }
    
    private void initOrder(){
        order01.setStyle("-fx-alignment: CENTER;");
        order02.setStyle("-fx-alignment: CENTER-LEFT;");
        order03.setStyle("-fx-alignment: CENTER-LEFT;");
        order04.setStyle("-fx-alignment: CENTER-RIGHT;");
        order05.setStyle("-fx-alignment: CENTER;");
        
        order01.prefWidthProperty().bind(tblOrders.widthProperty().multiply(0.05));
        order02.prefWidthProperty().bind(tblOrders.widthProperty().multiply(0.19));
        order03.prefWidthProperty().bind(tblOrders.widthProperty().multiply(0.40));
        order04.prefWidthProperty().bind(tblOrders.widthProperty().multiply(0.15));
        order05.prefWidthProperty().bind(tblOrders.widthProperty().multiply(0.20));
        
        order01.setCellValueFactory(new PropertyValueFactory<org.rmj.telecomfx.views.TableModel,String>("index01"));
        order02.setCellValueFactory(new PropertyValueFactory<org.rmj.telecomfx.views.TableModel,String>("index02"));
        order03.setCellValueFactory(new PropertyValueFactory<org.rmj.telecomfx.views.TableModel,String>("index03"));
        order04.setCellValueFactory(new PropertyValueFactory<org.rmj.telecomfx.views.TableModel,String>("index04"));
        order05.setCellValueFactory(new PropertyValueFactory<org.rmj.telecomfx.views.TableModel,String>("index05"));
        
        tblOrders.setItems(orders);
        orders.clear();
    }
    
    private void loadOrders(String fsValue, boolean fbByCode){
        JSONArray laJSON = poTrans.BrowseTransaction(fsValue, fbByCode);
        JSONObject loJSON;
        
        orders.clear();
        for(int lnCtr = 0; lnCtr <= laJSON.size() -1; lnCtr++){
            loJSON = (JSONObject) laJSON.get(lnCtr);
            
            orders.add(new TableModel(String.valueOf(lnCtr + 1), 
                                        (String) loJSON.get("sTransNox"), 
                                        (String) loJSON.get("sClientNm"), 
                                        (String) loJSON.get("nTranTotl"),
                                        (String) loJSON.get("xTranStat"),
                                        "",
                                        "",
                                        "",
                                        "",
                                        ""));
        }
    }
    
    private void initGrid() {
        index01.setStyle("-fx-alignment: CENTER;");
        index02.setStyle("-fx-alignment: CENTER-LEFT;");
        index03.setStyle("-fx-alignment: CENTER-LEFT;");
        index04.setStyle("-fx-alignment: CENTER-RIGHT;");
        index05.setStyle("-fx-alignment: CENTER;");
        index06.setStyle("-fx-alignment: CENTER;");
        index07.setStyle("-fx-alignment: CENTER-RIGHT;");
        index08.setStyle("-fx-alignment: CENTER-RIGHT;");
        index09.setStyle("-fx-alignment: CENTER-RIGHT;");
        
        index01.prefWidthProperty().bind(tblSalesMaster.widthProperty().multiply(0.04));
        index02.prefWidthProperty().bind(tblSalesMaster.widthProperty().multiply(0.16));
        index03.prefWidthProperty().bind(tblSalesMaster.widthProperty().multiply(0.25));
        index04.prefWidthProperty().bind(tblSalesMaster.widthProperty().multiply(0.11));
        index05.prefWidthProperty().bind(tblSalesMaster.widthProperty().multiply(0.08));
        index06.prefWidthProperty().bind(tblSalesMaster.widthProperty().multiply(0.08));
        index07.prefWidthProperty().bind(tblSalesMaster.widthProperty().multiply(0.08));
        index08.prefWidthProperty().bind(tblSalesMaster.widthProperty().multiply(0.08));
        index09.prefWidthProperty().bind(tblSalesMaster.widthProperty().multiply(0.11));
        
        index01.setCellValueFactory(new PropertyValueFactory<org.rmj.telecomfx.views.TableModel,String>("index01"));
        index02.setCellValueFactory(new PropertyValueFactory<org.rmj.telecomfx.views.TableModel,String>("index02"));
        index03.setCellValueFactory(new PropertyValueFactory<org.rmj.telecomfx.views.TableModel,String>("index03"));
        index04.setCellValueFactory(new PropertyValueFactory<org.rmj.telecomfx.views.TableModel,String>("index04"));
        index05.setCellValueFactory(new PropertyValueFactory<org.rmj.telecomfx.views.TableModel,String>("index05"));
        index06.setCellValueFactory(new PropertyValueFactory<org.rmj.telecomfx.views.TableModel,String>("index06"));
        index07.setCellValueFactory(new PropertyValueFactory<org.rmj.telecomfx.views.TableModel,String>("index07"));
        index08.setCellValueFactory(new PropertyValueFactory<org.rmj.telecomfx.views.TableModel,String>("index08"));
        index09.setCellValueFactory(new PropertyValueFactory<org.rmj.telecomfx.views.TableModel,String>("index09"));
        
        tblSalesMaster.setItems(data);
        data.clear();
    }

   @FXML
    private void tblSales_MasterClick(MouseEvent event) {
        if(tblSalesMaster.getItems().isEmpty()){return;}
        int lnRow = tblSalesMaster.getSelectionModel().getSelectedIndex();
        pnRow = lnRow;
        
        if (event.getClickCount() == 2){              
            boolean lbShow = pnEditMode == EditMode.ADDNEW || 
                                pnEditMode == EditMode.UPDATE;

            if (!lbShow) return;
            
            if (poTrans.getDetail(pnRow, "sStockIDx").equals("")) return;

            ModifySPDetailController spDetail = new ModifySPDetailController();
            spDetail.setGRider(poGRider);
            
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("child/modifySPDetail.fxml"));
            spDetail.setEditMode(EditMode.UPDATE);
            
            try {
                InvMaster loInv = new InvMaster(poGRider, psBranchCd, true);
                if (loInv.SearchStock((String) poTrans.getDetail(pnRow, "sStockIDx"), 
                        (String) poTrans.getDetail(pnRow, "sSerialID"), true, true)){
                    
                    spDetail.setStockIDx((String) loInv.getInventory("sStockIDx"));
                    spDetail.setBarCodex((String) loInv.getInventory("sBarCodex"));
                    spDetail.setDescript((String) loInv.getInventory("sDescript"));
                    spDetail.setQtyOnHnd((int) loInv.getMaster("nQtyOnHnd"));
                }
                
                spDetail.IsSerialized("0");
                spDetail.setUnitPrce(Double.valueOf(poTrans.getDetail(pnRow, "nUnitPrce").toString()));
                spDetail.setQuantity((int)poTrans.getDetail(pnRow, "nQuantity"));
                spDetail.setDiscRate(Double.valueOf(poTrans.getDetail(pnRow, "nDiscount").toString()));
                spDetail.setAddDiscx(Double.valueOf(poTrans.getDetail(pnRow, "nAddDiscx").toString()));

                if ("1".equals((String) loInv.getInventory("cSerialze"))){
                    spDetail.IsSerialized("1"); //tag the item as serialized
                    spDetail.setQtyOnHnd(1); //set serialized on hand to 1
                }
                
                fxmlLoader.setController(spDetail);
                fxmlLoader.load();

                Parent parent = fxmlLoader.getRoot();
                Scene scene = new Scene(parent);
                scene.setFill(new Color(0, 0, 0, 0));

                Stage nStage = new Stage();
                nStage.setScene(scene);
                nStage.initModality(Modality.APPLICATION_MODAL);
                nStage.initStyle(StageStyle.TRANSPARENT);
                nStage.showAndWait();

                if (!spDetail.isCancelled() && spDetail.getEditMode()== EditMode.UPDATE){
                        poTrans.setDetail(pnRow, "nUnitPrce", spDetail.getUnitPrce());
                        poTrans.setDetail(pnRow, "nQuantity", spDetail.getQuantity());
                        poTrans.setDetail(pnRow, "nDiscount", spDetail.getDiscRate());
                        poTrans.setDetail(pnRow, "nAddDiscx", spDetail.getAddDiscx());
                        
                        if (spDetail.getQuantity() > 0 && pnRow + 1 == poTrans.getDetailCount())
                            poTrans.addDetail();
                        
                        loadDetail2Grid();
                }
            } catch (IOException | SQLException e) {
                ShowMessageFX.Error(e.getMessage(), pxeModuleName, "Please inform MIS department.");
                System.exit(1);
            }
        }
    }
    
    final ChangeListener<? super Boolean> txtArea_Focus = (o,ov,nv)->{
      if (!pbLoaded) return;
        
        TextArea txtField = (TextArea)((ReadOnlyBooleanPropertyBase)o).getBean();
        int lnIndex = Integer.parseInt(txtField.getId().substring(8, 10));
        String lsValue = txtField.getText();
        
        if (lsValue == null) return;
        if(!nv){ /*Lost Focus*/            
            switch (lnIndex){
                case 11: /*sRemarksx*/
                    if (lsValue.length() > 256) lsValue = lsValue.substring(0, 256);
                    
                    poTrans.setMaster("sRemarksx", CommonUtils.TitleCase(lsValue));
                    txtField.setText((String)poTrans.getMaster("sRemarksx"));
            }
        }else{ 
            pnIndex = -1;
            txtField.selectAll();
        }
    };

    public void initButton(int fnValue) {
        boolean lbShow = (fnValue == EditMode.ADDNEW || fnValue == EditMode.UPDATE);
        
        btnBrowse.setVisible(!lbShow);
        btnNew.setVisible(!lbShow);
        btnUpdate.setVisible(!lbShow);
        btnPay.setVisible(!lbShow);
        btnVoid.setVisible(!lbShow);
        
        acFilter.setDisable(lbShow);
        acItem.setDisable(!lbShow);
        acMasterField.setDisable(!lbShow);
        acMasterLable.setDisable(!lbShow);
    }

    @FXML
    private void tblOrders_Click(MouseEvent event) {
    if(tblOrders.getItems().isEmpty()){return;}
       pos = (TablePosition) tblOrders.getSelectionModel().getSelectedCells().get(0);
        int row = pos.getRow();
        
        String lsValue = (String) order02.getCellObservableValue(row).getValue();

        if (poTrans.SearchTransaction(lsValue, true)){
            loadTransaction();
            pnEditMode = poTrans.getEditMode();
        }
    }
    
    private void getTime(){
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {            
        Calendar cal = Calendar.getInstance();
        int second = cal.get(Calendar.SECOND);
        
        Date date;
        date = poGRider.getServerDate();
        
        String fmtHourMin = "h:mm";
        String fmtSec = "ss a";
        String fmtCurrentDay = "EEEE";
        
        DateFormat dftHourMin = new SimpleDateFormat(fmtHourMin);
        DateFormat dftCurrentDay = new SimpleDateFormat(fmtCurrentDay);
        DateFormat dftSec = new SimpleDateFormat(fmtSec);
        
        String formattedTime= dftHourMin.format(date);
        String formattedDay = dftCurrentDay.format(date);
        String formattedSec = dftSec.format(date);
        
        lblTime.setText(formattedTime);
        lblDay.setText(formattedDay);
        lblSeconds.setText(formattedSec);
        lblDate.setText(CommonUtils.xsDateLong(date));
        
    }),
         new KeyFrame(Duration.seconds(1))
    );
        
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
        
    }
}
