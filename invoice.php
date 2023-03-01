<?php
require_once 'rawprint.php';
require __DIR__ . '/../vendor/autoload.php';
use Mike42\Escpos\Printer;
use Mike42\Escpos\PrintConnectors\WindowsPrintConnector;

$json = array();

$data = file_get_contents('php://input');
$parjson = json_decode($data, true);

if(is_null($parjson)){
    $json["result"] = "error";
    $json["error"]["message"] = "Invalid parameters detected...";
    echo json_encode($json);
    return false;
}

$jsonHeader = null;
$jsonMaster = null;
$jsonDetail = null;
$jsonFooter = null;
$jsonPaymnt = null;

foreach ($parjson as $key => $value){
    switch (strtolower($key)){
        case "header":
            $jsonHeader = $value; break;
        case "master";
            $jsonMaster = $value; break;
        case "detail";
            $jsonDetail = $value; break;
        case "payment":
            $jsonPaymnt = $value; break;
        case "footer";
            $jsonFooter = $value;
    }
}

if ($jsonHeader == null){
    $json["result"] = "error";
    $json["error"]["message"] = "Header parameter is invalid.";
    echo json_encode($json);
    return false;
}

if ($jsonFooter == null){
    $json["result"] = "error";
    $json["error"]["message"] = "Footer parameter is invalid.";
    echo json_encode($json);
    return false;
}

if ($jsonMaster == null){
    $json["result"] = "error";
    $json["error"]["message"] = "Master parameter is invalid.";
    echo json_encode($json);
    return false;
}

if ($jsonDetail == null){
    $json["result"] = "error";
    $json["error"]["message"] = "Detail parameter is invalid.";
    echo json_encode($json);
    return false;
}

if ($jsonPaymnt == null){
    $json["result"] = "error";
    $json["error"]["message"] = "Payment parameter is invalid.";
    echo json_encode($json);
    return false;
}

if ((strtolower($jsonHeader["sSlipType"]) == "or" || 
    strtolower($jsonHeader["sSlipType"]) == "si") == false){
    $json["result"] = "error";
    $json["error"]["message"] = "Invoice type is not suitable for this API.";
    echo json_encode($json);
    return false;
}

if(!isset($parjson['printer'])){
    $json["result"] = "error";
    $json["error"]["message"] = "Unset PRINTER ADDRESS detected..";
    echo json_encode($json);
    return;
}
$printer_name = $parjson['printer'];

$connector = new WindowsPrintConnector("smb://" . $printer_name);
$printer = new Printer($connector);

/* Initialize */
$printer -> initialize();

//default text formatting (REGULAR)
$printer -> setFont(Printer::FONT_C);
$printer -> selectPrintMode($printer::MODE_FONT_B);
$printer -> setEmphasis(false);

//print header
printHeader($printer, $jsonHeader);

//print detail
printDetail($printer, $jsonMaster, $jsonDetail, $jsonPaymnt, $jsonHeader["sSlipType"], $jsonHeader["cReprintx"], $jsonHeader["cTranMode"]);

//print footer
printFooter($printer, $jsonFooter, $jsonHeader["sSlipType"], $jsonHeader["cReprintx"], $jsonHeader["cTranMode"]);

/* Cut */
$printer -> feed();
$printer -> cut();

/* Pulse */
$printer -> pulse();

/* Always close the printer! On some PrintConnectors, no actual
 * data is sent until the printer is closed. */
$printer -> close();

$json["result"] = "success";
echo json_encode($json);
return true;

/**
 * PRINTS HEADER OF THE RECEIPT
 *
 * printHeader($printer, $json)
 *      $printer - printer object
 *      $json - json object for header details
 */
function printHeader($printer, $json){
    $printer -> setEmphasis(true);
    $printer -> text(str_pad($json["sBranchNm"], PrintCharSize::REGULAR, " ", STR_PAD_BOTH));
    $printer -> text(PrintStr::NEWLINE);
    
    $printer -> text(str_pad($json["sCompnyNm"], PrintCharSize::REGULAR, " ", STR_PAD_BOTH));
    $printer -> text(PrintStr::NEWLINE);
    
    $printer -> text(str_pad($json["sAddress1"], PrintCharSize::REGULAR, " ", STR_PAD_BOTH));
    $printer -> text(PrintStr::NEWLINE);
    
    $printer -> text(str_pad($json["sAddress2"], PrintCharSize::REGULAR, " ", STR_PAD_BOTH));
    $printer -> text(PrintStr::NEWLINE);
    
    $printer -> text(str_pad("VAT REG TIN: " . $json["sVATREGTN"], PrintCharSize::REGULAR, " ", STR_PAD_BOTH));
    $printer -> text(PrintStr::NEWLINE);
    
    $printer -> text(str_pad("MIN: " . $json["sMINumber"], PrintCharSize::REGULAR, " ", STR_PAD_BOTH));
    $printer -> text(PrintStr::NEWLINE);
    
    $printer -> text(str_pad("Serial No.: " . $json["sSerialNo"], PrintCharSize::REGULAR, " ", STR_PAD_BOTH));
    $printer -> text(PrintStr::NEWLINE . PrintStr::NEWLINE);
}

/**
 * PRINTS DETAIL OF THE RECEIPT
 *
 * printDetail($printer, $json)
 *      $printer - printer object
 *      $master - json object for master info
 *      $detail - json object for info info
 */
function printDetail($printer, $master, $detail, $payment, $sliptype, $reprint, $tranmode){
    $total = 0.00;
    $nonvat = 0.00;
    $itemctr = 0;
    $nonvatctr = 0;
    
    $trantotl = $master["nTranTotl"];
    $discrate = $master["nDiscount"];
    $adddiscx = $master["nAddDiscx"];
    $vatratex = $master["nVATRatex"];
    $nfreight = $master["nFreightx"];
    
    //add freight charge to the transaction total
    $trantotl += $nfreight;    
    
    $printer -> selectPrintMode($printer::MODE_DOUBLE_HEIGHT);
    
    if (strtolower($tranmode) == "a"){
        if (strtolower($sliptype) == "or")
            $printer -> text(str_pad("OFFICIAL RECEIPT", PrintCharSize::DDHGHT, " ", STR_PAD_BOTH));
        else
            $printer -> text(str_pad("SALES INVOICE", PrintCharSize::DDHGHT, " ", STR_PAD_BOTH));
    } else 
        $printer -> text(str_pad("TRAINING MODE", PrintCharSize::DDHGHT, " ", STR_PAD_BOTH));
    
    $printer -> selectPrintMode($printer::MODE_FONT_B);
    
    if ($reprint){
        $printer -> text(PrintStr::NEWLINE);
        $printer -> setEmphasis(true);
        $printer -> text(str_pad("REPRINT", PrintCharSize::REGULAR, " ", STR_PAD_BOTH));
        $printer -> setEmphasis(false);
    }
    
    $printer -> text(PrintStr::NEWLINE . PrintStr::NEWLINE);
    
    $s4Print = " Cashier: " . str_pad($master["sCashierx"], 30, " ", STR_PAD_RIGHT); //30
    $printer -> text($s4Print);
    $printer -> text(PrintStr::NEWLINE);
    
    
    $s4Print = " Terminal No.: " . str_pad($master["sTerminal"], 25, " ", STR_PAD_RIGHT); //25
    $printer -> text($s4Print);
    $printer -> text(PrintStr::NEWLINE);
    
    
    if (strtolower($sliptype) == "or"){
        $s4Print = " Official Receipt No.: " . str_pad($master["sInvoicex"], 17, " ", STR_PAD_RIGHT); //31
    } else {
        $s4Print = " Sales Invoice No.: " . str_pad($master["sInvoicex"], 20, " ", STR_PAD_RIGHT); //31
    }
    
    $printer -> text($s4Print);
    $printer -> text(PrintStr::NEWLINE);
    
    $s4Print = " Transaction No.: " . str_pad($master["sTransNox"], 22, " ", STR_PAD_RIGHT); //25
    $printer -> text($s4Print);
    $printer -> text(PrintStr::NEWLINE);
    
    $s4Print = " Date/Time: " . str_pad($master["sDateTime"], 28, " ", STR_PAD_RIGHT); //28
    $printer -> text($s4Print);
    $printer -> text(PrintStr::NEWLINE);
    
    $printer -> text(PrintStr::NEWLINE);
    $printer -> text(str_pad(PrintStr::ASTRSKX, PrintCharSize::REGULAR, PrintStr::ASTRSKX, STR_PAD_RIGHT));
    $printer -> text(PrintStr::NEWLINE);
    
    $s4Print = "";
    $printer -> setEmphasis(false);
    //Detail Header
    $s4Print = "QTY" . " "
                . str_pad("DESCRIPTION", PrintCharSize::DSCLEN, " ", STR_PAD_RIGHT) . " "
                . str_pad("UPRICE", PrintCharSize::PRCLEN, " ", STR_PAD_LEFT) . " "
                . str_pad("AMOUNT", PrintCharSize::TTLLEN, " ", STR_PAD_LEFT);
    $printer -> text($s4Print);
    $printer -> text(PrintStr::NEWLINE);

    //Loop Detail Here:
    foreach($detail as $item){                
        $total = $item["nQuantity"] * ($item["nAmountxx"]);
        
        $s4Print = str_pad($item["nQuantity"], PrintCharSize::QTYLEN, " ", STR_PAD_LEFT) . " "
                    . str_pad(strtoupper($item["sBarCodex"]), PrintCharSize::DSCLEN, " ", STR_PAD_RIGHT) . " "
                    . str_pad(PrintStr::format_number($item["nAmountxx"], false), PrintCharSize::PRCLEN, " ", STR_PAD_LEFT) . " "
                    . str_pad(PrintStr::format_number($total, false), PrintCharSize::TTLLEN, " ", STR_PAD_LEFT);
        
        //is the item vatable???
        if ($item["cVatablex"] == "1")
            $s4Print = $s4Print . "V";
        else {
            $nonvatctr += $item["nQuantity"];
            $nonvat += $total;
        }
        
        //print detail info
        $printer -> text($s4Print);
        $printer -> text(PrintStr::NEWLINE);
        
        //print item description
        $printer -> text("    " . strtoupper($item["sDescript"]));
        $printer -> text(PrintStr::NEWLINE);
        
        //print serial if item was serialized
        if ($item["cSerialze"] == "1"){
            if ($item["xDescript"] != null && $item["xDescript"] != ""){
                $printer -> text(str_pad("     " . strtoupper($item["xDescript"]), PrintCharSize::REGULAR, " ", STR_PAD_RIGHT));
                $printer -> text(PrintStr::NEWLINE);
            }
            
            if ($item["sSerial01"] != null && $item["sSerial01"] != ""){
                $printer -> text(str_pad("     " . "SN: " . strtoupper($item["sSerial01"]), PrintCharSize::REGULAR, " ", STR_PAD_RIGHT));
                $printer -> text(PrintStr::NEWLINE);
            }            
            
            if ($item["sSerial02"] != null && $item["sSerial02"] != ""){
                $printer -> text(str_pad("     " . "SN: " . strtoupper($item["sSerial02"]), PrintCharSize::REGULAR, " ", STR_PAD_RIGHT));
                $printer -> text(PrintStr::NEWLINE);
            }
        }
        
        $detdisc = $total * $item["nDiscount"] + $item["nAddDiscx"];
        
        if ($detdisc > 0.00){
            if ($item["sPromoDsc"] != null && $item["sPromoDsc"] != ""){
                $printer -> text(str_pad("      " . strtoupper($item["sPromoDsc"]), PrintCharSize::REGULAR, " ", STR_PAD_RIGHT));
                $printer -> text(PrintStr::NEWLINE);
            } 
            
            if ($item["nDiscount"] > 0.00){
                $s4Print = str_pad("       " . PrintStr::format_number($item["nDiscount"] * 100, false) . "%", "26", " ", STR_PAD_RIGHT) . " " . str_pad("(". PrintStr::format_number($total * $item["nDiscount"], false) . ")", PrintCharSize::REGLEN, " ", STR_PAD_LEFT);
                $printer -> text($s4Print);
                $printer -> text(PrintStr::NEWLINE);
            }
            
            if ($item["nAddDiscx"] > 0.00){
                $s4Print = str_pad("       " . PrintStr::format_number($item["nAddDiscx"], false), "26", " ", STR_PAD_RIGHT) . " " . str_pad("(". PrintStr::format_number($item["nAddDiscx"], false) . ")", PrintCharSize::REGLEN, " ", STR_PAD_LEFT);
                $printer -> text($s4Print);
                $printer -> text(PrintStr::NEWLINE);
            }
        }
        
        $itemctr += $item["nQuantity"];
    }
    
    if ($nfreight > 0.00){        
        $s4Print = str_pad(" ", PrintCharSize::QTYLEN, " ", STR_PAD_LEFT) . " "
            . str_pad("FREIGHT CHARGE", PrintCharSize::DSCLEN, " ", STR_PAD_RIGHT) . " "
                . str_pad(PrintStr::format_number($nfreight, false), PrintCharSize::PRCLEN, " ", STR_PAD_LEFT) . " "
                    . str_pad(PrintStr::format_number($nfreight, false), PrintCharSize::TTLLEN, " ", STR_PAD_LEFT) . "V";
        $printer -> text(PrintStr::NEWLINE . $s4Print);
        $printer -> text(PrintStr::NEWLINE);
    }
    
    $printer -> text(str_pad(PrintStr::DASHLNE, PrintCharSize::REGULAR, PrintStr::DASHLNE, STR_PAD_RIGHT));
    $printer -> text(PrintStr::NEWLINE);
    
    $printer -> text(" No. of Items: " . $itemctr);
    $printer -> text(PrintStr::NEWLINE . PrintStr::NEWLINE);
    
    //SUB TOTAL
    //$s4Print = str_pad(" Sub-Total", "25", " ", STR_PAD_RIGHT) . " " . str_pad(PrintStr::format_number($trantotl, false), PrintCharSize::REGLEN, " ", STR_PAD_LEFT);
    $s4Print = str_pad(" Gross Sales", "25", " ", STR_PAD_RIGHT) . " " . str_pad(PrintStr::format_number($trantotl, false), PrintCharSize::REGLEN, " ", STR_PAD_LEFT);
    $printer -> text($s4Print);
    $printer -> text(PrintStr::NEWLINE);
    
    $discount = 0.00;
    
    if ($discrate + $adddiscx > 0) $discount = $trantotl * $discrate + $adddiscx; 
    
    //DISCOUNT
    if ($discount > 0) {        
        //$s4Print = str_pad(" Less: Discount", "25", " ", STR_PAD_RIGHT) . " " . str_pad(PrintStr::format_number($discount, false), PrintCharSize::REGLEN, " ", STR_PAD_LEFT);
        //$printer -> text($s4Print);
        //$printer -> text(PrintStr::NEWLINE);
        
        if ($master["sPromoDsc"] != null && $master["sPromoDsc"] != ""){
            $printer -> text(str_pad(" Less: " . strtoupper($master["sPromoDsc"]), PrintCharSize::REGULAR, " ", STR_PAD_RIGHT));
            $printer -> text(PrintStr::NEWLINE);
        }
        
        if ($discrate > 0.00){
            $s4Print = str_pad("        (" . PrintStr::format_number($discrate * 100, false) . "%)", "25", " ", STR_PAD_RIGHT) . " " . str_pad(PrintStr::format_number($trantotl * $discrate, false), PrintCharSize::REGLEN, " ", STR_PAD_LEFT);
            $printer -> text($s4Print);
            $printer -> text(PrintStr::NEWLINE);
        }
        
        if ($adddiscx > 0.00){
            $s4Print = str_pad("        (P" . PrintStr::format_number($adddiscx, false) . ")", "25", " ", STR_PAD_RIGHT) . " " . str_pad(PrintStr::format_number($adddiscx, false), PrintCharSize::REGLEN, " ", STR_PAD_LEFT);
            $printer -> text($s4Print);
            $printer -> text(PrintStr::NEWLINE);
        }
    }
    
    //small separator
    $s4Print = str_pad(" ", "25", " ", STR_PAD_RIGHT) . " " . str_pad(PrintStr::DASHLNE, PrintCharSize::REGLEN, PrintStr::DASHLNE, STR_PAD_LEFT);
    $printer -> text($s4Print);
    $printer -> text(PrintStr::NEWLINE);
    
    $s4Print = str_pad(" VATable Sales", "25", " ", STR_PAD_RIGHT) . " " . str_pad(PrintStr::format_number($trantotl - $discount, false), PrintCharSize::REGLEN, " ", STR_PAD_LEFT);
    $printer -> text($s4Print);
    $printer -> text(PrintStr::NEWLINE);
    
    //small separator
    $s4Print = str_pad(" ", "25", " ", STR_PAD_RIGHT) . " " . str_pad(PrintStr::DASHLNE, PrintCharSize::REGLEN, PrintStr::DASHLNE, STR_PAD_LEFT);
    $printer -> text($s4Print);
    $printer -> text(PrintStr::NEWLINE);
    
    $netWOVAT = ($trantotl - $discount) / $vatratex;
    $s4Print = str_pad(" Net VATable Sales", "25", " ", STR_PAD_RIGHT) . " " . str_pad(PrintStr::format_number($netWOVAT, false), PrintCharSize::REGLEN, " ", STR_PAD_LEFT);
    $printer -> text($s4Print);
    $printer -> text(PrintStr::NEWLINE);
    
    $addVATxx = $netWOVAT * ($vatratex - 1);
    $s4Print = str_pad(" Add: VAT", "25", " ", STR_PAD_RIGHT) . " " . str_pad(PrintStr::format_number($addVATxx, false), PrintCharSize::REGLEN, " ", STR_PAD_LEFT);
    $printer -> text($s4Print);
    $printer -> text(PrintStr::NEWLINE);
    
    //small separator
    $s4Print = str_pad(" ", "25", " ", STR_PAD_RIGHT) . " " . str_pad(PrintStr::DASHLNE, PrintCharSize::REGLEN, PrintStr::DASHLNE, STR_PAD_LEFT);
    $printer -> text($s4Print);
    $printer -> text(PrintStr::NEWLINE);
    
    $printer -> setEmphasis(true);
    $nettotal = $netWOVAT + $addVATxx;
    $s4Print = str_pad(" TOTAL AMOUNT DUE :", "25", " ", STR_PAD_RIGHT) . " " . str_pad(PrintStr::format_number($nettotal, false), PrintCharSize::REGLEN, " ", STR_PAD_LEFT);
    $printer -> text($s4Print);
    $printer -> text(PrintStr::NEWLINE . PrintStr::NEWLINE);
    
    //PAYMENT COMPUTATION
    $cash = $payment["nCashAmtx"];
    
    $card = 0.00;
    $jsonCard = $payment["sCredtCrd"];
    foreach($jsonCard as $item){
        $card += $item["nAmountxx"];
    }
    
    $check = 0.00;
    $jsonCheck = $payment["sCheckPay"];
    foreach($jsonCheck as $item){
        $check += $item["nAmountxx"];
    }
    
    $gc= 0.00;
    $jsonGC = $payment["sGiftCert"];
    foreach($jsonGC as $item){
        $gc += $item["nAmountxx"];
    }
    
    $financer= 0.00;
    $jsonFinancer = $payment["sFinancer"];
    foreach($jsonFinancer as $item){
        $financer += $item["nAmountxx"];
    }
    
    $printer -> setEmphasis(false);

    if ($cash > 0.00){
        $s4Print = str_pad(" Cash", "25", " ", STR_PAD_RIGHT) . " " . str_pad(PrintStr::format_number($cash, false), PrintCharSize::REGLEN, " ", STR_PAD_LEFT);
        $printer -> text($s4Print);
        $printer -> text(PrintStr::NEWLINE);
    }
    
    if ($card > 0.00){
        $s4Print = str_pad(" Credit Card", "25", " ", STR_PAD_RIGHT) . " " . str_pad(PrintStr::format_number($card, false), PrintCharSize::REGLEN, " ", STR_PAD_LEFT);
        $printer -> text($s4Print);
        $printer -> text(PrintStr::NEWLINE);
    }
    
    if ($check > 0.00){
        $s4Print = str_pad(" Check", "25", " ", STR_PAD_RIGHT) . " " . str_pad(PrintStr::format_number($check, false), PrintCharSize::REGLEN, " ", STR_PAD_LEFT);
        $printer -> text($s4Print);
        $printer -> text(PrintStr::NEWLINE);
    }
    
    if ($gc > 0.00){
        $s4Print = str_pad(" Gift Certificate", "25", " ", STR_PAD_RIGHT) . " " . str_pad(PrintStr::format_number($gc, false), PrintCharSize::REGLEN, " ", STR_PAD_LEFT);
        $printer -> text($s4Print);
        $printer -> text(PrintStr::NEWLINE);
    }
    
    if ($financer > 0.00){
        $s4Print = str_pad(" Other", "25", " ", STR_PAD_RIGHT) . " " . str_pad(PrintStr::format_number($financer, false), PrintCharSize::REGLEN, " ", STR_PAD_LEFT);
        $printer -> text($s4Print);
        $printer -> text(PrintStr::NEWLINE);
    }
    
    $printer -> text(PrintStr::NEWLINE);
    
    
    //CHANGE COMPUTATION
    //use GC Amount First
    $change = 0.00;
    if ($gc > $nettotal){
        $change = $cash + $card + $check + $financer;           
    } else {
        $change = ($gc +$cash + $card + $check + $financer) - $nettotal;
    }
    
    $s4Print = str_pad(" CHANGE           :", "25", " ", STR_PAD_RIGHT) . " " . str_pad(PrintStr::format_number($change, false), PrintCharSize::REGLEN, " ", STR_PAD_LEFT);
    $printer -> text($s4Print);
    $printer -> text(PrintStr::NEWLINE);
    
    $printer -> text(str_pad(PrintStr::DASHLNE, PrintCharSize::REGULAR, PrintStr::DASHLNE, STR_PAD_RIGHT));
    $printer -> text(PrintStr::NEWLINE . PrintStr::NEWLINE);
    
    $s4Print = str_pad(" VAT Exempt Sales      ", "25", " ", STR_PAD_RIGHT) . " " . str_pad(PrintStr::format_number($nonvat, false), PrintCharSize::REGLEN, " ", STR_PAD_LEFT);
    $printer -> text($s4Print);
    $printer -> text(PrintStr::NEWLINE);
    
    $s4Print = str_pad(" Zero-Rated Sales      ", "25", " ", STR_PAD_RIGHT) . " " . str_pad(PrintStr::format_number("0.00", false), PrintCharSize::REGLEN, " ", STR_PAD_LEFT);
    $printer -> text($s4Print);
    $printer -> text(PrintStr::NEWLINE);
    
    $s4Print = str_pad(" VATable Sales         ", "25", " ", STR_PAD_RIGHT) . " " . str_pad(PrintStr::format_number($netWOVAT, false), PrintCharSize::REGLEN, " ", STR_PAD_LEFT);
    $printer -> text($s4Print);
    $printer -> text(PrintStr::NEWLINE);
    
    $s4Print = str_pad(" VAT Amount            ", "25", " ", STR_PAD_RIGHT) . " " . str_pad(PrintStr::format_number($addVATxx, false), PrintCharSize::REGLEN, " ", STR_PAD_LEFT);
    $printer -> text($s4Print);
    $printer -> text(PrintStr::NEWLINE . PrintStr::NEWLINE);
    
    $s4Print = str_pad(" Cust Name: ". $master["sClientNm"], 40, " ", STR_PAD_RIGHT); //28
    $printer -> text($s4Print);
    $printer -> text(PrintStr::NEWLINE);
    
    $s4Print = " Address: ". str_pad($master["sAddressx"], 30, " ", STR_PAD_RIGHT); //30
    $printer -> text($s4Print);
    $printer -> text(PrintStr::NEWLINE);
    
    $s4Print = " TIN: ". str_pad($master["sTINumber"], 34, " ", STR_PAD_RIGHT); //34
    $printer -> text($s4Print);
    $printer -> text(PrintStr::NEWLINE);
    
    $s4Print = " Bus. Style: ". str_pad($master["sBusStyle"], 27, " ", STR_PAD_RIGHT); //27
    $printer -> text($s4Print);
    $printer -> text(PrintStr::NEWLINE);
    
    /*
    
    $s4Print = " Cashier: " . str_pad($master["sCashierx"], 30, " ", STR_PAD_RIGHT); //30
    $printer -> text($s4Print);
    $printer -> text(PrintStr::NEWLINE);
    
    $s4Print = " Terminal No.: " . str_pad($master["sTerminal"], 25, " ", STR_PAD_RIGHT); //25
    $printer -> text($s4Print);
    $printer -> text(PrintStr::NEWLINE);      
    
    
    if (strtolower($sliptype) == "or"){
        $s4Print = " OR No.: " . str_pad($master["sInvoicex"], 31, " ", STR_PAD_RIGHT); //31
    } else {
        $s4Print = " SI No.: " . str_pad($master["sInvoicex"], 31, " ", STR_PAD_RIGHT); //31
    }
    
    $printer -> text($s4Print);
    $printer -> text(PrintStr::NEWLINE);
    
    $s4Print = " Transaction No.: " . str_pad($master["sTransNox"], 22, " ", STR_PAD_RIGHT); //25
    $printer -> text($s4Print);
    $printer -> text(PrintStr::NEWLINE);
    
    $s4Print = " Date/Time: " . str_pad($master["sDateTime"], 28, " ", STR_PAD_RIGHT); //28
    $printer -> text($s4Print);
    $printer -> text(PrintStr::NEWLINE);
    
    */
    
    //if payment has card or check or gc or finance, display the details 
    if ($card + $check + $gc + $financer > 0.00){
        $printer -> text(str_pad(PrintStr::DASHLNE, PrintCharSize::REGULAR, PrintStr::DASHLNE, STR_PAD_RIGHT));
        $printer -> text(PrintStr::NEWLINE);
        
        if ($card > 0.00){
            $printer -> text(PrintStr::NEWLINE);
            
            $s4Print = " Credit Card(s): ";
            $printer -> text($s4Print);
            $printer -> text(PrintStr::NEWLINE);
            
            foreach($jsonCard as $item){
                $s4Print = str_pad(" " . $item["sBankCode"], "25", " ", STR_PAD_RIGHT) . " " . str_pad(PrintStr::format_number($item["nAmountxx"], false), PrintCharSize::REGLEN, " ", STR_PAD_LEFT);
                $printer -> text($s4Print);
                $printer -> text(PrintStr::NEWLINE);
                
                $s4Print = " Card No. : " . str_pad(substr($item["sCardNoxx"], strlen($item["sCardNoxx"]) - 4), PrintCharSize::CARDLN, "*", STR_PAD_LEFT);
                $printer -> text($s4Print);
                $printer -> text(PrintStr::NEWLINE);
            }
        }
        
        if ($check > 0.00){
            $printer -> text(PrintStr::NEWLINE);
            
            $s4Print = " Check(s): ";
            $printer -> text($s4Print);
            $printer -> text(PrintStr::NEWLINE);
            
            foreach($jsonCheck as $item){
                $s4Print = str_pad(" " . $item["sBankCode"], "25", " ", STR_PAD_RIGHT) . " " . str_pad(PrintStr::format_number($item["nAmountxx"], false), PrintCharSize::REGLEN, " ", STR_PAD_LEFT);
                $printer -> text($s4Print);
                $printer -> text(PrintStr::NEWLINE);
                
                $s4Print = " Check No. : " . $item["sCheckNox"];
                $printer -> text($s4Print);
                $printer -> text(PrintStr::NEWLINE);
            }
        }
        
        if ($gc > 0.00){
            $printer -> text(PrintStr::NEWLINE);
            
            $s4Print = " Gift Certificate(s): ";
            $printer -> text($s4Print);
            $printer -> text(PrintStr::NEWLINE);
            
            foreach($jsonGC as $item){
                $s4Print = str_pad(" " . $item["sCompnyCd"], "25", " ", STR_PAD_RIGHT) . " " . str_pad(PrintStr::format_number($item["nAmountxx"], false), PrintCharSize::REGLEN, " ", STR_PAD_LEFT);
                $printer -> text($s4Print);
                $printer -> text(PrintStr::NEWLINE);
                
                $s4Print = " Reference No. : " . $item["sReferNox"];
                $printer -> text($s4Print);
                $printer -> text(PrintStr::NEWLINE);
            }
        }
        
        if ($financer > 0.00){
            $printer -> text(PrintStr::NEWLINE);
            
            $s4Print = " Other(s): ";
            $printer -> text($s4Print);
            $printer -> text(PrintStr::NEWLINE);
            
            foreach($jsonFinancer as $item){
                $s4Print = str_pad(" " . $item["sCompnyCd"], "25", " ", STR_PAD_RIGHT) . " " . str_pad(PrintStr::format_number($item["nAmountxx"], false), PrintCharSize::REGLEN, " ", STR_PAD_LEFT);
                $printer -> text($s4Print);
                $printer -> text(PrintStr::NEWLINE);
                
                $s4Print = " Reference No. : " . $item["sReferNox"];
                $printer -> text($s4Print);
                $printer -> text(PrintStr::NEWLINE);
            }
        }       
    }
}

/**
 * PRINTS FOOTER OF THE RECEIPT
 * 
 * printFooter($printer, $json)
 *      $printer - printer object
 *      $json - json object for footer details
 *      $sliptype - slip type
 */
function printFooter($printer, $json, $sliptype, $reprint, $tranmode){
    //START : PRINT HEADER
    $printer -> setEmphasis(false);
    $printer -> text(str_pad(PrintStr::ASTRSKX, PrintCharSize::REGULAR, PrintStr::ASTRSKX, STR_PAD_RIGHT));
    $printer -> text(PrintStr::NEWLINE . PrintStr::NEWLINE);
    
    if (!$reprint){
        $printer -> setEmphasis(true);
        
        if (strtolower($tranmode) == "a"){
            if (strtolower($sliptype) == "or")
                $printer -> text(str_pad("This serves as an OFFICIAL RECEIPT.", PrintCharSize::REGULAR, " ", STR_PAD_BOTH));
            else
                $printer -> text(str_pad("This serves as your SALES INVOICE.", PrintCharSize::REGULAR, " ", STR_PAD_BOTH));
            
        } else
            $printer -> text(str_pad("This is not a SALES INVOICE.", PrintCharSize::REGULAR, " ", STR_PAD_BOTH));
        
        $printer -> setEmphasis(false);
        $printer -> text(PrintStr::NEWLINE);
    }    
    
    $printer -> text(str_pad("Thank you, and please come again.", PrintCharSize::REGULAR, " ", STR_PAD_BOTH));
    $printer -> text(PrintStr::NEWLINE . PrintStr::NEWLINE);
    
    $printer -> text(str_pad($json["sDevelopr"], PrintCharSize::REGULAR, " ", STR_PAD_BOTH));
    $printer -> text(PrintStr::NEWLINE);
    $printer -> text(str_pad($json["sAddress1"], PrintCharSize::REGULAR, " ", STR_PAD_BOTH));
    $printer -> text(PrintStr::NEWLINE);
    $printer -> text(str_pad($json["sAddress2"], PrintCharSize::REGULAR, " ", STR_PAD_BOTH));
    $printer -> text(PrintStr::NEWLINE);
    $printer -> text(str_pad("NON-VAT REG TIN: " . $json["sVATREGTN"], PrintCharSize::REGULAR, " ", STR_PAD_BOTH));
    $printer -> text(PrintStr::NEWLINE);
    $printer -> text(str_pad("ACCR NO.: " . $json["sAccrNmbr"], PrintCharSize::REGULAR, " ", STR_PAD_BOTH));
    $printer -> text(PrintStr::NEWLINE);
    $printer -> text(str_pad("Date Issued: " . $json["sAccrIssd"], PrintCharSize::REGULAR, " ", STR_PAD_BOTH));
    $printer -> text(PrintStr::NEWLINE);
    $printer -> text(str_pad("Valid Until: " . $json["sAccdExpr"], PrintCharSize::REGULAR, " ", STR_PAD_BOTH));
    $printer -> text(PrintStr::NEWLINE);
    $printer -> text(str_pad("PTU NO.: " . $json["sPTUNmber"], PrintCharSize::REGULAR, " ", STR_PAD_BOTH));
    //$printer -> text(PrintStr::NEWLINE);
    //$printer -> text(str_pad("Date Issued: " . $json["sPTUIssdx"], PrintCharSize::REGULAR, " ", STR_PAD_BOTH));
    //$printer -> text(PrintStr::NEWLINE);
    //$printer -> text(str_pad("Valid Until: " . $json["sPTUExpry"], PrintCharSize::REGULAR, " ", STR_PAD_BOTH));
    //$printer -> text(PrintStr::NEWLINE . PrintStr::NEWLINE);
    
    //$printer -> setEmphasis(true);
    //if (strtolower($sliptype) == "or"){
    //    $printer -> text(str_pad("THIS RECEIPT SHALL BE VALID", PrintCharSize::REGULAR, " ", STR_PAD_BOTH));
    //} else
    //    $printer -> text(str_pad("THIS INVOICE SHALL BE VALID", PrintCharSize::REGULAR, " ", STR_PAD_BOTH));
    
    //$printer -> text(PrintStr::NEWLINE);
    //$printer -> text(str_pad("FOR FIVE(5) YEARS FROM THE DATE OF", PrintCharSize::REGULAR, " ", STR_PAD_BOTH));
    //$printer -> text(PrintStr::NEWLINE);
    //$printer -> text(str_pad("THE PERMT TO USE.", PrintCharSize::REGULAR, " ", STR_PAD_BOTH));
    //END : PRINT HEADER
}
