package org.rmj.telecomfx.views.child;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.rmj.appdriver.agentfx.ShowMessageFX;

public class CancelledInvoiceController implements Initializable {

    @FXML private AnchorPane dataPane;
    @FXML private Button btnExit;
    @FXML private FontAwesomeIconView glyphExit;
    @FXML private Button btnOk;
    @FXML private Button btnCancel;
    @FXML private TextField txtField00;
    
    private boolean bCancelled = false;
    private String psInvoiceNo = "";
    
    public String getInvoiceNo(){
        return psInvoiceNo;
    }
    
    public boolean isCancelled(){return bCancelled;}
    private final String pxeModuleName = CancelledInvoiceController.class.getName();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnOk.setOnAction(this::cmdButton_Click);
        btnCancel.setOnAction(this::cmdButton_Click);
    }

     public void cmdButton_Click(ActionEvent event) {
        String lsButton = ((Button)event.getSource()).getId();
        switch (lsButton){
            case "btnOk":
                if (isEntryOk() ==false){
                    ShowMessageFX.Warning("Invalid SI/OR Number detected", pxeModuleName, "Please inform MIS/SEG");
                }else{
                    bCancelled = false;
                    unloadScene(event);
                }
                break;
            case "btnCancel":
                bCancelled = true;
                unloadScene(event);
        }
    }
     
    private boolean isEntryOk(){    
       psInvoiceNo =txtField00.getText().toLowerCase();
       try{
           Integer.parseInt(psInvoiceNo);
           return true;
       }catch(NumberFormatException ex){
          System.err.println(ex);
      }    
    return false;
    }
     
     private void unloadScene(ActionEvent event){
        Node source = (Node)  event.getSource(); 
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
    
}
