package test.apidemo.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import vpos.apipackage.ByteUtil;
import vpos.apipackage.PosApiHelper;

/**
 * Created by Administrator on 2017/8/17.
 */

public class SysActivity extends Activity implements OnClickListener {

    public static final int OPCODE_SET_SN = 0;
    public static final int OPCODE_GET_SN = 1;
    public static final int OPCODE_BEEP_TEST = 2;
    public static final int OPCODE_GET_CHIP_ID = 3;

    public static String[] MY_PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.MOUNT_UNMOUNT_FILESYSTEMS"};

    public static final int REQUEST_EXTERNAL_STORAGE = 1;


    private final String TAG = "SysActivity";

    byte SN[] = new byte[32];
    String snString = "";
    byte version[] = new byte[9];

    EditText editSn = null;
    TextView tvMsg = null;

    Button btnSetSN, btnGetSN, btnGetChipID, btnBeep, btnVersion, btnUpdate;

    int ret = 0;

    PosApiHelper posApiHelper = PosApiHelper.getInstance();

    Handler handler =  new Handler(){

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_sys);

        tvMsg = (TextView) findViewById(R.id.textview);
//        tvMsg.setMovementMethod(new ScrollingMovementMethod());

        editSn = (EditText) findViewById(R.id.editSn);

        btnSetSN = (Button) findViewById(R.id.btnSetSN);
        btnGetSN = (Button) findViewById(R.id.btnGetSN);
        btnGetChipID = (Button) findViewById(R.id.btnGetChipID);
        btnBeep = (Button) findViewById(R.id.btnBeep);
        btnVersion = (Button) findViewById(R.id.btnVersion);
        btnUpdate = (Button) findViewById(R.id.btnUpdate);

        btnSetSN.setOnClickListener(this);
        btnGetSN.setOnClickListener(this);
        btnGetChipID.setOnClickListener(this);
        btnBeep.setOnClickListener(this);
        btnVersion.setOnClickListener(this);
        btnUpdate.setOnClickListener(this);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            findViewById(R.id.btnGetOsVer).setVisibility(View.GONE);
            findViewById(R.id.btnMcuVer).setVisibility(View.GONE);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        synchronized (this) {
            try {
                startTestLed(1, 0);
                Thread.sleep(20);
                startTestLed(2, 0);
                Thread.sleep(20);
                startTestLed(3, 0);
                Thread.sleep(20);
                startTestLed(4, 0);
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    protected void onResume() {
        // TODO Auto-generated method stub
        disableFunctionLaunch(true);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onResume();

        Toast.makeText(SysActivity.this,posApiHelper.getAARVersion(),Toast.LENGTH_SHORT).show();
        // isQuit = false;
    }

    @Override
    protected void onPause() {
        disableFunctionLaunch(false);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onPause();
//        isQuit = true;
    }

    ProgressDialog mcuPowerDlg = null;

    public void OnClickMcuOn(View view){
        disableFunctionLaunch(true);
//        mcuPowerDlg = ProgressDialog.show(this, null, getString(R.string.isUpdating), false, false);

//        new Thread()
        int ret = posApiHelper.SetMcuPowerMode(1);
        if (ret == 0){
            tvMsg.setText("MCU ON success~");
        }else {
            tvMsg.setText("MCU ON failed~ ret = " +ret);
        }
    }

    public void OnClickMcuOff(View view){
        int ret =posApiHelper.SetMcuPowerMode(0);
        if (ret == 0){
            tvMsg.setText("MCU OFF success~");
        }else {
            tvMsg.setText("MCU OFF failed~ ret = " +ret);
        }
    }

    public void OnClickMcuVer(View view) {
        tvMsg.setText("Mcu Target Version :" + posApiHelper.getMcuTargetVersion(SysActivity.this));
    }

    public void OnClickOsVer(View view) {
        tvMsg.setText("OS Version :" + posApiHelper.getOSVersion(SysActivity.this));
    }

    public void OnClickLED1Open(View view) {
        startTestLed(1, 1);
    }

    public void OnClickLED2Open(View view) {
        startTestLed(2, 1);
    }

    public void OnClickLED3Open(View view) {
        startTestLed(3, 1);
    }

    public void OnClickLED4Open(View view) {
        startTestLed(4, 1);
    }

    public void OnClickLED1Close(View view) {
        startTestLed(1, 0);
    }

    public void OnClickLED2Close(View view) {
        startTestLed(2, 0);
    }

    public void OnClickLED3Close(View view) {
        startTestLed(3, 0);
    }

    public void OnClickLED4Close(View view) {
        startTestLed(4, 0);
    }

    void startTestSys(int OpCode) {
        switch (OpCode) {
            case OPCODE_BEEP_TEST:
                tvMsg.setText("Test Beep...");
                ret = posApiHelper.SysBeep();
             //   ret = posApiHelper.SysSetLedMode(6, 0);
            //    tvMsg.setText("PSAM1: " + ret);
                break;
            case OPCODE_GET_SN:
                tvMsg.setText("Get SN...");

                ret = posApiHelper.SysReadSN(SN);
                if (ret == 0) {
                    //tvMsg.setText("Read SN Success: " + new String(SN,ByteUtil.returnActualLength(SN)).trim());
                    tvMsg.setText("Read SN Success: " + ByteUtil.bytesToString(SN));
                } else {
                    tvMsg.setText("Read SN Failed");
                }
                break;
            case OPCODE_SET_SN:
                tvMsg.setText("Set SN...");
                snString = editSn.getText().toString();
                ret = posApiHelper.SysWriteSN(snString.getBytes());
                if (ret == 0) {
                    tvMsg.setText("Write SN Success\n" + "setSN : " + snString);
                } else {
                    tvMsg.setText("Write SN Failed");
                }
                break;
            case OPCODE_GET_CHIP_ID:
                byte chipIdBuf[] = new byte[16];
                ret = posApiHelper.SysReadChipID(chipIdBuf, 16);
                if (ret == 0) {
                    tvMsg.setText("Read ChipID Success: " + ByteUtil.bytearrayToHexString(chipIdBuf, 16));
                } else {
                    tvMsg.setText("Read ChipID Failed");
                }
                break;
            default:
                break;
        }
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void getSysVersionInfo() {
        ret = posApiHelper.SysGetVersion(version);
        Log.e(TAG, "getSysVersionInfo ret = " + ret);
        if (ret == 0) {
            if (version[6] == -1 && version[7] == -1 && version[8] == -1) {
                tvMsg.setText("Security SP Version: A26-" + version[0] + "." + version[1] + "." + version[2] +
                        "\nLib Version: V" + version[3] + "." + version[4] + "." + version[5] +
                        "\nSecurity Boot Version: NULL" + "\nSucceed"
                );
            } else {
                tvMsg.setText("Security App Version: V" + version[0] + "." + version[1] + "." + version[2] +
                        "\nLib Version: V" + version[3] + "." + version[4] + "." + version[5] +
                        "\nSecurity Boot Version: V" + version[6] + "." + version[7] + "." + version[8]
                        + "\nSucceed");
            }

        } else {
            tvMsg.setText("Get_Version Failed");
        }


    }


    private void restartApp() {

        disableFunctionLaunch(false);
        android.os.Process.killProcess(android.os.Process.myPid());
//        android.os.Process.killProcess(android.os.Process.myPid());
//        Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
//        PendingIntent restartIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
//        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent); // 1秒钟后重启应用
//        System.exit(0);
    }

    ProgressDialog updateDlg = null;

    private void startUpdate() {
        Log.e(TAG, "startUpdate  ........ 00");

        disableFunctionLaunch(true);
        updateDlg = ProgressDialog.show(this, null, getString(R.string.isUpdating), false, false);
        new Thread() {
            @Override
            public void run() {
                super.run();
                int ret = posApiHelper.SysUpdate();
                Log.e(TAG, "SysUpdate ret = " + ret);
                if (ret == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateDlg.cancel();
                            //升级成功 重启应用
                            tvMsg.setText(R.string.update_finish);
                        }
                    });

                    new Thread() {
                        public void run() {
                            try {
                                sleep(2000);
                                restartApp();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateDlg.cancel();
                            tvMsg.setText(R.string.update_fail);
                        }
                    });
                }
            }
        }.start();
    }

    /**
     * @Description: Request permission
     * 申请权限
     */
    private void requestPermission() {
        //检测是否有写的权限
        //Check if there is write permission
        int checkCallPhonePermission = ContextCompat.checkSelfPermission(SysActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
            // 没有写文件的权限，去申请读写文件的权限，系统会弹出权限许可对话框
            //Without the permission to Write, to apply for the permission to Read and Write, the system will pop up the permission dialog
            ActivityCompat.requestPermissions(SysActivity.this, MY_PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        } else {
            updateMcu();
        }
    }

    /**
     * a callback for request permission
     * 注册权限申请回调
     *
     * @param requestCode  申请码
     * @param permissions  申请的权限
     * @param grantResults 结果
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateMcu();
            }
        }
    }

    private void updateMcu() {

        tvMsg.setText("Update...");

        File file = null , file1 = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            file = new File("/storage/emulated/0/Download/MAXQ3255X_App.bin");
            file1 = new File("/storage/emulated/0/MAXQ3255X_App.bin");
        } else {
            file = new File("/storage/sdcard0/Download/MAXQ3255X_App.bin");
            file1 = new File("/storage/sdcard0/MAXQ3255X_App.bin");
        }

        if (!file.exists()&&(!file1.exists())) {
            Toast.makeText(getApplicationContext(), getString(R.string.file_not_found), Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this).setTitle(R.string.update)
                .setMessage(R.string.update_or_not)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startUpdate();
                        dialog.cancel();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                dialog.cancel();
            }
        }).show();


    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSetSN:
                startTestSys(OPCODE_SET_SN);
                break;
            case R.id.btnGetSN:
                startTestSys(OPCODE_GET_SN);
                break;
            case R.id.btnGetChipID:
                startTestSys(OPCODE_GET_CHIP_ID);
                break;
            case R.id.btnBeep:
                startTestSys(OPCODE_BEEP_TEST);
                break;
            case R.id.btnVersion:
                getSysVersionInfo();
                break;
            case R.id.btnUpdate:
                //Determine if the current Android version is >=23
                // 判断Android版本是否大于23
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermission();
                } else {
                    updateMcu();
                }
                break;
        }
    }

    /*
     * @Date 2017.12.01
     * @Description： get the GPS
     */

    private LocationManager locationManager;
    private double latitude = 0.0;
    private double longitude = 0.0;

    public void OnClickGps(View view) {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            getLocation();
            //gps has open
        } else {
            toggleGPS();
            new Handler() {}.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getLocation();
                }
            }, 2000);

        }
    }

    private void toggleGPS() {
        Intent gpsIntent = new Intent();
        gpsIntent.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
        gpsIntent.addCategory("android.intent.category.ALTERNATIVE");
        gpsIntent.setData(Uri.parse("custom:3"));
        try {
            PendingIntent.getBroadcast(this, 0, gpsIntent, 0).send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
            Location location1 = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location1 != null) {
                latitude = location1.getLatitude();
                longitude = location1.getLongitude();
            }
        }
    }

    /*
     *@Description: getLocation info
     */
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        } else {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
        }
        tvMsg.setText("Latitude ：" + latitude + "\n" + "Longitude ：" + longitude);
    }

    LocationListener locationListener = new LocationListener() {
        // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
        /*
         *The state of the Provider triggers this function when it is available, temporarily unavailable,
         * and no service three states to switch directly
         */
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        // Provider被enable时触发此函数，比如GPS被打开
        /*
         * The Provider triggers this function when it is enabled,
         * such as the GPS being opened
         */
        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, provider);
        }

        // Provider被disable时触发此函数，比如GPS被关闭
        /*
         *This function is triggered when the Provider is disabled,
         * such as the shutdown of the GPS
         */
        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, provider);
        }

        // 当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
        /*
        This function is triggered when the coordinate changes,
        and if the Provider passes in the same coordinate, it will not be triggered
         */
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                Log.e("Map", "Location changed : Lat: " + location.getLatitude() + " Lng: " + location.getLongitude());
                latitude = location.getLatitude(); // 经度
                longitude = location.getLongitude(); // 纬度
            }
        }
    };


    /*
     * @Date : 20171201
     * @Description : Setting LED state
     * 1 - > open led
     * 0 - > close led
     */
    private void startTestLed(final int testCode, final int mode) {
        tvMsg.setText("LED Test");

        new Thread() {
            @Override
            public void run() {
                super.run();
                ret = posApiHelper.SysSetLedMode(testCode, mode);
                final String txt = mode == 1 ? "Open" : "Close";
                if (ret == 0) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            tvMsg.setText("LED" + testCode + " " + txt + " Succeed");
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            tvMsg.setText("LED" + testCode + " " + txt + " Failed");
                        }
                    });
                }
            }
        }.start();
    }



    // disable the power key when the device is boot from alarm but not ipo boot
    private static final String DISABLE_FUNCTION_LAUNCH_ACTION = "android.intent.action.DISABLE_FUNCTION_LAUNCH";

    private void disableFunctionLaunch(boolean state) {
        Intent disablePowerKeyIntent = new Intent(
                DISABLE_FUNCTION_LAUNCH_ACTION);
        if (state) {
            disablePowerKeyIntent.putExtra("state", true);
        } else {
            disablePowerKeyIntent.putExtra("state", false);
        }
        sendBroadcast(disablePowerKeyIntent);
    }
}
