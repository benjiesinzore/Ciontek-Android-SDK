package test.apidemo.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;

public class ScanActivity extends Activity implements View.OnClickListener {

    private Button btnDisableScan, btnEnableScan, btnNormal, btnContinuous, btnStartScan, btnStopScan;

    private TextView tvMsg;

    private BroadcastReceiver mScanRecevier = null;

    public static final int ENCODE_MODE_UTF8 = 1;
    public static final int ENCODE_MODE_GBK = 2;
    public static final int ENCODE_MODE_NONE = 3;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_scan);

        btnDisableScan = (Button) findViewById(R.id.btnDisableScan);
        btnEnableScan = (Button) findViewById(R.id.btnEnableScan);
        btnNormal = (Button) findViewById(R.id.btnNormal);
        btnContinuous = (Button) findViewById(R.id.btnContinuous);
        btnStartScan = (Button) findViewById(R.id.btnStartScan);
//        btnStopScan = (Button) findViewById(R.id.btnStopScan);

        tvMsg = (TextView) findViewById(R.id.tvMsg);
        tvMsg.setMovementMethod(ScrollingMovementMethod.getInstance());

        btnDisableScan.setOnClickListener(ScanActivity.this);
        btnEnableScan.setOnClickListener(ScanActivity.this);
        btnNormal.setOnClickListener(ScanActivity.this);
        btnContinuous.setOnClickListener(ScanActivity.this);
        btnStartScan.setOnClickListener(ScanActivity.this);
//        btnStopScan.setOnClickListener(ScanActivity.this);

        mScanRecevier = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Log.e("Scan", "scan receive.......");

                String scanResult = "";
                int length = intent.getIntExtra("EXTRA_SCAN_LENGTH", 0);
                int encodeType = intent.getIntExtra("EXTRA_SCAN_ENCODE_MODE", 1);

                if (encodeType == ENCODE_MODE_NONE) {
                    byte[] data = intent.getByteArrayExtra("EXTRA_SCAN_DATA");
                    try {
                        scanResult = new String(data, 0, length, "iso-8859-1");//Encode charSet
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                } else {
                    scanResult = intent.getStringExtra("EXTRA_SCAN_DATA");
                }
//                final String  scanResultData=intent.getStringExtra("EXTRA_SCAN_DATA");
                tvMsg.setText("Scan Bar Code ：" + scanResult);
            }
        };

        IntentFilter filter = new IntentFilter("ACTION_BAR_SCAN");
        ScanActivity.this.registerReceiver(mScanRecevier, filter);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        //disable scan
        Intent intentDisScan = new Intent("ACTION_BAR_SCANCFG");
        intentDisScan.putExtra("EXTRA_SCAN_POWER", 0);
        ScanActivity.this.sendBroadcast(intentDisScan);

    }

    @Override
    protected void onResume() {
        super.onResume();

        disableFunctionLaunch(true);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //enable scan
        Intent intentEnableScan = new Intent("ACTION_BAR_SCANCFG");
        intentEnableScan.putExtra("EXTRA_SCAN_POWER", 1);
        ScanActivity.this.sendBroadcast(intentEnableScan);
    }

    @Override
    protected void onPause() {

        disableFunctionLaunch(false);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnEnableScan:

                tvMsg.setText("Open...");

                Intent intentEnableScan = new Intent("ACTION_BAR_SCANCFG");
                intentEnableScan.putExtra("EXTRA_SCAN_POWER", 1);
                ScanActivity.this.sendBroadcast(intentEnableScan);

                break;

            case R.id.btnDisableScan:

                tvMsg.setText("Close...");

                Intent intentDisScan = new Intent("ACTION_BAR_SCANCFG");
                intentDisScan.putExtra("EXTRA_SCAN_POWER", 0);
                ScanActivity.this.sendBroadcast(intentDisScan);

                break;

            case R.id.btnNormal:

                Intent intentNormal = new Intent("ACTION_BAR_SCANCFG");
                intentNormal.putExtra("EXTRA_TRIG_MODE", 0);
                tvMsg.setText("Set Scan: Normal Mode");
                ScanActivity.this.sendBroadcast(intentNormal);

                break;

            case R.id.btnContinuous:

                Intent intentContinuous = new Intent("ACTION_BAR_SCANCFG");
                intentContinuous.putExtra("EXTRA_TRIG_MODE", 1);
                tvMsg.setText("Set Scan: Continuous Mode");
                ScanActivity.this.sendBroadcast(intentContinuous);

                break;
            case R.id.btnStartScan:

                Intent startIntent = new Intent("ACTION_BAR_TRIGSCAN");
                startIntent.putExtra("timeout", 60);// Units per second,and Maximum 9
                tvMsg.setText("Start Scan...");

                ScanActivity.this.sendBroadcast(startIntent);

                break;
//            case R.id.btnStopScan:
//
//                Intent stopIntent = new Intent();
//                stopIntent.setAction("ACTION_BAR_TRIGSTOP");
//                tvMsg.setText("Stop Scan...");
//
//                ScanActivity.this.sendBroadcast(stopIntent);
//
//                break;
            default:
                //Set Scan Key
//                PosApiHelper.getInstance().SetKeyScanByLetfVolume(this,1);
//                PosApiHelper.getInstance().SetKeyScanByRightVolume(this,1);
//
//                PosApiHelper.getInstance().SetKeyScanByLetfVolume(this,0);
//                PosApiHelper.getInstance().SetKeyScanByRightVolume(this,0);
                break;
        }
    }


    // disable the power key when the device is boot from alarm but not ipo boot
    private static final String DISABLE_FUNCTION_LAUNCH_ACTION = "android.intent.action.DISABLE_FUNCTION_LAUNCH";
    private void disableFunctionLaunch(boolean state) {
        Intent disablePowerKeyIntent = new Intent(DISABLE_FUNCTION_LAUNCH_ACTION);
        if (state) {
            disablePowerKeyIntent.putExtra("state", true);
        } else {
            disablePowerKeyIntent.putExtra("state", false);
        }
        sendBroadcast(disablePowerKeyIntent);
    }

}
