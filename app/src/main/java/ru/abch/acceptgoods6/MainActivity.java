package ru.abch.acceptgoods6;

import static android.os.Environment.isExternalStorageEmulated;
import static android.text.InputType.TYPE_CLASS_NUMBER;

import static org.apache.http.conn.ssl.SSLSocketFactory.SSL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.bosphere.filelogger.FL;
import com.bosphere.filelogger.FLConfig;
import com.bosphere.filelogger.FLConst;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import android.device.ScanManager;
import android.device.scanner.configuration.PropertyID;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import ru.abch.acceptgoods6.ui.main.ExcessiveGoodsFragment;
import ru.abch.acceptgoods6.ui.main.GoodsFragment;
import ru.abch.acceptgoods6.ui.main.MainFragment;
import ru.abch.acceptgoods6.ui.main.MainViewModel;
import ru.abch.acceptgoods6.ui.main.PhotoFragment;
import ru.abch.acceptgoods6.ui.main.StartFragment;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, SoundPool.OnLoadCompleteListener {
    private static final String TAG = "MainActivity";
    final int POSTGOODS = 1, POSTLABEL = 2, POSTEXCESSIVE = 3, POSTLOG = 4;
    private static final int REQ_PERMISSION = 1233;
    private static TextToSpeech mTTS;
    public static MainViewModel mViewModel;
    ConnectivityManager cm;
    final static String CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    private static final String ACTION_BARCODE_DATA = "com.honeywell.sample.action.BARCODE_DATA";
//    GoodsPosition gp = null, prevGP = null;
    IntentFilter intentFilter;
    public static boolean online = false;
    private Timer mTimer;
    String filenameSD;
    URI uri = null;
    String barcodesURL = null;
    String goodsURL = null;
    String acceptedGoodsURL = null;
    String cellsURL = null;
    String dumpURL = null;
    public String[] names;
    SoundPool soundPool;
    int soundId1;
    final int MAX_STREAMS = 2;
    AlertDialog.Builder adbSettings, adbLost, adbPosition, adbDCTNum, adbPlacedGoods;
    private EditText etDCTNumber, etPosition;
    boolean requestPosition;
    AlertDialog adPosition;
    Cell newPosition;
    GetWSCells getCells;
    boolean barcodesRequest = false, goodsRequest = false;
    MainFragment mf = null;
    boolean backPressed = false, boxReady = false;
    PostWebservice postWS;
    GetWSAllGoods getAllGoods;
    ExcessiveGoodsFragment egf = null;
    GoodsFragment gf = null;
    StartFragment sf;
    PhotoFragment phf;
    int progressLevel = 0;
    ProgressBar pbWait;
//    LiveData<ScannedCode> scannedGoodsData, scannedMainData, scannedEGFData;
    int retryBox = -1;
    Context ctx;
    private BroadcastReceiver barcodeDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_BARCODE_DATA.equals(intent.getAction())) {
                int version = intent.getIntExtra("version", 0);
                if (version >= 1) {
                    String codeId = intent.getStringExtra("codeId");
                    String data = intent.getStringExtra("data");
                    FL.d(TAG, "Scanned=" + data + " codeId=" + codeId + " state " + App.state);
//                    mViewModel.loadScannedData(new ScannedCode(codeId, data),App.state);
                    processScannedData(new ScannedCode(codeId, data),App.state);
                }
            }
        }
    };
    public void processScannedData(ScannedCode sc, int state) {
        notification();
        if(state == App.START) {
            sf = (StartFragment) getSupportFragmentManager().findFragmentByTag(StartFragment.class.getSimpleName());
            if(sf != null) sf.processScan(sc);
        } else if(state == App.SELECTEXCESSIVEGOODS) {
//            scannedEGFragment.postValue(sc);
//            FL.d(TAG,"To ExcessiveGoodsFragment data " + sc.data + " id " + sc.codeId);
            egf = (ExcessiveGoodsFragment) getSupportFragmentManager().findFragmentByTag(ExcessiveGoodsFragment.class.getSimpleName());
            if(egf != null) egf.processScan(sc);
        } else if(state == App.PUTGOODS) {
//            scannedGoodsFragment.postValue(sc);
//            FL.d(TAG,"To GoodsFragment data " + sc.data + " id " + sc.codeId);
            gf = (GoodsFragment) getSupportFragmentManager().findFragmentByTag(GoodsFragment.class.getSimpleName());
            if(gf != null) gf.processScan(sc);
        } else {
//            scannedMainFragment.postValue(sc);
//            Log.d(TAG,"To MainFragment data " + sc.data + " id " + sc.codeId);
            mf = (MainFragment) getSupportFragmentManager().findFragmentByTag(MainFragment.class.getSimpleName());
            if(mf != null) mf.processScan(sc);
        }
    }
    @Override
    public void onLoadComplete(SoundPool soundPool, int i, int i1) {
        Log.d(TAG, "onLoadComplete, sampleId = " + i + ", status = " + i1);
    }
    public static void say(String text) {
        if (Config.tts) mTTS.speak(text, TextToSpeech.QUEUE_ADD, null, null);
    }

    void writeFileSD(String line) {
        // проверяем доступность SD
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Log.d(TAG, "SDcard not available: " + Environment.getExternalStorageState());
            return;
        }
        File sdPath = null;
        File[] listExternalDirs = ContextCompat.getExternalFilesDirs(this, null);
        for(File f : listExternalDirs) {
            if(!isExternalStorageEmulated(f)) {
                sdPath = f;
                break;
            }
        }
        if (sdPath == null) {
            Toast.makeText(this,getResources().getString(R.string.no_sd_card),Toast.LENGTH_SHORT).show();
            for(File f : listExternalDirs) {
                if(f.getAbsolutePath().contains("emulated")) {
                    sdPath = f;
                    break;
                }
            }
        }
        if (sdPath != null) {
            sdPath.mkdirs();
            File sdFile = new File(sdPath, filenameSD);
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile, true));
                bw.write(line + "\n\r");
                bw.close();
                Log.d(TAG, "File is written out: " + sdFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this,getResources().getString(R.string.sd_card_error),Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this,getResources().getString(R.string.sd_card_error),Toast.LENGTH_SHORT).show();
        }
    }
    private BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String phrase;
            if(cm.getActiveNetworkInfo() != null) {
                FL.d(TAG,"Network " + cm.getActiveNetworkInfo().getExtraInfo() + " " + cm.getActiveNetworkInfo().getDetailedState());
                phrase = "wifi подключен";
                online = true;
                if (mTimer != null) {
                    mTimer.cancel();
                }
            } else {
                FL.d(TAG, "Network disconnected");
                phrase = "wifi отключен";
                online = false;
                mTimer = new Timer();
//                offlineTimerTask = new OfflineTimerTask();
//                mTimer.schedule(offlineTimerTask, Config.offlineTimeout);
            }
            Toast.makeText(context, phrase, Toast.LENGTH_LONG).show();
        }
    };
    public void gotoPhotoFragment() {
//        if(App.state != App.SELECTGOODS) App.state = App.MAINCYCLE;
//        getDump();
        if(phf == null) phf = PhotoFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, phf, PhotoFragment.class.getSimpleName())
                .commitNow();
    }
    public void gotoMainFragment() {
        if(mf == null) mf = MainFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, mf, MainFragment.class.getSimpleName())
                .commitNow();
    }
    public void gotoGoodsFragment(GoodsRow gr, int qnt) {
        App.state = App.PUTGOODS;
        gf = GoodsFragment.newInstance(gr, qnt);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, gf, GoodsFragment.class.getSimpleName())
                .commitNow();
    }
    public void gotoExcessiveGoodsFragment() {
        egf = ExcessiveGoodsFragment.newInstance(App.state);
        App.state = App.SELECTEXCESSIVEGOODS;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, egf, ExcessiveGoodsFragment.class.getSimpleName())
                .commitNow();
    }
    @Override
    public void onDestroy() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
        unregisterReceiver(networkChangeReceiver);
        if(barcodeDataReceiver != null) unregisterReceiver(barcodeDataReceiver);
        registerReceiverUrovo(false);
        super.onDestroy();
    }
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            Locale locale = new Locale("ru");
            int result = mTTS.setLanguage(locale);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "TTS: language not supported");
            }
            if (result == TextToSpeech.SUCCESS) {
                Log.d(TAG, "TTS OK");
                if (App.state == App.START) {
                    say(getResources().getString(R.string.storeman_number_tts));
                }
            }
        } else {
            Log.e(TAG, "TTS: error");
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(networkChangeReceiver, intentFilter);
        if(cm.getActiveNetworkInfo() != null) {
            FL.d(TAG,"Network " + cm.getActiveNetworkInfo().getExtraInfo() + " " + cm.getActiveNetworkInfo().getDetailedState());
        }
//        registerReceiver(barcodeDataReceiver, new IntentFilter(ACTION_BARCODE_DATA));
        if (initScanUrovo()) {
            registerReceiverUrovo(true);
            barcodeDataReceiver = null;
//            getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM, WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        } else {
            registerReceiver(barcodeDataReceiver, new IntentFilter(ACTION_BARCODE_DATA));
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PERMISSION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                finish();
            }
        }
    }
    void notification() {
        soundPool.play(soundId1, 1, 1, 0, 0, 1);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
        setContentView(R.layout.main_activity);
        dumpLog();
        FL.init(new FLConfig.Builder(this)
                .minLevel(FLConst.Level.V)
                .logToFile(true)
                .dir(new File(Environment.getExternalStorageDirectory(), App.appName))
                .retentionPolicy(FLConst.RetentionPolicy.FILE_COUNT)
                .build());
        FL.setEnabled(true);
        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        pbWait = findViewById((R.id.pb_wait));
//        names = getResources().getStringArray(R.array.store_names);
        if(App.warehouse != null) {
            FL.d(TAG, "Start " + App.packageName.trim() + " build " + App.versionCode + " store id '" + App.getStoreId() + "' " + App.getStoreName().trim() +
                    " current box " + App.currentBoxName + " id '" + App.currentBoxId + "'");
        } else {
            FL.d(TAG, "First start " + App.packageName.trim() + " build " + App.versionCode);
        }
        /*
        scannedGoodsData = mViewModel.getScannedGoodsFragment();
        scannedMainData = mViewModel.getScannedMainFragment();
        scannedEGFData = mViewModel.getScannedEGFragment();

         */
        filenameSD = Database.getCurrentDate() + ".txt";
        try {
            uri = new URI(
                    Config.scheme, null, Config.ip, Config.port,
                    Config.warehousesPath,
                    null, null);
            /*
            uri = new URI(
                    "http", null, "192.168.21.244", 8080,
                    Config.allGoodsPath + App.getStoreId() + "/",
                    null, null);

             */
        } catch (URISyntaxException e) {
            FL.e(TAG, e.getMessage());
        }

        String warehousesURL = uri.toASCIIString();
        GetWSWarehouses getWSWarehouses = new GetWSWarehouses();
        try {
            getWSWarehouses.run(warehousesURL);
        } catch (IOException e) {
            FL.e(TAG, e.getMessage());
        }
        try {
            uri = new URI(
                    Config.scheme, null, Config.ip, Config.port,
                    Config.barcodesPath + App.getStoreId() + "/",
                    null, null);
        } catch (URISyntaxException e) {
            FL.e(TAG, e.getMessage());
        }
        barcodesURL = uri.toASCIIString();
        GetWSBarcodes getBarCodes = new GetWSBarcodes();
        try {
            getBarCodes.run(barcodesURL);
            barcodesRequest = true;
        } catch (IOException e) {
            FL.e(TAG, e.getMessage());
        }
        try {
            uri = new URI(
                    Config.scheme, null, Config.ip, Config.port,
                    Config.allGoodsPath + App.getStoreId() + "/",
                    null, null);
        } catch (URISyntaxException e) {
            FL.e(TAG, e.getMessage());
        }
        String allGoodsURL = uri.toASCIIString();
        try {
            uri = new URI(
                    Config.scheme, null, Config.ip, Config.port,
                    Config.cellsPath + App.getStoreId() + "/",
                    null, null);
        } catch (URISyntaxException e) {
            FL.e(TAG, e.getMessage());
        }
        cellsURL = uri.toASCIIString();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mTTS = new TextToSpeech(this, this);
        ArrayList<String> requestPermissionsList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissionsList.add(Manifest.permission.CAMERA);
        }
        if(requestPermissionsList.size() > 0) {
            String[] requestPermissionsArray = new String[requestPermissionsList.size()];
            requestPermissionsArray = requestPermissionsList.toArray(requestPermissionsArray);
            ActivityCompat.requestPermissions(this, requestPermissionsArray, REQ_PERMISSION);
        }
        /*
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_PERMISSION);
        }

        if(App.getStoreName().isEmpty()) {
            adbSettings = new AlertDialog.Builder(this);
            adbSettings.setTitle(R.string.store_choice)
                    .setItems(names, (dialog, which) -> {
                        FL.d(TAG, "Index = " + which + " store id=" + App.ids[which]);
                        App.setStoreIndex(which);
                        App.setStoreId(App.ids[which]);
                        App.setStoreName(names[which]);
                    }).create().show();
        }

         */
        soundPool= new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(this);
        soundId1 = soundPool.load(this, R.raw.pik, 1);
        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.addDefaultNetworkActiveListener(() -> FL.d(TAG,"Network active"));
        intentFilter = new IntentFilter();
        intentFilter.addAction(CONNECTIVITY_ACTION);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // end onCreate
        Timer logTimer = new Timer();
        logTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                dumpLog();
            }
        }, 3600000L - System.currentTimeMillis() % 3600000L, 3600000L); //1 hour period
        Database.clearAllGoods();
        getAllGoods = new GetWSAllGoods();
        try {
            getAllGoods.run(allGoodsURL);
        } catch (IOException e) {
            FL.e(TAG, e.getMessage());
        }
        if (App.getCurrentBox() == null) {
            // start new box
            refreshCells();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, StartFragment.newInstance(), StartFragment.class.getSimpleName())
                    .commitNow();
        } else {
            Log.d(TAG, "onCreate() current box " + App.getCurrentBox().descr);
            gotoMainFragment();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Add your menu entries here
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings_item:
                FL.d(TAG, "Settings clicked");
                adbSettings = new AlertDialog.Builder(this);
                adbSettings.setTitle(R.string.store_choice)
                        .setItems(names, new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which) {
                                FL.d(TAG, "Index = " + which + " store id=" + App.warehouses[which].id);
                                sf = (StartFragment) getSupportFragmentManager().findFragmentByTag(StartFragment.class.getSimpleName());
                                if (sf != null) {
                                    App.setWarehouse(App.warehouses[which]);
                                    sf.setStore();
                                    Database.clearGoods();
                                    refreshData();
                                    refreshCells();
                                    System.exit(0);
                                }
                            }
                        }).create().show();

                return true;
            case R.id.refresh_item:
                FL.d(TAG, "Refresh clicked");
                Database.clearGoods();
//                refreshData();
                if (App.state != App.START) refreshData();
                return true;
            case R.id.dct_num_item:
                FL.d(TAG, "DCT num clicked");
                etDCTNumber = new EditText(this);
                etDCTNumber.setInputType(TYPE_CLASS_NUMBER);
                etDCTNumber.setText(String.valueOf(App.getDctNum()));
                adbDCTNum = new AlertDialog.Builder(this);
                adbDCTNum.setCancelable(false);
                adbDCTNum.setMessage(R.string.dct_num);
                adbDCTNum.setView(etDCTNumber);
                adbDCTNum.setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                    String num = etDCTNumber.getText().toString();
                    Log.d(TAG, "DCT num " + num);
                    App.setDctNum(num);
                });
                adbDCTNum.create().show();
                return true;
            case R.id.position:
                Log.d(TAG, "Position clicked");
                requestPosition = true;
                adbPosition = new AlertDialog.Builder(this);
                adbPosition.setCancelable(true);
                adbPosition.setMessage(R.string.current_position);
                etPosition = new EditText(this);
                adbPosition.setView(etPosition);
                adPosition = adbPosition.create();
                etPosition.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        Log.d(TAG, "On text changed =" + charSequence);
                    }
                    @Override
                    public void afterTextChanged(Editable editable) {
                        String input;
                        int entIndex;
                        int prefix, suffix;
                        String result;
                        if(editable.length() > 2) {
                            input = editable.toString();
                            if (input.contains("\n") && input.indexOf("\n") == 0) {
                                input = input.substring(1);
                            }
                            if (input.contains("\n") && input.indexOf("\n") > 0) {
                                entIndex = input.indexOf("\n");
                                input = input.substring(0, entIndex);
                                if (CheckCode.checkCellStr(input)) {
                                    prefix = Integer.parseInt(input.substring(0, input.indexOf(".")));
                                    suffix = Integer.parseInt(input.substring(input.indexOf(".") + 1));
                                    result = String.format("%02d",prefix) + String.format("%03d",suffix);
                                    Log.d(TAG, "Cell name " + result);
                                    newPosition = Database.getCellByName(result);
                                    requestPosition = false;
                                    adPosition.dismiss();
                                    if (newPosition != null) setPosition(newPosition);
                                } else {
                                    MainActivity.say(getResources().getString(R.string.enter_again));
                                }
                                etPosition.setText("");
                            }
                        }
                    }
                });
                adPosition.show();
                return true;

            case R.id.clear_item:
                AlertDialog.Builder adbClear = new AlertDialog.Builder(this);
                adbClear.setCancelable(true);
                adbClear.setTitle(getResources().getString(R.string.clear)).setMessage(getResources().getString(R.string.clear_msg));
                adbClear.setNegativeButton(getResources().getString(R.string.no), (dialog, which) -> {

                });
                adbClear.setPositiveButton(getResources().getString(R.string.yes), (dialog, which) -> {
                    FL.d(TAG, "Clear all data");
                    App.setCurrentBox(null);
                    Database.clearGoods();
                    Database.clearData();
                    mViewModel.loadGoodsData();
//                    App.setPackMode(false);
                    App.setCurrentPackId("");
                    App.setCurrentPackNum("");
                });
                adbClear.create().show();
                return true;
            case R.id.placed_item:
                showAdbPlaced();
                return true;
            case R.id.lost_item:
                Log.d(TAG,"state " + App.state);
                if(App.state == App.PUTGOODS) {
                    if (App.getPackMode()) {
                        Log.d(TAG, "Lost goods in pack");
                        adbLost = new AlertDialog.Builder(this);
                        adbLost.setCancelable(false);
                        adbLost.setMessage(R.string.confirm_lost);
                        adbLost.setTitle(R.string.lost_goods);
                        adbLost.setNegativeButton(R.string.no, (dialogInterface, i) -> {

                        });
                        adbLost.setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                            Log.d(TAG, "Confirm lost goods ");
                            GoodsFragment gf = (GoodsFragment) getSupportFragmentManager().findFragmentByTag(GoodsFragment.class.getSimpleName());
                            if (gf != null) {
                                gf.processLost();
                            }
                        });
                        adbLost.create().show();
                    } else {
                        Cell lostGoodsCell = Database.getCellByName(getResources().getString(R.string.lost_cell_name));
                        if (lostGoodsCell != null) {
                            Log.d(TAG, "Lost goods cell id " + lostGoodsCell.id);
                            adbLost = new AlertDialog.Builder(this);
                            adbLost.setCancelable(false);
                            adbLost.setMessage(R.string.confirm_lost);
                            adbLost.setTitle(R.string.lost_goods);
                            adbLost.setNegativeButton(R.string.no, (dialogInterface, i) -> {

                            });
                            adbLost.setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                                Log.d(TAG, "Confirm lost goods ");
                                GoodsFragment gf = (GoodsFragment) getSupportFragmentManager().findFragmentByTag(GoodsFragment.class.getSimpleName());
                                if (gf != null) {
                                    gf.processLost(lostGoodsCell);
                                }
                            });
                            adbLost.create().show();
                        }
                    }
                }
                return true;
            case R.id.excessive_item:
                if(App.state == App.MAINCYCLE || App.state == App.SELECTGOODS) {
                    gotoExcessiveGoodsFragment();
                }
                return true;
            default:
                break;
        }
        return false;
    }
    private void setPosition(Cell cell) {
        App.currentDistance = cell.distance;
        Log.d(TAG, "Set position distance " + App.currentDistance);
    }
    private void refreshCells() {
        getCells = new GetWSCells();
        try {
            getCells.run(cellsURL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void getDump(int type) {
        GetWSDump getWSDump = new GetWSDump();
        String pathPrefix = (type == POSTEXCESSIVE)? Config.dumpExcessivePath : Config.dumpPath;
        try {
//            String scheme = "http", ip = "192.168.21.244";
//            int port = 8080;
            uri = new URI(
                    Config.scheme, null, Config.ip, Config.port,
                    pathPrefix + App.getStoreId() + "/" + App.getDctNum() + "/",
                    null, null);
        } catch (URISyntaxException e) {
            FL.e(TAG, e.getMessage());
        }
        dumpURL = uri.toASCIIString();
        try{
            getWSDump.run(dumpURL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void refreshData() {
        Log.d(TAG,"Pack mode " + App.getPackMode());
        /*
        GetWSBarcodes getBarCodes = new GetWSBarcodes();
        try {
            getBarCodes.run(barcodesURL);
            barcodesRequest = true;
        } catch (IOException e) {
            FL.e(TAG, e.getMessage());
        }

         */
        if(App.getPackMode()) {
            if(!App.getCurrentPackId().isEmpty()) {
                Log.d(TAG, "Get pack content");
                GetWSPack getPack = new GetWSPack();
                Database.clearData();
                try {
                    uri = new URI(
                            Config.scheme, null, Config.ip, Config.port,
                            Config.packBarcodesPath + "id/" + App.getStoreId() + "/" + App.getCurrentPackId() + "/",
                            null, null);
                } catch (URISyntaxException e) {
                    FL.e(TAG, e.getMessage());
                }
                barcodesURL = uri.toASCIIString();
                try {
                    getPack.run(getPackURL(App.getCurrentPackId()));
                } catch (IOException e) {
                    FL.e(TAG, e.getMessage());
                }
                GetWSBarcodes getBarCodes = new GetWSBarcodes();
                try {
                    getBarCodes.run(barcodesURL);
                    barcodesRequest = true;
                } catch (IOException e) {
                    FL.e(TAG, e.getMessage());
                }
            } else if(!App.getCurrentPackNum().isEmpty()){
                Log.d(TAG, "Get pack content by num " + App.getCurrentPackNum());
                GetWSPack getPack = new GetWSPack();
                Database.clearData();
                try {
                    getPack.run(getPackNumURL(App.getCurrentPackNum()));
                } catch (IOException e) {
                    FL.e(TAG, e.getMessage());
                }
                GetWSBarcodes getBarCodes = new GetWSBarcodes();
                try {
                    uri = new URI(
                            Config.scheme, null, Config.ip, Config.port,
                            Config.packBarcodesPath + "num/" + App.getStoreId() + "/" + App.getCurrentPackNum() + "/",
                            null, null);
                } catch (URISyntaxException e) {
                    FL.e(TAG, e.getMessage());
                }
                barcodesURL = uri.toASCIIString();
                try {
                    getBarCodes.run(barcodesURL);
                    barcodesRequest = true;
                } catch (IOException e) {
                    FL.e(TAG, e.getMessage());
                }
            }
        } else {
            if (App.getCurrentBox() != null) {
                GetWSGoods getGoods = new GetWSGoods();
                Database.clearData();
                try {
                    goodsURL = getGoodsURL(App.getCurrentBox().id);
                    getGoods.run(goodsURL);
                    goodsRequest = true;
                } catch (IOException e) {
                    FL.e(TAG, e.getMessage());
                }
            }
        }
    }
    public static class GetWSCells {
        OkHttpClient client;
        String TAG = "GetWSCells";
        void run(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Log.d(TAG, "GET url " + url);
            WebserviceHTTPClient httpClient = new WebserviceHTTPClient(url);
            client = httpClient.getClient();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                public void onResponse(Call call, Response response)
                        throws IOException {
                    final String resp = response.body().string();
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    CellsResult cellsResult = gson.fromJson(resp, CellsResult.class);
                    if (cellsResult != null) {
                        Log.d(TAG, "Result = " + cellsResult.success + " length = " + cellsResult.counter);
                        if (cellsResult.counter > 0) {
                            Database.beginTr();
                            Database.clearCells();
                            for (int i = 0; i < cellsResult.counter; i++) {
//                                Log.d(TAG, " " + bcr.bc[i].goods + " " + bcr.bc[i].barcode+ " " + bcr.bc[i].qnt);
                                Database.addCell(
                                        cellsResult.cells[i].id,
                                        cellsResult.cells[i].name,
                                        cellsResult.cells[i].descr,
                                        cellsResult.cells[i].type,
                                        cellsResult.cells[i].distance,
                                        cellsResult.cells[i].zonein,
                                        cellsResult.cells[i].zonein_descr
                                );
                            }
                            Database.endTr();
                        }
                    }
                }
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, e.getMessage());
                }
            });
        }
    }
    public static class GetWSDump {
        OkHttpClient client;
        String TAG = "GetWSDump";
        void run(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Log.d(TAG, "GET url " + url);
            WebserviceHTTPClient httpClient = new WebserviceHTTPClient(url);
            client = httpClient.getClient();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                public void onResponse(Call call, Response response)
                        throws IOException {
                    final String resp = response.body().string();
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    DumpResult dumpResult = gson.fromJson(resp, DumpResult.class);
                    if (dumpResult != null) {
                        Log.d(TAG, "Result = " + dumpResult.success + " length = " + dumpResult.counter);
                        if (dumpResult.counter > 0) {
                            Database.beginTr();
                            for (int i = 0; i < dumpResult.counter; i++) {
                                Database.setGoodsSent(dumpResult.rows[i]);
                                Log.d(TAG, "Set row sent " + dumpResult.rows[i]);
                            }
                            Database.endTr();
                        }
                    }
                }
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            });
        }
    }
    public class GetWSGoods {
        OkHttpClient client;
        String TAG = "GetWSGoods";
        void run(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Log.d(TAG, "GET url " + url);
            WebserviceHTTPClient httpClient = new WebserviceHTTPClient(url);
            client = httpClient.getClient();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                public void onResponse(Call call, Response response)
                        throws IOException {
                    final String resp = response.body().string();
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    GoodsSet gs = gson.fromJson(resp, GoodsSet.class);
                    if (gs != null) {
                        Log.d(TAG, "Result = " + gs.success + " length = " + gs.counter);
                        if (gs.counter > 0) {
                            Database.beginTr();
                            for (int i = 0; i < gs.counter; i++) {
                                /*
                                Log.d(TAG, " " + gs.goodsRows[i].id + " " + gs.goodsRows[i].description + " " + gs.goodsRows[i].article + " " + gs.goodsRows[i].qnt
                                        + " " + gs.goodsRows[i].cell);

                                 */
                                Database.addGoods(
                                        gs.goodsRows[i].id,
                                        gs.goodsRows[i].description,
                                        gs.goodsRows[i].cell,
                                        gs.goodsRows[i].article,
                                        gs.goodsRows[i].qnt,
                                        gs.goodsRows[i].brand,
                                        gs.goodsRows[i].cellId
                                );
                            }
                            Database.endTr();
                            boxReady = true;
                            retryBox = -1;
                        } else if (gs.counter < 0) {
                            FL.d(TAG, "Processing box");
                            boxReady = false;
                            if(retryBox == -1) {
                                retryBox = App.MAX_RETRY;
                            } else {
                                --retryBox;
                            }
                            if(retryBox > 0) {
                                Timer refreshTimer = new Timer();
                                RefreshBoxTask rbt = new RefreshBoxTask();
                                refreshTimer.schedule(rbt, 10000);
                            } else {
                                boxReady = true;
                                retryBox = -1;
                            }
                        } else {        //gs.counter == 0
                            boxReady = true;
                            retryBox = -1;
                        }
                        runOnUiThread(waitBox);
                    }
                    goodsRequest = false;
                    mViewModel.loadGoodsData();
                }

                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, e.getMessage());
                }
            });
        }
    }
    public class GetWSBarcodes {
        OkHttpClient client;
        String TAG = "GetWSBarcodes";
        void run(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Log.d(TAG, "GET url " + url);
            WebserviceHTTPClient httpClient = new WebserviceHTTPClient(url);
            client = httpClient.getClient();
            Call call = client.newCall(request);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showProgress(true);
                }
            });
            call.enqueue(new Callback() {
                public void onResponse(Call call, Response response)
                        throws IOException {
                    final String resp = response.body().string();
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    BarCodesResult bcr = gson.fromJson(resp, BarCodesResult.class);
                    if (bcr != null) {
                        Log.d(TAG, "Result = " + bcr.success + " length = " + bcr.counter);
                        if (bcr.counter > 0) {
                            Database.beginTr();
                            for (int i = 0; i < bcr.counter; i++) {
//                                Log.d(TAG, " " + bcr.bc[i].goods + " " + bcr.bc[i].barcode+ " " + bcr.bc[i].qnt);
                                Database.addBarCode(
                                        bcr.bc[i].goods,
                                        bcr.bc[i].barcode,
                                        bcr.bc[i].qnt
                                );
                            }
                            Database.endTr();
                            FL.d(TAG, "Barcodes loaded");
                        }
                    }
                    barcodesRequest = false;
//                    if (!goodsRequest) runOnUiThread(endProgress);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showProgress(false);
                        }
                    });
                }
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showProgress(false);
                        }
                    });
                    FL.d(TAG, e.getMessage());
                }
            });
        }
    }
    public class PostWebservice {
        public final MediaType JSON = MediaType.get("application/json; charset=utf-8");
        int type = 0;
        String logFile = null;
        OkHttpClient client;
        String TAG = "PostWebService";
        void post(String url, String json) throws IOException {
//            Log.d(TAG, "\n\r" +json + "\n\r");
            if(type == POSTGOODS || type == POSTEXCESSIVE) writeFileSD(json);
            RequestBody body = RequestBody.create(json, JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            Log.d(TAG, "POST url " + url);
            WebserviceHTTPClient httpClient = new WebserviceHTTPClient(url);
            client = httpClient.getClient();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    FL.d(TAG, e.getMessage());
//                    say(getResources().getString(R.string.no_connection));
                }

                public void onResponse(Call call, Response response)
                        throws IOException {
                    FL.d(TAG, "Responce = " + response.code());
                    if (response.code() == 200) {
                        if (type == POSTGOODS || type == POSTEXCESSIVE) {
                            getDump(type);
                        }
                        if (type == POSTLOG && logFile != null) {
                            File log = new File(logFile);
                            log.delete();
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (App.state == App.PUTGOODS || App.state ==App.SELECTEXCESSIVEGOODS) {
            gotoMainFragment();
        } else {
            if (backPressed) {
//                App.setCurrentBox(null);
                super.onBackPressed();
                System.exit(0);
            } else {
                Toast.makeText(this, R.string.confirm_exit, Toast.LENGTH_LONG).show();
                say(getResources().getString(R.string.confirm_exit));
                backPressed = true;
            }
        }
    }
    public String getGoodsURL(String boxId) {
        URI uri = null;
        try {
            uri = new URI(
                    Config.scheme, null, Config.ip, Config.port,
                    Config.goodsPath + App.getStoreId() + "/" + boxId + "/",
                    null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return uri.toASCIIString();
    }
    public static String getCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Yekaterinburg"));
        Date today = Calendar.getInstance().getTime();
        return dateFormat.format(today);
    }
    private void dumpLog() {
        Gson gson = new Gson();
        LogFile logFile;
        try {
            uri = new URI(
                    Config.scheme, null, Config.ip, Config.port,
                    "/filelogger/log/" + App.deviceUniqueIdentifier + "/",
                    null, null);
            String logURL = uri.toASCIIString();
            File logPath = new File(Environment.getExternalStorageDirectory() + "/" + App.appName);
            File[] logFilesArray = logPath.listFiles();
            if (logFilesArray != null) for (File log : logFilesArray) {
                try {
                    FileInputStream fis = new FileInputStream(Environment.getExternalStorageDirectory() + "/" + App.appName + "/" + log.getName());
                    BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                    String str = "", sBody = "";
                    while ((str = br.readLine()) != null) {
                        sBody += str + "\r\n";
                    }
                    byte[] fileBody = sBody.getBytes(StandardCharsets.UTF_8);
                    logFile = new LogFile(App.appName, log.getName(), Base64.encodeToString(fileBody, Base64.DEFAULT));
                    postWS = new PostWebservice();
                    postWS.type = POSTLOG;
                    postWS.logFile = Environment.getExternalStorageDirectory() + "/" + App.appName + "/" + log.getName();
                    try {
                        postWS.post(logURL, gson.toJson(logFile));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (URISyntaxException e) {
            Log.d(TAG, e.getMessage());
        }
    }
    public void showAdbPlaced() {
        LinearLayout llAdbPlaced = (LinearLayout) getLayoutInflater().inflate(R.layout.adb_placed_goods, null);
        ListView lvPlaced = llAdbPlaced.findViewById(R.id.lv_placed_list);
        adbPlacedGoods = new AlertDialog.Builder(this);
        ArrayList<GoodsRow> placedList = Database.getPlacedList();
        PlacedGoodsAdapter pga = new PlacedGoodsAdapter(this, placedList);
        lvPlaced.setAdapter(pga);
        adbPlacedGoods.setView(llAdbPlaced);
        adbPlacedGoods.setTitle(R.string.placed_goods);
        adbPlacedGoods.setCancelable(true);
        adbPlacedGoods.setNegativeButton(R.string.close, (dialogInterface, i) -> {

        });
        adbPlacedGoods.create().show();
    }
    public void uploadGoods() {
//        uploadGoodsPosition(Database.goodsToUpload());
        uploadGoodsMove(Database.movesToUpload());
    }

    private void uploadGoodsMove(GoodsMove[] gma) {
        try {
            uri = new URI(
                    Config.scheme, null, Config.ip, Config.port,
                    Config.postExcessivePath + App.getStoreId() + "/" + App.getStoreMan() + "/",
                    null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        acceptedGoodsURL = uri.toASCIIString();
        if(gma != null && gma.length > 0) {
            MoveResult mr = new MoveResult(true, gma.length);
            mr.Moves = gma;
            postWS = new PostWebservice();
            postWS.type = POSTEXCESSIVE;
            Gson gson = new Gson();
            try {
                postWS.post(acceptedGoodsURL, gson.toJson(mr));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public class GetWSWarehouses {
        OkHttpClient client;
        String TAG = "GetWSWarehouses";
        void run(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Log.d(TAG, "GET url " + url);
            WebserviceHTTPClient httpClient = new WebserviceHTTPClient(url);
            client = httpClient.getClient();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                public void onResponse(Call call, Response response)
                        throws IOException {
                    final String resp = response.body().string();
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    WarehousesResult warehousesResult = gson.fromJson(resp, WarehousesResult.class);

                    if (warehousesResult != null) {
                        Log.d(TAG, "Result = " + warehousesResult.success + " length = " + warehousesResult.counter);
                        if (warehousesResult.counter > 0) {
                            /*
                            App.ids = new String[warehousesResult.counter];
                            App.storeCode = new String[warehousesResult.counter];
                            names = new String[warehousesResult.counter];
                            App.codeMap = new HashMap<>();
                            App.storeMap = new HashMap<>();
                            for (int i = 0; i < warehousesResult.counter; i++) {
                                App.storeCode[i] = warehousesResult.Warehouses[i].storeCode;
                                App.ids[i] = warehousesResult.Warehouses[i].id;
                                names[i] = warehousesResult.Warehouses[i].descr;
                                App.storeMap.put(warehousesResult.Warehouses[i].id,warehousesResult.Warehouses[i].descr);
                                App.codeMap.put(warehousesResult.Warehouses[i].id,warehousesResult.Warehouses[i].storeCode);
                            }

                             */
                            names = new String[warehousesResult.counter];
                            App.warehouses = warehousesResult.Warehouses;
                            for (int i = 0; i < warehousesResult.counter; i++) {
                                names[i] = warehousesResult.Warehouses[i].descr;
                            }
                            runOnUiThread(() -> {
                                FL.d(TAG, "Store names length = " + names.length);
                                adbSettings = new AlertDialog.Builder(ctx);
                                if (App.warehouse == null) {
                                    adbSettings.setTitle(R.string.store_choice)
                                            .setItems(names, (dialog, which) -> {
                                                FL.d(TAG, "Index = " + which + " store id=" + App.warehouses[which].id);
                                                App.setWarehouse(App.warehouses[which]);
                                                sf = (StartFragment) getSupportFragmentManager().findFragmentByTag(StartFragment.class.getSimpleName());
                                                if (sf != null) {
                                                    sf.setStore();
                                                }
                                                Database.clearGoods();
//                                                refreshData();
                                                refreshCells();
//                                                mViewModel.getGoodsData();
                                            }).create().show();
                                }
                            });
                        }
                    }

                }
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, e.getMessage());
                }
            });

        }
    }

    public class GetWSAllGoods {
        OkHttpClient client;
        String TAG = "GetWSAllGoods";
        void run(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Log.d(TAG, "GET url " + url);
            try {
                KeyStore keyStore = KeyStore.getInstance("PKCS12");
                InputStream clientCertificateContent = getResources().openRawResource(R.raw.terminal);
                keyStore.load(clientCertificateContent, "".toCharArray());
                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(keyStore, "".toCharArray());
                InputStream myTrustedCAFileContent = getResources().openRawResource(R.raw.chaincert);
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                X509Certificate myCAPublicKey = (X509Certificate) certificateFactory.generateCertificate(myTrustedCAFileContent);
                KeyStore trustedStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustedStore.load(null);
                trustedStore.setCertificateEntry(myCAPublicKey.getSubjectX500Principal().getName(), myCAPublicKey);
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(trustedStore);
                final TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                                                           String authType) throws
                                    CertificateException {
                            }

                            @Override
                            public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                                           String authType) throws
                                    CertificateException {
                            }

                            @Override
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return new java.security.cert.X509Certificate[]{};
                            }
                        }
                };
                final SSLContext sslContext = SSLContext.getInstance(SSL);
                sslContext.init(keyManagerFactory.getKeyManagers(), trustAllCerts, new java.security.SecureRandom());
                final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
                if (url.contains("https:")) {
                    client = new OkHttpClient.Builder().sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                            .hostnameVerifier(new HostnameVerifier() {
                                @Override
                                public boolean verify(String hostname, SSLSession session) {
                                    return true;
                                }
                            })
                            .build();
                } else client = new OkHttpClient();
            } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | UnrecoverableKeyException | KeyManagementException e) {
                e.printStackTrace();
            }

            Call call = client.newCall(request);
            showProgress(true);
            call.enqueue(new Callback() {
                public void onResponse(Call call, Response response)
                        throws IOException {
                    final String resp = response.body().string();
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    GoodsSet gs = gson.fromJson(resp, GoodsSet.class);
                    if (gs != null) {
                        Log.d(TAG, "Result = " + gs.success + " length = " + gs.counter);
                        if (gs.counter > 0) {
                            Database.beginTr();
                            for (int i = 0; i < gs.counter; i++) {
//                                Log.d(TAG, " " + gs.goodsRows[i].id + " " + gs.goodsRows[i].description);
                                Database.addAllGoods(
                                        gs.goodsRows[i].id,
                                        gs.goodsRows[i].description,
                                        gs.goodsRows[i].cell,
                                        gs.goodsRows[i].article,
                                        gs.goodsRows[i].qnt
                                );
                            }
                            Database.endTr();
                            FL.d(TAG, "Goods directory loaded");
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showProgress(false);
                        }
                    });
                }

                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showProgress(false);
                        }
                    });
                    Log.d(TAG, e.getMessage());
                }
            });
        }
    }
    private void showWaitBox(boolean show) {
        MainFragment mf = (MainFragment) getSupportFragmentManager().findFragmentByTag(MainFragment.class.getSimpleName());
        if (mf != null) {
            Log.d(TAG, MainFragment.class.getSimpleName() + " is on");
            mf.showWait(show);
        }
    }
    Runnable waitBox = () -> showWaitBox(!boxReady);
    private void refreshBox() {
        GetWSGoods getGoods = new GetWSGoods();
        try {
            goodsURL = getGoodsURL(App.getCurrentBox().id);
            getGoods.run(goodsURL);
            goodsRequest = true;
        } catch (IOException e) {
            FL.e(TAG, e.getMessage());
        }
    }
    class RefreshBoxTask extends TimerTask {
        @Override
        public void run() {
            refreshBox();
        }
    }
    class LoadGoodsTask extends AsyncTask<Void, Void, Void> {
        String TAG = "LoadGoodsTask";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            FL.d(TAG, "onPreExecute");
            if (mf != null) {
                runOnUiThread(mf.showProgress);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            LiveData<ArrayList<GoodsRow>> goodsList = mViewModel.getGoodsData();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            FL.d(TAG, "onPostExecute");
            if(mf != null) {
                runOnUiThread(mf.hideProgress);
            }
        }
    }
    public void loadGoodsData() {
        LoadGoodsTask loadGoodsTask = new LoadGoodsTask();
        loadGoodsTask.execute();
    }
    public void showProgress(boolean show) {
        Log.d(TAG,"showProgress(" + show + ") progressLevel " + progressLevel);
        if(show) {
            progressLevel++;
        } else {
            progressLevel--;
            if(progressLevel < 0) progressLevel = 0;
        }
        if (progressLevel > 0) {
            pbWait.setVisibility(View.VISIBLE);
        } else {
            pbWait.setVisibility(View.GONE);
        }
    }
    private boolean initScanUrovo() {
        boolean powerOn = false;
        try {
            mScanManager = new ScanManager();
            powerOn = mScanManager.getScannerState();
        } catch (Exception e) {
            Log.d (TAG, "No Urovo terminal\r\n" + e.getMessage());
            mScanManager = null;
        }
        return powerOn;
    }
    private void registerReceiverUrovo(boolean register) {
        if (register && mScanManager != null) {
            IntentFilter filter = new IntentFilter();
            int[] idbuf = new int[]{PropertyID.WEDGE_INTENT_ACTION_NAME, PropertyID.WEDGE_INTENT_DATA_STRING_TAG};
            String[] value_buf = mScanManager.getParameterString(idbuf);
            if (value_buf != null && value_buf[0] != null && !value_buf[0].equals("")) {
                filter.addAction(value_buf[0]);
            } else {
                filter.addAction(ACTION_DECODE);
            }
            filter.addAction(ACTION_CAPTURE_IMAGE);
            registerReceiver(mReceiver, filter);
        } else if (mScanManager != null) {
            mScanManager.stopDecode();
            unregisterReceiver(mReceiver);
        }
    }
    private static final String ACTION_DECODE = ScanManager.ACTION_DECODE;   // default action
    //    private static final String ACTION_DECODE_IMAGE_REQUEST = "action.scanner_capture_image";
    private static final String ACTION_CAPTURE_IMAGE = "scanner_capture_image_result";
    private static final String BARCODE_STRING_TAG = ScanManager.BARCODE_STRING_TAG;
    private static final String BARCODE_TYPE_TAG = ScanManager.BARCODE_TYPE_TAG;
    //    private static final String BARCODE_LENGTH_TAG = ScanManager.BARCODE_LENGTH_TAG;
//    private static final String DECODE_DATA_TAG = ScanManager.DECODE_DATA_TAG;
    private ScanManager mScanManager = null;
    //    private static Map<String, BarcodeHolder> mBarcodeMap = new HashMap<String, BarcodeHolder>();
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG,"onReceive , action:" + action);
            // Get scan results, including string and byte data etc.
            byte type = intent.getByteExtra(BARCODE_TYPE_TAG, (byte) 0);
            String barcodeStr = intent.getStringExtra(BARCODE_STRING_TAG);
            Log.i(TAG,"barcode type:" + type);
//            String scanResult = new String(barcode, 0, barcodeLen);
            String codeId = " ";
            if(type == 100) {
                if(barcodeStr.length() == 12) {
                    codeId = "c";   //upc
                    barcodeStr = barcodeStr.substring(0,11);
                } else if(barcodeStr.length() == 13) {
                    codeId = "d";   //ean13
                    barcodeStr = barcodeStr.substring(0,12);
                }
            }
            if(type == 98) codeId = "b";
//            processScan(barcodeStr, codeId );
            processScannedData(new ScannedCode(codeId, barcodeStr),App.state);
        }
    };
    public class GetWSPack {
        OkHttpClient client;
        String TAG = "GetWSPack";
        void run(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Log.d(TAG, "GET url " + url);
            WebserviceHTTPClient httpClient = new WebserviceHTTPClient(url);
            client = httpClient.getClient();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                public void onResponse(Call call, Response response)
                        throws IOException {
                    final String resp = response.body().string();
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    PackResult pack = gson.fromJson(resp, PackResult.class);
                    if (pack != null) {
                        Log.d(TAG, "Result = " + pack.success + " length = " + pack.counter);
                        if (pack.counter > 0) {
                            Database.beginTr();
                            for (int i = 0; i < pack.counter; i++) {
                                App.setCurrentPackId(pack.rows[i].packid);
                                App.setCurrentPackNum(pack.rows[i].packnum);
                                /*
                                Log.d(TAG, " " + gs.goodsRows[i].id + " " + gs.goodsRows[i].description + " " + gs.goodsRows[i].article + " " + gs.goodsRows[i].qnt
                                        + " " + gs.goodsRows[i].cell);

                                 */
                                Database.addPack(
                                        pack.rows[i].goods,
                                        pack.rows[i].goods_descr,
                                        pack.rows[i].mdoc,
                                        pack.rows[i].goods_article,
                                        pack.rows[i].qnt,
                                        pack.rows[i].packqnt,
                                        pack.rows[i].goods_brand,
                                        pack.rows[i].cell
                                );
                            }
                            Database.endTr();

                        }
//                        runOnUiThread(waitBox);
                    }
//                    goodsRequest = false;
                    mViewModel.loadGoodsData();
                }

                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, e.getMessage());
                }
            });
        }
    }
    public String getPackURL(String packId) {
        URI uri = null;
        try {
            uri = new URI(
                    Config.scheme, null, Config.ip, Config.port,
                    Config.packPath + "id/" + App.getStoreId() + "/" + packId + "/",
                    null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return uri.toASCIIString();
    }
    public String getPackNumURL(String packNum) {
        URI uri = null;
        try {
            uri = new URI(
                    Config.scheme, null, Config.ip, Config.port,
                    Config.packPath + "num/" + App.getStoreId() + "/" + packNum + "/",
                    null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return uri.toASCIIString();
    }
    public void uploadPack() {
        Log.d(TAG,"uploadPack()");
        uploadGoodsPosition(Database.goodsToUpload());
    }
    private void uploadGoodsPosition(GoodsPosition[] gpa) {
        try {
//            String scheme = "http", ip = "192.168.21.244";
//            int port = 8080;
            uri = new URI(
                    Config.scheme, null, Config.ip, Config.port,
                    Config.postGoodsPath + App.getStoreId() + "/" + App.getStoreMan() + "/",
                    null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        acceptedGoodsURL = uri.toASCIIString();
        if(gpa != null && gpa.length > 0) {
            GoodsResult gr = new GoodsResult(true, gpa.length);
            gr.Goods = gpa;
            gr.storeman = App.getStoreMan();
            postWS = new PostWebservice();
            postWS.type = POSTGOODS;
            Gson gson = new Gson();
            try {
                postWS.post(acceptedGoodsURL, gson.toJson(gr));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public class GetWSStoreman {
        OkHttpClient client;
        String TAG = "GetWSStoreman";
        void run(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Log.d(TAG, "GET url " + url);
            WebserviceHTTPClient httpClient = new WebserviceHTTPClient(url);
            client = httpClient.getClient();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                public void onResponse(Call call, Response response)
                        throws IOException {
                    final String resp = response.body().string();
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    Storeman storeman = gson.fromJson(resp, Storeman.class);
                    if (storeman != null) {
                        Log.d(TAG, "Storeman " + storeman.fullname + " num " + storeman.num + " id " + storeman.id);
                        App.setStoremanId(storeman.id);
                    }
                }
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, e.getMessage());
                }
            });
        }
    }
    public void getStoremanId(){
        try {
            uri = new URI(
                    Config.scheme, null, Config.ip, Config.port,
                    Config.storemanPath + App.getStoreMan() + "/",
                    null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        GetWSStoreman getWSStoreman = new GetWSStoreman();
        try {
            getWSStoreman.run(uri.toASCIIString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public class GetWSCamera {
        OkHttpClient client;
        String TAG = "GetWSCamera";
        void run(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Log.d(TAG, "GET url " + url);
            WebserviceHTTPClient httpClient = new WebserviceHTTPClient(url);
            client = httpClient.getClient();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                public void onResponse(Call call, Response response)
                        throws IOException {
                    final String resp = response.body().string();
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    Camera camera = gson.fromJson(resp, Camera.class);
                    if (camera != null) {
                        FL.d(TAG, "Camera " + camera.descr + " ip " + camera.ip);
                        App.setIpcam(camera.ip);
                    }
                }
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, e.getMessage());
                }
            });
        }
    }
    public void getCamera(int num){
        try {
            uri = new URI(
                    Config.scheme, null, Config.ip, Config.port,
                    Config.cameraPath + App.getStoreId() + "/" + num + "/",
                    null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        GetWSCamera getWSCamera = new GetWSCamera();
        try {
            getWSCamera.run(uri.toASCIIString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}