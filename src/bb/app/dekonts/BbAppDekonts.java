/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bb.app.dekonts;

import java.io.File;
import jaxesa.util.Util;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

/**
 *
 * @author esabil
 */
public class BbAppDekonts 
{
    public static final int BANK_CODE_YKB    = 0;
    public static final int BANK_CODE_ISBANK = 1;

    /**
     * This app reads receipts (dekont) from the file and generates / output a excel decont file
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        // TODO code application logic here
        try
        {
            System.out.println("Working Directory = " + System.getProperty("user.dir"));

            //String sFilePath = "/Users/esabil/Documents/files/Hesap_Hareket_Detay_64265549_TL.pdf";
            //String sFilePath = "C:/NEOTEMP/CODE/SHIPSHUK/files/Hesap_Hareket_Detay_64265549_TL.pdf";//ykb
            String sFilePath = "C:/Users/Administrator/Downloads/64050199824_20190829_16393162_HesapOzeti.pdf";//isbank
            
            PDDocument document = null; 
            document = PDDocument.load(new File(sFilePath));
            document.getClass();
            String st = "";
            if( !document.isEncrypted() )
            {
                PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                stripper.setSortByPosition( true );
                PDFTextStripper Tstripper = new PDFTextStripper();
                st = Tstripper.getText(document);
                //System.out.println("Text:"+st);

            }
            
            //PDF FILE LINES
            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            String[] Lines = st.split("\\n");
            
            DekontStructure CurDefs = new DekontStructure();

            //YKB DEKONT FORMAT/DEFINITIONS
            //------------------------------------------------------------------            
            DekontStructure YKBDefs = new DekontStructure();
            YKBDefs.TMP_LBL_END     = "YAPI VE KREDİ BANKASI A.Ş";
            YKBDefs.TMP_LBL_START   = "İşlem Tutarı";
            YKBDefs.DATA_TXNTYPE_SALE             = "PEŞİNSATIŞ";
            YKBDefs.DATA_TXNTYPE_INSTALLMENT      = "TAKSİTSATIŞ";
            YKBDefs.DATA_TXNTYPE_COMM_SALE        = "PEŞSTKOM";
            YKBDefs.DATA_TXNTYPE_COMM_INSTALLMENT = "TAKST KOM";
            YKBDefs.DATA_TXNTYPE_EFT              = "";

            //ISBANK DEKONT FORMAT/DEFINITIONS
            //------------------------------------------------------------------
            DekontStructure IsbankDefs = new DekontStructure();
            IsbankDefs.TMP_LBL_END     = "Sayfa ";
            IsbankDefs.TMP_LBL_START   = "Bakiyesi İşlem İşlem Tipi Açıklama";
            IsbankDefs.DATA_TXNTYPE_SALE             = "NET SATIŞ TUTAR";
            IsbankDefs.DATA_TXNTYPE_INSTALLMENT      = "";
            IsbankDefs.DATA_TXNTYPE_COMM_SALE        = "";
            IsbankDefs.DATA_TXNTYPE_COMM_INSTALLMENT = "";
            IsbankDefs.DATA_TXNTYPE_EFT              = "EFT";

            CurDefs = IsbankDefs;
            // REFORMATTING LINE
            //------------------------------------------------------------------
            // - Title Words (YKB)
            //   - Şube
            //   - Müşteri Numarası
            //   - Hesap Şubesi
            //   - Bitiş Tarihi
            //   - Müşteri Bilgileri
            //   - Müşteri Tipi
            //   - SBU
            //   - Ad-Soyad/Unvan
            //   - Hesap Numarası
            //   - Başlangıç Tarihi
            //   - IBAN Numarası
            //
            // - Data Start (Columns Starting)
            //   - İşlem Tutarı
            //   
            // - Data Ending (Footnote)
            //   - YAPI VE KREDİ BANKASI A.Ş
            //
            // - Content Filters (The records will be saved)
            //   - Contains PEŞİNSATIŞ (txn type PESIN)
            //   - Contains TAKSİTSATIŞ (TXN type TAKSIT)
            //   - Contains PEŞSTKOM (Txn type = KOMISYON)
            //   - Contains TAKST KOM (Txn type = KOMISYON)

            
            //String sPathFormattedFile = "/Users/esabil/Documents/files/dekont_summary.txt";
            String sPathFormattedFile = "C:/NEOTEMP/CODE/SHIPSHUK/files/dekont_summary.txt";
            boolean bNoFilter = false;
            boolean bDataStarted = false;
            for (String lineN: Lines)
            {
                
                //int index = lineN.indexOf( "YAPI VE KREDİ BANKASI A.Ş");
                int index = lineN.indexOf(CurDefs.TMP_LBL_END);
                if (index ==0)
                    bDataStarted = false;//ending
                    
                if (bDataStarted==true) 
                {
                    boolean bRecordYes = false;
                    
                    //FILTER HERE
                    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    String sTxnType = "";
                    //bRecordYes = lineN.contains("PEŞİNSATIŞ");
                    sTxnType = findTxnType(CurDefs, lineN);
                    if (sTxnType.trim().length()>0)
                        bRecordYes = true;
                    
                    //REFORMAT HERE
                    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    if (bRecordYes==true)
                    {
                        //lineN = sTxnType + "\t" + lineN;
                        String sNewLine = "";
                        sNewLine = parseDataLine(BANK_CODE_ISBANK, sTxnType, lineN);
                        //sNewLine = parseYKBDataLine(sTxnType, lineN);

                        System.out.println(lineN);
                        System.out.println(sNewLine);

                        //if (bNoFilter==false)
                            Util.Files.Write2File(sPathFormattedFile, sNewLine);
                        //else
                            
                        
                    }
                }
                
                if (bNoFilter==true)
                    Util.Files.Write2File(sPathFormattedFile, lineN);//no filter - write full data

                //index = lineN.indexOf( "İşlem Tutarı");
                index = lineN.indexOf( CurDefs.TMP_LBL_START );
                if (index == 0)
                {
                    bDataStarted = true;
                }
                
            }
            
            String sEnd = "end";
            
            // FULL-TEXT READ
            /*
            PDDocument document = PDDocument.load(new File(sFilePath));
            if (!document.isEncrypted()) 
            {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);
                System.out.println("Text:" + text);
            }
            document.close();
            */
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
    
    public static String findTxnType(DekontStructure pBankDefs, String pDataLine)
    {
        boolean bRecordYes = false;
        String lineN = pDataLine;
        String sTxnType = "";
        
        String sLBL_DEF = pBankDefs.DATA_TXNTYPE_SALE;
        bRecordYes = lineN.contains(sLBL_DEF);
        if ((bRecordYes==true) && (sLBL_DEF.trim().length()>0))
        {
            //PESIN SATIS
            sTxnType = "PESIN";
        }
        else
        {
            //bRecordYes = lineN.contains("TAKSİTSATIŞ"); 
            sLBL_DEF = pBankDefs.DATA_TXNTYPE_INSTALLMENT;
            bRecordYes = lineN.contains(sLBL_DEF);
            if ((bRecordYes==true) && (sLBL_DEF.trim().length()>0))
            {
                sTxnType = "TAKSIT";
            }
            else
            {
                //bRecordYes = lineN.contains("PEŞSTKOM");
                sLBL_DEF = pBankDefs.DATA_TXNTYPE_COMM_SALE;
                bRecordYes = lineN.contains(sLBL_DEF);
                if ((bRecordYes==true) && (sLBL_DEF.trim().length()>0))
                {
                    sTxnType = "KOMISYON";
                }
                else
                {
                    //bRecordYes = lineN.contains("TAKST KOM");
                    sLBL_DEF = pBankDefs.DATA_TXNTYPE_COMM_INSTALLMENT;
                    bRecordYes = lineN.contains(sLBL_DEF);
                    if ((bRecordYes==true) && (sLBL_DEF.trim().length()>0))
                    {
                        sTxnType = "KOMISYON";
                        lineN = lineN.replaceAll(pBankDefs.DATA_TXNTYPE_COMM_INSTALLMENT,"TAKSTKOM");//works for YKB only
                    }
                    else
                    {
                        sLBL_DEF = pBankDefs.DATA_TXNTYPE_EFT;
                        bRecordYes = lineN.contains(sLBL_DEF);
                        if ((bRecordYes==true) && (sLBL_DEF.trim().length()>0))
                        {
                            sTxnType = "EFT";
                        }
                    }
                }

            }
        }

        return sTxnType;
    }

    /*
        This returns 
    */
    public static String parseDataLine(int piBankCode, String psTxnType, String pDataLine)
    {
        int i = 0;
        
        switch(piBankCode)
        {
            case BANK_CODE_YKB:

                return parseYKBDataLine(psTxnType, pDataLine);

            case BANK_CODE_ISBANK:

                return parseIsbankDataLine(psTxnType, pDataLine);
        }

        return "";
    }

    /*
        Warning: 
    */
    public static String parseIsbankDataLine(String psTxnType, String pDataLine)
    {
        String[] sCols  = pDataLine.split(" ");
        
        return "";
    }
    
    /*
        Warning: Columns order in pdf file comes different order when it is read.
        The order in pdf is following
        Tarih(1), Valor(2), Aciklama(3), Islem Tutari(4), Islem Saati(5), Bakiye(6), Dekont No(7)
    
        However, when read
        Tarih(1), Tutar(2), Bakiye + Valor (3) + ....
    */
    public static String parseYKBDataLine(String psTxnType, String pYKBDataLine)
    {
        String sColTxnType = psTxnType;
        String sColDate = "";
        String sColRelease = "";
        String sColDesc = "";
        String sColAmount = "";
        String sColTime = "";
        String sColBalance = "";
        String sColTraceNo = "";
        String sColMonthNo = "";

        //String sNewLine = lineN.replaceAll(" ", "\t");
        String[] sCols  = pYKBDataLine.split(" ");

        for (int i=0;i<sCols.length;i++)
        {
            String sColData = sCols[i];

            switch(i)
            {
                case 0:
                    //Txn Date
                    sColDate = sColData;

                    String[] sDateParts = sColDate.split("\\.");

                    sColMonthNo = sDateParts[1];

                    break;
                case 6:
                    //Time Date
                    sColTime = sColData;

                    break;
                case 1:
                    //Desc
                    sColDesc = sColData;

                    break;
                case 2:
                    //Amount
                    sColAmount = sColData;

                    break;
                case 3:
                    //Balance + Release (No space between) (To be parsed)

                    String sBalNRelease = sColData;

                    int index1stDot = sBalNRelease.indexOf(".");

                    sColBalance = sBalNRelease.substring(0, index1stDot + 2 + 1);
                    sColRelease = sBalNRelease.substring(index1stDot + 2 + 1);

                    break;
                case 5:
                    // Trance No
                    sColTraceNo = sColData;

                    break;
            }
        }

        String sNewLine =   sColTxnType + "\t" + 
                            sColDate + "\t" + 
                            sColRelease + "\t" + 
                            sColDesc + "\t" + 
                            sColAmount + "\t" + 
                            sColTime + "\t" + 
                            sColBalance + "\t" + 
                            sColTraceNo + "\t" + 
                            sColMonthNo;
        
        return sNewLine;
    }
    
}
