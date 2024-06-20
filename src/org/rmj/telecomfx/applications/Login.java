package org.rmj.telecomfx.applications;

import org.rmj.telecomfx.views.TelecomFX;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import org.rmj.appdriver.GProperty;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.CommonUtils;
import org.rmj.appdriver.constants.CRMEvent;
import org.rmj.sales.agentfx.XMDailySales;

public class Login {
    public static void main(String [] args){
        if (!loadProperties()){
            System.err.println("Unable to load config.");
            System.exit(1);
        } else System.out.println("Config file loaded successfully.");
        
        String lsProdctID;
        String lsUserIDxx;
        
        if (System.getProperty("app.debug.mode").equals("0")){
            if(args.length != 2){
                System.err.println("Invalid credential parameter.");
                System.exit(1);
            }
            
            lsProdctID = args[0];
            lsUserIDxx = args[1];
        } else {
            lsProdctID = System.getProperty("app.product.id");
            lsUserIDxx = System.getProperty("user.id");
        }
        
        String path;
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            path = "C:/GGC_Java_Systems";
        }
        else{
            path = "/srv/GGC_Java_Systems";
        }
        System.setProperty("sys.default.path.config", path);
        
        System.out.println(lsProdctID);
        GRider poGRider = new GRider(lsProdctID);
        GProperty loProp = new GProperty("GhostRiderXP");

        if (!poGRider.loadEnv(lsProdctID)) {
            System.err.println(poGRider.getErrMsg());
            System.exit(1);
        }
        
        if (!poGRider.logUser(lsProdctID, lsUserIDxx)) {
            System.err.println(poGRider.getErrMsg());
            System.exit(1);
        } 
        
        if (!initMachine(poGRider)) System.exit(1);
        
        CommonUtils.createEventLog(poGRider, poGRider.getBranchCode() + System.getProperty("pos.clt.trmnl.no"),  CRMEvent.CASHIER_LOGIN, "", System.getProperty("pos.clt.crm.no"));

        TelecomFX instance = new TelecomFX();
        instance.setGRider(poGRider);

        Application.launch(instance.getClass());
    }
    
    private static boolean loadProperties(){
        try {
            Properties po_props = new Properties();
            po_props.load(new FileInputStream("C:\\GGC_Java_Systems\\config\\rmj.properties"));
            
            System.setProperty("app.debug.mode", po_props.getProperty("app.debug.mode"));
            System.setProperty("app.product.id", po_props.getProperty("app.product.id"));
            System.setProperty("user.id", po_props.getProperty("user.id"));
            
            if (System.getProperty("app.product.id").equalsIgnoreCase("integsys")){
                System.setProperty("pos.clt.nm", po_props.getProperty("pos.clt.nm.integsys"));              
            } else{
                System.setProperty("pos.clt.nm", po_props.getProperty("pos.clt.nm.telecom"));         
            }
            
            System.setProperty("pos.clt.tin", po_props.getProperty("pos.clt.tin"));       
            System.setProperty("pos.clt.crm.no", po_props.getProperty("pos.clt.crm.no"));        
            System.setProperty("pos.clt.dir.ejournal", po_props.getProperty("pos.clt.dir.ejournal"));        
            
            //store info
            System.setProperty("store.inventory.type", po_props.getProperty("store.inventory.type"));
            System.setProperty("store.inventory.strict.type", po_props.getProperty("store.inventory.strict.type"));
            
            System.setProperty("pos.backend.sys.journal", po_props.getProperty("pos.backend.sys.journal"));
            System.setProperty("pos.backend.sys.user", po_props.getProperty("pos.backend.sys.user"));
            System.setProperty("pos.backend.sys.pass", po_props.getProperty("pos.backend.sys.pass"));
            
            //Footer
            System.setProperty("pos.footer.sDevelopr", po_props.getProperty("pos.footer.sDevelopr"));
            System.setProperty("pos.footer.sAddress1", po_props.getProperty("pos.footer.sAddress1"));
            System.setProperty("pos.footer.sAddress2", po_props.getProperty("pos.footer.sAddress2"));
            System.setProperty("pos.footer.sVATREGTN", po_props.getProperty("pos.footer.sVATREGTN"));
            System.setProperty("pos.footer.sAccrNmbr", po_props.getProperty("pos.footer.sAccrNmbr"));
            System.setProperty("pos.footer.sAccrIssd", po_props.getProperty("pos.footer.sAccrIssd"));
            System.setProperty("pos.footer.sAccdExpr", po_props.getProperty("pos.footer.sAccdExpr"));
            System.setProperty("pos.footer.sPTUNmber", po_props.getProperty("pos.footer.sPTUNmber"));
//            System.setProperty("pos.footer.sPTUIssdx", po_props.getProperty("pos.footer.sPTUIssdx"));
//            System.setProperty("pos.footer.sPTUExpry", po_props.getProperty("pos.footer.sPTUExpry"));
            
            return true;
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }  
    
    public static boolean initMachine(GRider foApp){
        try {
            if (System.getProperty("pos.clt.crm.no").isEmpty()) {
                System.err.println("Invalid machine identification number detected.");
                return false;
            }
            
            String lsSQL = "SELECT" +
                                "  a.sAccredtn" +
                                ", a.sPermitNo" +
                                ", a.sSerialNo" +
                                ", a.nPOSNumbr" +
                                ", a.nZReadCtr" +
                                ", a.cTranMode" +
                                ", IFNULL(b.sWebSrver, '') sWebSrver" +
                                ", IFNULL(b.sPrinter1, '') sPrinter1" +
                            " FROM Cash_Reg_Machine a" +
                                " LEFT JOIN Cash_Reg_Machine_Printer b" +
                                    " ON a.sIDNumber = b.sIDNumber" +
                            " WHERE a.sIDNumber = " + SQLUtil.toSQL(System.getProperty("pos.clt.crm.no"));
            
            ResultSet loRS = foApp.executeQuery(lsSQL);
            long lnRow = MiscUtil.RecordCount(loRS);
            
            if (lnRow != 1){
                System.err.println("Invalid Config for MIN Detected.");
                return false;
            }
            
            loRS.first();
            
            System.setProperty("pos.clt.accrd.no", loRS.getString("sAccredtn"));
            System.setProperty("pos.clt.prmit.no", loRS.getString("sPermitNo"));
            System.setProperty("pos.clt.srial.no", loRS.getString("sSerialNo"));
            System.setProperty("pos.clt.trmnl.no", loRS.getString("nPOSNumbr"));
            System.setProperty("pos.clt.zcounter", String.valueOf(loRS.getInt("nZReadCtr")));

            System.setProperty("pos.clt.web.svrx", loRS.getString("sWebSrver"));
            System.setProperty("pos.clt.prntr.01", loRS.getString("sPrinter1"));
                                                    
            System.setProperty("pos.clt.tran.mode", loRS.getString("cTranMode"));
            
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(XMDailySales.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println(ex.getMessage());
        }
        return false;
    }
}
