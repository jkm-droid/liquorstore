package jkmdroid.likastore;

/**
 * Created by jkmdroid on 6/16/21.
 */
public class Constants {
    public static final int CONNECT_TIMEOUT = 60 * 1000;

    public static final int READ_TIMEOUT = 60 * 1000;

    public static final int WRITE_TIMEOUT = 60 * 1000;

    public static final String BASE_URL = "https://sandbox.safaricom.co.ke/";

    public static final String BUSINESS_SHORT_CODE = "174379";
    public static final String PASSKEY = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919";
    public static final String TRANSACTION_TYPE = "CustomerPayBillOnline";
    public static final String PARTY_B = "174379"; //same as business shortcode above
    public static final String CALLBACK_URL = "https://mpesa-requestbin.herokuapp.com/1meiusq1";
    public static final String ACCOUNT_REFERENCE = "LiquorStore";
    public static final String TRANSACTION_DESCRIPTION = "Payment of D";
}
