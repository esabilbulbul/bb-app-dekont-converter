/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bb.app.dekonts;

/**
 *
 * @author Administrator
 */
public class DekontDefs 
{
    public int    BankCode;
    
    public String TMP_LBL_START;//Indicates that the data starts after this
    public String TMP_LBL_END;//Indicates that the data ends after this
    
    public String DATA_TXNTYPE_SALE;//Satis
    public String DATA_TXNTYPE_INSTALLMENT;//Taksit
    public String DATA_TXNTYPE_COMM_SALE;//Satis Komisyon
    public String DATA_TXNTYPE_COMM_INSTALLMENT;//Taksitli Komisyon
    
    public String DATA_TXNTYPE_EFT;

}
