# NfcReaderLibrary

Android Library to read NFC Tag

 Add the JitPack repository to your build file
 
 Gradle:

    allprojects { 
      repositories 
      { ...
         maven { url 'https://jitpack.io'
     } 
     } 
   }
       
    dependencies {
        implementation 'com.github.Androidlabz:NfcReaderLibrary:LatestVersion'}
       }
       
  Add NFC Permision in androidmanifest.xml
       
   <uses-permission android:name="android.permission.NFC" />
  
  
    Declare nfc adapter and chip reader object in the class
  
     public class MainActivity extends AppCompatActivity{


    private NfcAdapter mNfcAdapter;
    private NfcChipReader mNfcChipReader;
  
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    //initialize nfc adapter which is default in-built android class
    mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
    //check if nfc adapter is not null and initialize the chip reader object
    if (mNfcAdapter != null) {
      mNfcChipReader = new NfcChipReader(mNfcAdapter, new NfcChipReader.Callback() {
        @Override public void onRecieveTag(String s) {
          // Callback to recieve read tag content here
        }

        @Override public void onReadTagError() {
          // Callback to recieve error to read tags
        }

        @Override public void onNfcDisabled() {
          // Callback to recieve NFC Disabled
        }

        @Override public void onNfcNotFoundInDevice() {
          // Callback to no nfc found in device
        }
      });
    }
  
  
    //In on resume method enable the nfc adapter foreground dispatch
    @Override public void onResume() {
   
    super.onResume();
    IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
    IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
    IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
    IntentFilter[] nfcIntentFilter = new IntentFilter[] {techDetected, tagDetected, ndefDetected};
    PendingIntent pendingIntent = PendingIntent.getActivity(
        this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    if (mNfcAdapter != null) {
      if (!mNfcAdapter.isEnabled()) {
        // Nfc is disabled
        Toast.makeText(this, "Nfc Disabled", Toast.LENGTH_SHORT).show();
      }
      mNfcAdapter.enableForegroundDispatch(this, pendingIntent, nfcIntentFilter, null);
      mNfcAdapter.isNdefPushEnabled();
     } else {
      // No nfc found in device
      Toast.makeText(this, "No Nfc Found", Toast.LENGTH_SHORT).show();
     }
   }
  
    //In on pause disable the nfc adapter foreground dispatch
  
     @Override
     protected void onPause() {
       super.onPause();
       if (mNfcAdapter != null) {
       mNfcAdapter.disableForegroundDispatch(this);
     }
    }

    //on New intent we will recieve the tag in parcelable extra
    @Override
    protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    if (mNfcChipReader != null) {
      mNfcChipReader.nfcTagReadBuilder(intent);
     }
    }


