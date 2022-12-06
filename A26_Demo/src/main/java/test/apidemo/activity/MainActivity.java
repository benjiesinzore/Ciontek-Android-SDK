package test.apidemo.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import vpos.apipackage.PosApiHelper;
import vpos.keypad.EMVCOHelper;

public class MainActivity extends Activity {

    Context mContext;

    //ITEM icc
    private static final int ITEM_CODE_ICC = 0;
    //ITEM nfc
    private static final int ITEM_CODE_NFC = 1;
    //ITEM mcr
    private static final int ITEM_CODE_MCR = 2;
    //ITEM pci
    private static final int ITEM_CODE_PCIDUKPT = 3;
    //ITEM print
    private static final int ITEM_CODE_PRINT = 4;
    //ITEM sys
    private static final int ITEM_CODE_SYS = 5;
    //ITEM Scan
    private static final int ITEM_CODE_SCAN = 6;
    //update
    private static final int ITEM_CODE_UPDATE_OS = 7;
    //emv
    private static final int ITEM_CODE_EMV = 8;

    private static final int ITEM_CODE_PCIMKSK = 9;

    // Used to load the 'native-lib' library on application startup.

    private GridMenuLayout mGridMenuLayout;

    public static String[] MY_PERMISSIONS = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
//            "android.permission.MOUNT_UNMOUNT_FILESYSTEMS",
            Manifest.permission.READ_PHONE_STATE
    };

    public static final int REQUEST_EXTERNAL_PERMISSION = 1;

    EMVCOHelper emvcoHelper = EMVCOHelper.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Determine if the current Android version is >=23
        // 判断Android版本是否大于23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermission();
        } else {
            initViews();
        }
       PosApiHelper.getInstance().SysLogSwitch(1);
     //  int ret = emvcoHelper.AdapterUartBaud();

     //   Log.e("Robert MainActivity", "onCreate ret= "+ret);
    }


    /**
     * @Description: Request permission
     * 申请权限
     */
    private void requestPermission() {
        //检测是否有写的权限
        //Check if there is write permission
        int checkCallPhonePermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
            // 没有写文件的权限，去申请读写文件的权限，系统会弹出权限许可对话框
            //Without the permission to Write, to apply for the permission to Read and Write, the system will pop up the permission dialog
            ActivityCompat.requestPermissions(MainActivity.this, MY_PERMISSIONS, REQUEST_EXTERNAL_PERMISSION);
        } else {
            initViews();
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
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_EXTERNAL_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initViews();
            } else {
//                Toast.makeText(MainActivity.this,R.string.title_permission,Toast.LENGTH_SHORT).show();
                requestPermission();
            }
        }

    }

    public static Drawable tintDrawable(Drawable drawable, ColorStateList colors) {
        final Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTintList(wrappedDrawable, colors);
        return wrappedDrawable;
    }

    private void initViews() {


        setContentView(R.layout.main);

        mContext = MainActivity.this;
        final Drawable[] itemImgs = {
                getResources().getDrawable(R.mipmap.icc),
                getResources().getDrawable(R.mipmap.nfc),
                getResources().getDrawable(R.mipmap.mcr),
                getResources().getDrawable(R.mipmap.pci),
                getResources().getDrawable(R.mipmap.print),
                getResources().getDrawable(R.mipmap.sys),
                getResources().getDrawable(R.mipmap.scan),
                getResources().getDrawable(R.mipmap.upgrade)
                , getResources().getDrawable(R.mipmap.emv)
                , getResources().getDrawable(R.mipmap.more)
        };

        final String[] itemTitles = {
                getString(R.string.icc)
                , getString(R.string.picc)
                , getString(R.string.mcr)
                , getString(R.string.pcidukpt)
                , getString(R.string.print)
                , getString(R.string.sys)
                , getString(R.string.scan)
                , getString(R.string.upgrade_os)
                , " Emv "
                , getString(R.string.PCI_MKSK)
        };

        final int sizeWidth = getResources().getDisplayMetrics().widthPixels / 25;

        mGridMenuLayout = (GridMenuLayout) findViewById(R.id.myGrid);
        mGridMenuLayout.setGridAdapter(new GridMenuLayout.GridAdapter() {

            @Override
            public View getView(int index) {
                View view = getLayoutInflater().inflate(R.layout.gridmenu_item, null);
                ImageView gridItemImg = (ImageView) view.findViewById(R.id.gridItemImg);
                TextView gridItemTxt = (TextView) view.findViewById(R.id.gridItemTxt);

                gridItemImg.setImageDrawable(tintDrawable(itemImgs[index], mContext.getResources().getColorStateList(R.color.item_image_select)));

                gridItemTxt.setText(itemTitles[index]);
                gridItemTxt.setTextSize(sizeWidth);

                return view;
            }

            @Override
            public int getCount() {
                return itemTitles.length;
            }
        });

        mGridMenuLayout.setOnItemClickListener(new GridMenuLayout.OnItemClickListener() {

            @SuppressLint("NewApi")
            @TargetApi(Build.VERSION_CODES.M)
            public void onItemClick(View v, int index) {
                switch (index) {
                    case ITEM_CODE_ICC:
                        Intent iccIntent = new Intent(MainActivity.this, IccActivity.class);
                        startActivity(iccIntent);
                        break;
                    case ITEM_CODE_NFC:
                        Intent nfcIntent = new Intent(MainActivity.this, PiccActivity.class);
                        startActivity(nfcIntent);
                        break;
                    case ITEM_CODE_MCR:
                        Intent mcrIntent = new Intent(MainActivity.this, McrActivity.class);
                        startActivity(mcrIntent);
                        break;
                    case ITEM_CODE_PCIDUKPT:
                        Intent pciIntent = new Intent(MainActivity.this, PciDukptActivity.class);
                        startActivity(pciIntent);
                        break;
                    case ITEM_CODE_PRINT:
                        Intent printIntent = new Intent(MainActivity.this, PrintActivity.class);
                        startActivity(printIntent);
                        break;
                    case ITEM_CODE_SYS:
                        Intent sysIntent = new Intent(MainActivity.this, SysActivity.class);
                        startActivity(sysIntent);
                        break;
                    case ITEM_CODE_SCAN:
                        Intent scanIntent = new Intent(MainActivity.this, ScanActivity.class);
                        startActivity(scanIntent);
                        break;
                    case ITEM_CODE_UPDATE_OS:

                        new AlertDialog.Builder(mContext)
                                .setTitle(getResources().getString(R.string.upgrade_os))
                                .setMessage(R.string.upgradeTips)
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();

                                        Intent osIntent = new Intent(MainActivity.this, UpgradeOsActivity.class);
                                        startActivity(osIntent);
                                    }
                                })
                                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();


                        break;
                    case ITEM_CODE_EMV:
                       Intent emvIntent = new Intent(MainActivity.this, EmvTestActivity.class);
                        startActivity(emvIntent);
                        break;

                    case ITEM_CODE_PCIMKSK:
                        Intent pcimkskIntent = new Intent(MainActivity.this, PciMkSkActivity.class);
                        startActivity(pcimkskIntent);
                        break;

                    default:

                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                            MainActivity.this.requestPermissions(new String[] {Manifest.permission.READ_PHONE_STATE},2);
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                        }
                        String deviceId = ((TelephonyManager) MainActivity.this.getSystemService(TELEPHONY_SERVICE)).getDeviceId();
                        Log.e("liuhao", "-------> IMEI:" + deviceId);
                        Toast.makeText(MainActivity.this,deviceId,Toast.LENGTH_SHORT).show();
                        /*
                        String path = getStoragePath(getApplicationContext(), true) + "/write.txt";
                        try {
                            File file = new File(path);
                            //file.createNewFile();
                            Log.e("liuhao write",file.getAbsolutePath());

                            FileOutputStream fos=new FileOutputStream(file);
                            fos.write("abcdefghijklmn".getBytes());
                            fos.close();

                            Log.e("liuhao write","end");

                        } catch (IOException e) {
                            Log.e("liuhao write",e.getMessage());
                            e.printStackTrace();
                        }
                        */
                        break;
                }
            }
        });
    }

//    public static final String getIMEI(Context context) {
//        try {
//            //实例化TelephonyManager对象
//            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//            //获取IMEI号
//            String imei = telephonyManager.getDeviceId();
//            //在次做个验证，也不是什么时候都能获取到的啊
//            if (imei == null) {
//                imei = "";
//            }
//            return imei;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "";
//        }
//    }

    /**
     * @param mContext
     * @param is_removale
     * @return
     * @Description : 获取内置存储设备 或 外置SD卡的路径
     * Get path : the built-in storage device or external SD card path.
     */
    private static String getStoragePath(Context mContext, boolean is_removale) {

        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (is_removale == removable) {
                    return path;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
