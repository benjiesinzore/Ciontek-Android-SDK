package test.apidemo.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;

import java.util.Timer;

import test.apidemo.service.MyService;
import vpos.apipackage.PosApiHelper;
import vpos.apipackage.Print;
import vpos.apipackage.PrintInitException;

/**
 * Created by Administrator on 2017/8/17.
 */

public class PrintActivity extends Activity {

    public String tag = "PrintActivity-Robert2";

    final int PRINT_TEST = 0;
    final int PRINT_UNICODE = 1;
    final int PRINT_BMP = 2;
    final int PRINT_BARCODE = 4;
    final int PRINT_CYCLE = 5;
    final int PRINT_LONGER = 7;
    final int PRINT_OPEN = 8;

    private RadioGroup rg = null;
    private Timer timer;
    private Timer timer2;
    private BroadcastReceiver receiver;
    private IntentFilter filter;
    private int voltage_level;
    private int BatteryV;
    SharedPreferences preferences;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    private RadioButton rb_high;
    private RadioButton rb_middle;
    private RadioButton rb_low;
    private RadioButton radioButton_4;
    private RadioButton radioButton_5;
    private Button gb_test;
    private Button gb_unicode;
    private Button gb_barcode;
    private Button btnBmp;
    private final static int ENABLE_RG = 10;
    private final static int DISABLE_RG = 11;

    TextView textViewMsg = null;
    TextView textViewGray = null;
    int ret = -1;
    private boolean m_bThreadFinished = true;

    private boolean is_cycle = false;
    private int cycle_num = 0;

    private int RESULT_CODE = 0;
    //private Pos pos;

    int IsWorking = 0;


    PosApiHelper posApiHelper = PosApiHelper.getInstance();

    Intent mPrintServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_print);
        //linearLayout = (LinearLayout) this.findViewById(R.id.widget_layout_print);
        textViewMsg = (TextView) this.findViewById(R.id.textView_msg);
        textViewGray = (TextView) this.findViewById(R.id.textview_Gray);
        rg = (RadioGroup) this.findViewById(R.id.rg_Gray_type);
        rb_high = (RadioButton) findViewById(R.id.RadioButton_high);
        rb_middle = (RadioButton) findViewById(R.id.RadioButton_middle);
        rb_low = (RadioButton) findViewById(R.id.radioButton_low);
        radioButton_4 = (RadioButton) findViewById(R.id.radioButton_4);
        radioButton_5 = (RadioButton) findViewById(R.id.radioButton_5);
        gb_test = (Button) findViewById(R.id.button_test);
        gb_unicode = (Button) findViewById(R.id.button_unicode);
        gb_barcode = (Button) findViewById(R.id.button_barcode);
        btnBmp = (Button) findViewById(R.id.btnBmp);
        //gb_printCycle = (Button) findViewById(R.id.printCycle);

        init_Gray();

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {

                if (printThread != null && !printThread.isThreadFinished()) {

                    Log.e(tag, "Thread is still running...");
                    return;
                }

                String strGray=getResources().getString(R.string.selectGray);

                switch (checkedId) {
                    case R.id.radioButton_low:
                        textViewGray.setText(strGray+"3");
                        posApiHelper.PrintSetGray(3);
                        setValue(3);

                        break;
                    case R.id.RadioButton_middle:
                        textViewGray.setText(strGray+"2");
                        posApiHelper.PrintSetGray(2);
                        setValue(2);

                        break;
                    case R.id.RadioButton_high:
                        textViewGray.setText(strGray+"1");
                        posApiHelper.PrintSetGray(1);
                        setValue(1);
                        break;

                    case R.id.radioButton_4:
                        textViewGray.setText(strGray+"4");
                        posApiHelper.PrintSetGray(4);
                        setValue(4);
                        break;
                    case R.id.radioButton_5:
                        textViewGray.setText(strGray+"5");
                        posApiHelper.PrintSetGray(5);
                        setValue(5);
                        break;
                }
            }
        });
    }

    private void setValue(int val) {
        sp = getSharedPreferences("Gray", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("value", val);
        editor.commit();
    }

    private int getValue() {
        sp = getSharedPreferences("Gray", MODE_PRIVATE);
        int value = sp.getInt("value", 2);
        return value;
    }

    private void init_Gray() {
        int flag = getValue();
        posApiHelper.PrintSetGray(flag);

        String strGray=getResources().getString(R.string.selectGray);

        if (flag == 3) {
            rb_low.setChecked(true);
            textViewGray.setText(strGray+"3");
        }else if(flag == 2){
            rb_middle.setChecked(true);
            textViewGray.setText(strGray+"2");
        }else if(flag == 1){
            rb_high.setChecked(true);
            textViewGray.setText(strGray+"1");
        }else if(flag == 4){
            radioButton_4.setChecked(true);
            textViewGray.setText(strGray+"4");
        }else if(flag == 5){
            radioButton_5.setChecked(true);
            textViewGray.setText(strGray+"5");
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub

        disableFunctionLaunch(true);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onResume();
        filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        receiver = new BatteryReceiver();
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        disableFunctionLaunch(false);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onPause();
        QuitHandler();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        stopService(mPrintServiceIntent);
//        unbindService(serviceConnection);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("onKeyDown", "keyCode = " + keyCode);

//		if (keyCode == event.KEYCODE_BACK) {
//			if(m_bThreadFinished  == false)
//				return false;
//		}

        Log.d("ROBERT2 onKeyDown", "keyCode = " + keyCode);
        Log.d("ROBERT2 onKeyDown", "IsWorking== " + IsWorking);
        if (keyCode == event.KEYCODE_BACK) {
            if (IsWorking == 1)
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void onClickTest(View v) {
        if (printThread != null && !printThread.isThreadFinished()) {
            Log.e(tag, "Thread is still running...");
            return;
        }

        printThread = new Print_Thread(PRINT_TEST);
        printThread.start();
    }

    public void onClickUnicodeTest(View v) {
        if (printThread != null && !printThread.isThreadFinished()) {
            Log.e(tag, "Thread is still running...");
            return;
        }

        printThread = new Print_Thread(PRINT_UNICODE);
        printThread.start();

    }

    public void OnClickBarcode(View view) {
        if (printThread != null && !printThread.isThreadFinished()) {
            Log.e(tag, "Thread is still running...");
            return;
        }

        printThread = new Print_Thread(PRINT_BARCODE);
        printThread.start();
    }

    public void onClickBmp(View view) {
        if (printThread != null && !printThread.isThreadFinished()) {
            Log.e(tag, "Thread is still running...");
            return;
        }

        printThread = new Print_Thread(PRINT_BMP);
        printThread.start();

    }


    public void onClickCycle(View v) {
        if (printThread != null && !printThread.isThreadFinished()) {
            Log.e(tag, "Thread is still running...");
            return;
        }

        if (is_cycle == false) {
            is_cycle = true;
            preferences = getSharedPreferences("count", MODE_WORLD_READABLE);
            cycle_num = preferences.getInt("count", 0);
            SendMsg("total cycle num =" + cycle_num);
            Log.e(tag, "Thread is still 3000ms...");
            handlers.postDelayed(runnable, 3000);

        }
    }


    public void onClickClean(View v) {
        textViewMsg.setText("");
        preferences = getSharedPreferences("count", MODE_WORLD_READABLE);
        cycle_num = preferences.getInt("count", 0);
        editor = preferences.edit();
        cycle_num = 0;
        editor.putInt("count", cycle_num);
        editor.commit();
        QuitHandler();
    }

    public void onClickPrnOpen(View v) {
        if (printThread != null && !printThread.isThreadFinished()) {
            Log.e(tag, "Thread is still running...");
            return;
        }

        printThread = new Print_Thread(PRINT_OPEN);
        printThread.start();

    }

    public void onClickLong(View v) {

        if (printThread != null && !printThread.isThreadFinished()) {
            Log.e(tag, "Thread is still running...");
            return;
        }
        printThread = new Print_Thread(PRINT_LONGER);
        printThread.start();

//        if (timer != null) {
//            timer.cancel();
//            // 一定设置为null，否则定时器不会被回收
//            timer = null;
//        }
//
//        if (timer2 != null) {
//            timer.cancel();
//            // 一定设置为null，否则定时器不会被回收
//            timer2 = null;
//        }
//        //  wakeLock.release();
//
//
//        //if(m_bThreadFinished)
//        QuitHandler();
//        finish();
    }


    public void QuitHandler() {
        is_cycle = false;
        gb_test.setEnabled(true);
        gb_barcode.setEnabled(true);
        btnBmp.setEnabled(true);
        gb_unicode.setEnabled(true);
        handlers.removeCallbacks(runnable);
    }


    Handler handlers = new Handler();
    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub

            Log.e(tag, "TIMER log...");
            printThread = new Print_Thread(PRINT_UNICODE);
            printThread.start();

            Log.e(tag, "TIMER log2...");
            if (RESULT_CODE == 0) {
                editor = preferences.edit();
                editor.putInt("count", ++cycle_num);
                editor.commit();
                Log.e(tag, "cycle num=" + cycle_num);
                SendMsg("cycle num =" + cycle_num);
            }
            handlers.postDelayed(this, 9000);

        }
    };

    Print_Thread printThread = null;

    public class Print_Thread extends Thread {

        String content = "1234567890";
        int type;

        public boolean isThreadFinished() {
            return m_bThreadFinished;
        }

        public Print_Thread(int type) {
            this.type = type;
        }

        public void run() {
            Log.d("Robert2", "Print_Thread[ run ] run() begin");
            Message msg = Message.obtain();
            Message msg1 = new Message();

            synchronized (this) {

                m_bThreadFinished = false;
                try {
                    ret = posApiHelper.PrintInit();
                } catch (PrintInitException e) {
                    e.printStackTrace();
                    int initRet = e.getExceptionCode();
                    Log.e(tag, "initRer : " + initRet);
                }

                Log.e(tag, "init code:" + ret);

                ret = getValue();
                Log.e(tag, "getValue():" + ret);

                posApiHelper.PrintSetGray(ret);
                Log.e(tag, "PrintSetGray():" );

                {
                    RESULT_CODE = 0;
                }


                Log.d("Robert2", "Lib_PrnStart type= "+type );
                switch (type) {

                    case PRINT_LONGER:
                        SendMsg("PRINT LONG");

                        msg.what = DISABLE_RG;
                        handler.sendMessage(msg);

                        String stringg = "Hello Benjamin Sinzore?";

                        posApiHelper.PrintSetFont((byte) 24, (byte) 24, (byte) 0x00);


                        posApiHelper.PrintStr(stringg);


                        posApiHelper.PrintBarcode(content, 360, 120, BarcodeFormat.CODE_128);
                        posApiHelper.PrintStr("CODE_128 : " + content + "\n\n");
                        posApiHelper.PrintBarcode(content, 240, 240, BarcodeFormat.QR_CODE);
                        posApiHelper.PrintStr("QR_CODE : " + content + "\n\n");
                        posApiHelper.PrintStr("发卡行(ISSUER):01020001 工商银行\n");
                        posApiHelper.PrintStr("卡号(CARD NO):\n");
                        posApiHelper.PrintStr("    9558803602109503920\n");
                        posApiHelper.PrintStr("收单行(ACQUIRER):03050011民生银行\n");
                        posApiHelper.PrintStr("交易类型(TXN. TYPE):消费/SALE\n");
                        posApiHelper.PrintStr("卡有效期(EXP. DATE):2013/08\n");
                        posApiHelper.PrintStr("- - - - - - - - - - - - - - - -\n");
                        posApiHelper.PrintStr("                                         ");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("\n");

                        SendMsg("Printing... ");
                        final long starttime_long = System.currentTimeMillis();

                        ret = posApiHelper.PrintStart();

                        Log.e(tag, "PrintStart ret = " + ret);

                        msg1.what = ENABLE_RG;
                        handler.sendMessage(msg1);

                        if (ret != 0) {
                            RESULT_CODE = -1;
                            Log.e("liuhao", "Lib_PrnStart fail, ret = " + ret);
                            if (ret == -1) {
                                SendMsg("No Print Paper ");
                            } else if(ret == -2) {
                                SendMsg("too hot ");
                            }else if(ret == -3) {
                                SendMsg("low voltage ");
                            }else{
                                SendMsg("Print fail ");
                            }
                        } else {
                            RESULT_CODE = 0;
                            SendMsg("Print Finish ");
                        }
                        final long endttime_long = System.currentTimeMillis();

                        final long totaltime_long = starttime_long - endttime_long;
                        SendMsg("Print Long Totaltimie " + totaltime_long +"Ms");
                        break;

                    case PRINT_TEST:
                        Log.d("Robert2", "Lib_PrnStart ret START0 " );
                        SendMsg("PRINT_TEST");
                        msg.what = DISABLE_RG;
                        handler.sendMessage(msg);
                        posApiHelper.PrintSetFont((byte) 24, (byte) 24, (byte) 0x00);

                        posApiHelper.PrintStr("中文:你好，好久不见。\n");
                        posApiHelper.PrintStr("英语:Hello, Long time no see   ￡ ：2089.22\n");
                        posApiHelper.PrintStr("意大利语Italian :Ciao, non CI vediamo da Molto Tempo.\n");
                        posApiHelper.PrintStr("西班牙语:España, ¡Hola! Cuánto tiempo sin verte!\n");
                        posApiHelper.PrintStr("Arabic:");//阿拉伯语
                        posApiHelper.PrintStr("قل مرحبا عند مقابلتك");//阿拉伯语
                        posApiHelper.PrintStr("الفبای فارسی گروه سی‌ودوگانهٔ");//阿拉伯语
                        posApiHelper.PrintStr("سی‌ودوگانهٔ");
                        posApiHelper.PrintStr("حروف الفبا یا حروف هجای فارسی می‌گویند");
                        //Print.Lib_PrnStr("\n");
                        posApiHelper.PrintStr("الفبای فارسی گروه سی‌ودوگانهٔ حروف (اَشکال نوشتاری) در خط فارسی است که نمایندهٔ نگاشتن (همخوان‌ها یا صامت‌ها) در زبان فارسی است و");//阿拉伯语
                        //Print.Lib_PrnStr("\n");
                        //Print.Lib_PrnStr("ابر» و «امروز» و «اردک») هر دو می‌تواند باشد)، و برعکس، ممکن");
                        posApiHelper.PrintStr("است که نمایندهٔ نگاشتن (همخوان‌ها یا صامت‌ها) در زبان فارسی است و");                        posApiHelper.PrintStr("泰语:สวัสดีครับไม่เจอกันนานเลยนะ!\n");
                        posApiHelper.PrintStr("法语:Bonjour! Ça fait longtemps!\n");
                        posApiHelper.PrintStr("                                         \n");
                        posApiHelper.PrintStr("                                         \n");


                        SendMsg("Printing... ");
                        Log.d("Robert2", "Lib_PrnStart ret START1 " );
                        ret = posApiHelper.PrintStart();

                        msg1.what = ENABLE_RG;
                        handler.sendMessage(msg1);

                        Log.d("Robert2", "Lib_PrnStart ret = " + ret);

                        if (ret != 0) {
                            RESULT_CODE = -1;
                            Log.e("liuhao", "Lib_PrnStart fail, ret = " + ret);
                            if (ret == -1) {
                                SendMsg("No Print Paper ");
                            } else if(ret == -2) {
                                SendMsg("too hot ");
                            }else if(ret == -3) {
                                SendMsg("low voltage ");
                            }else{
                                SendMsg("Print fail ");
                            }
                        } else {
                            RESULT_CODE = 0;
                            SendMsg("Print Finish ");
                        }
                        Log.d("Robert2", "Lib_PrnStart ret9 " );
                        break;


                    case PRINT_CYCLE:
                        SendMsg("PRINT_CYCLE");
                        msg.what = DISABLE_RG;
                        handler.sendMessage(msg);
                        posApiHelper.PrintSetFont((byte) 24, (byte) 24, (byte) 0x00);
                        for (long dd = 0; dd < 100; dd++) {
                            posApiHelper.PrintStr("0 1 2 3 4 5 6 7 8 9 A B C D E\n");
                        }

                        SendMsg("Printing... ");
                        ret = posApiHelper.PrintStart();


                        msg1.what = ENABLE_RG;
                        handler.sendMessage(msg1);

                        Log.d("", "Lib_PrnStart ret = " + ret);
                        if (ret != 0) {
                            RESULT_CODE = -1;
                            Log.e("liuhao", "Lib_PrnStart fail, ret = " + ret);
                            if (ret == -1) {
                                SendMsg("No Print Paper ");
                            } else if(ret == -2) {
                                SendMsg("too hot ");
                            }else if(ret == -3) {
                                SendMsg("low voltage ");
                            }else{
                                SendMsg("Print fail ");
                            }
                        } else {
                            RESULT_CODE = 0;
                            SendMsg("Print Finish ");
                        }

                        break;

                    case PRINT_UNICODE:
                        Log.d("Robert2", "Lib_PrnStart ret START11 " );
                        final long starttime = System.currentTimeMillis();
                        Log.e("Robert2", "PRINT_UNICODE starttime = " + starttime);

                        SendMsg("PRINT_UNICODE");
                        msg.what = DISABLE_RG;
                        handler.sendMessage(msg);
                        posApiHelper.PrintSetFont((byte) 24, (byte) 24, (byte) 0x00);

                        posApiHelper.PrintStr("中文:你好，好久不见。\n");
                        posApiHelper.PrintStr("英语: ￡20.00 ，￡20.00 ，￡20.00 Hello, Long time no see\n");
                        posApiHelper.PrintStr("西班牙语:España, ¡Hola! Cuánto tiempo sin verte!\n");
//                         posApiHelper.PrintStr("阿拉伯语:مرحبا! وقت طويل لا رؤية!\n");
                        posApiHelper.PrintStr("法语:Bonjour! Ça fait longtemps!\n");
                        posApiHelper.PrintStr("Italian :Ciao, non CI vediamo da Molto Tempo.\n");
//                        posApiHelper.PrintStr("日语:こんにちは！久しぶり！\n");
//                        posApiHelper.PrintStr("俄语:Привет! Давно не виделись!\n");
//                        posApiHelper.PrintStr("韩语:안녕하세요! 긴 시간은 더 볼 수 없습니다!\n");
//                        String string1 = "А а, Б б, В в, Г г, Д д, Е е, Ё ё, Ж ж, З з, И и, Й й, К к, Л л, М м, Н н, О о, Ө ө, П п, Р р, С с, Т т, У у, Ү ү, Ф ф, Х х, Ц ц, Ч ч, Ш ш, Щ щ, Ъ ъ, Ь ь, Ы ы, Э э, Ю ю, Я я";
//                        posApiHelper.PrintStr(string1 + "\n");
//                        posApiHelper.PrintStr("                                         ");
//                        posApiHelper.PrintStr("\n");
//                        posApiHelper.PrintStr("                                         ");
//                        posApiHelper.PrintStr("\n");

                        SendMsg("Printing... ");
//                        ret = posApiHelper.PrintStart();
                        Log.d("Robert2", "Lib_PrnStart ret START12 " );
                        ret = posApiHelper.PrintCtnStart();

                            posApiHelper.PrintSetFont((byte) 16, (byte) 16, (byte) 0x33);
                            for (int i = 1; i < 3; i++) {
                                posApiHelper.PrintSetFont((byte) 24, (byte) 24, (byte) 0x33);
                                posApiHelper.PrintStr("打印第：" + i + "次\n");
                            posApiHelper.PrintStr("商户存根MERCHANT COPY\n");
                            posApiHelper.PrintStr("- - - - - - - - - - - - - - - - - - - - - - - -\n");
                            posApiHelper.PrintSetFont((byte) 24, (byte) 24, (byte) 0x00);
                            posApiHelper.PrintStr("商户名称(MERCHANT NAME):\n");
                            posApiHelper.PrintStr("中国银联直连测试\n");
                            posApiHelper.PrintStr("商户编号(MERCHANT NO):\n");
                            posApiHelper.PrintStr("    001420183990573\n");
                            posApiHelper.PrintStr("终端编号(TERMINAL NO):00026715\n");
                            posApiHelper.PrintStr("操作员号(OPERATOR NO):12345678\n");
                            posApiHelper.PrintStr("- - - - - - - - - - - - - - - -\n");
                            //	posApiHelper.PrintStr("\n");
                            posApiHelper.PrintStr("发卡行(ISSUER):01020001 工商银行\n");
                            posApiHelper.PrintStr("卡号(CARD NO):\n");
                            posApiHelper.PrintStr("    9558803602109503920\n");
                            posApiHelper.PrintStr("收单行(ACQUIRER):03050011民生银行\n");
                            posApiHelper.PrintStr("交易类型(TXN. TYPE):消费/SALE\n");
                            posApiHelper.PrintStr("卡有效期(EXP. DATE):2013/08\n");
                            posApiHelper.PrintStr("- - - - - - - - - - - - - - - -\n");
                            //	posApiHelper.PrintStr("\n");
                            posApiHelper.PrintStr("批次号(BATCH NO)  :000023\n");
                            posApiHelper.PrintStr("凭证号(VOUCHER NO):000018\n");
                            posApiHelper.PrintStr("授权号(AUTH NO)   :987654\n");
                            posApiHelper.PrintStr("日期/时间(DATE/TIME):\n");
                            posApiHelper.PrintStr("    2008/01/28 16:46:32\n");
                            posApiHelper.PrintStr("交易参考号(REF. NO):200801280015\n");
                            posApiHelper.PrintStr("金额(AMOUNT):  RMB:2.55\n");
                            posApiHelper.PrintStr("- - - - - - - - - - - - - - - -\n");
                            //	posApiHelper.PrintStr("\n");
                            posApiHelper.PrintStr("备注/REFERENCE\n");
                            posApiHelper.PrintStr("- - - - - - - - - - - - - - - -\n");
                            posApiHelper.PrintSetFont((byte) 16, (byte) 16, (byte) 0x00);
                            posApiHelper.PrintStr("持卡人签名(CARDHOLDER SIGNATURE)\n");
                            posApiHelper.PrintStr("\n");
                            posApiHelper.PrintStr("- - - - - - - - - - - - - - - - - - - - - - - -\n");
                            //	posApiHelper.PrintStr("\n");
                            posApiHelper.PrintStr("  本人确认以上交易，同意将其计入本卡帐户\n");
                            posApiHelper.PrintStr("  I ACKNOWLEDGE SATISFACTORY RECEIPT\n");
                            posApiHelper.PrintStr("\n\n\n\n\n\n\n\n\n\n");

                            ret = posApiHelper.PrintCtnStart();

                            if (ret != 0) break;
                        }

                        msg1.what = ENABLE_RG;
                        handler.sendMessage(msg1);
                        Log.d("", "Lib_PrnStart ret = " + ret);
                        if (ret != 0) {
                            RESULT_CODE = -1;
                            Log.e("liuhao", "Lib_PrnStart fail, ret = " + ret);
                            if (ret == -1) {
                                SendMsg("No Print Paper ");
                            } else if(ret == -2) {
                                SendMsg("too hot ");
                            }else if(ret == -3) {
                                SendMsg("low voltage ");
                            }else{
                                SendMsg("Print fail ");
                            }
                        } else {
                            RESULT_CODE = 0;
                            SendMsg("Print Finish ");
                        }

                //        ret = posApiHelper.PrintCheckStatus();
               //         Log.e("liuhao", "PrintCheckStatus = " + ret);

                        ret = posApiHelper.PrintClose();
                        final long endttime = System.currentTimeMillis();
                        Log.e("printtime", "PRINT_UNICODE endttime = " + endttime);
                        final long totaltime = starttime - endttime;
                        SendMsg("Print Finish totaltime" + totaltime);
                        break;

                    case PRINT_OPEN:

                        SendMsg("PRINT_OPEN");
                        msg.what = DISABLE_RG;
                        handler.sendMessage(msg);

                        SendMsg("Print Open... ");
                        try {
                            ret = posApiHelper.PrintOpen();
                        } catch (PrintInitException e) {
                            e.printStackTrace();
                        }

                        msg1.what = ENABLE_RG;
                        handler.sendMessage(msg1);

                        Log.d("", "Lib_PrnStart ret = " + ret);
                        if (ret != 0) {
                            RESULT_CODE = -1;
                            Log.e("liuhao", "Lib_PrnStart fail, ret = " + ret);
                            if (ret == -1) {
                                SendMsg("No Print Paper ");
                            } else if (ret == -2) {
                                SendMsg("too hot ");
                            } else if (ret == -3) {
                                SendMsg("low voltage ");
                            } else {
                                SendMsg("Print fail ");
                            }
                        } else {
                            RESULT_CODE = 0;
                            SendMsg("Print Finish ");
                        }


//                        posApiHelper.PrintSetFont((byte) 24, (byte) 24, (byte) 0x00);
//                        posApiHelper.PrintStr("   TRES AMIGOS  \n");
//                        posApiHelper.PrintStr("   LOTERICA  \n");
//                        posApiHelper.PrintStr("                  \n");
//                        posApiHelper.PrintStr("   Filial: 9999  \n");
//                        posApiHelper.PrintStr("   Equipamento: 123456789012345  \n");
//                        posApiHelper.PrintStr("   Usuario: 9999  \n");
//                        posApiHelper.PrintStr("   Data: 23/01/2018  \n\n");
//                        posApiHelper.PrintStr("   CONCURSO(S) APOSTADO(S):  \n");
//                        posApiHelper.PrintStr("   23/01/2018 18:00 cap  \n");
//                        posApiHelper.PrintStr("   24/01/2018 18:00 cap  \n");
//                        posApiHelper.PrintStr("   25/01/2018 18:00 cap  \n");
//                        posApiHelper.PrintStr("   26/01/2018 18:00 cap  \n");
//                        posApiHelper.PrintStr("   27/01/2018 18:00 cap  \n\n");
//                        posApiHelper.PrintStr("   APOSTA(S):  \n");
//                        posApiHelper.PrintStr("   /repeat the code below 100 times/  \n");
//                        posApiHelper.PrintStr("   Tipo: centena invertido  \n");
//                        posApiHelper.PrintStr("   Aposta: 1528  \n");
//                        posApiHelper.PrintStr("   valor: R$1,64  \n");
//                        posApiHelper.PrintStr("   /end 100 times/  \n\n");
//                        posApiHelper.PrintStr("   Valor total: R$20,00  \n\n");
//                        posApiHelper.PrintStr("   Desconto:R$0,00  \n\n");
//                        posApiHelper.PrintStr("   Total a pagar: R$20,00  \n\n");
//                        posApiHelper.PrintStr("   qrcode (here generate qrcode 200 x 200)\n\n");
//                        posApiHelper.PrintStr("QR_CODE display " );
//                        content = "com.chips.ewallet.scheme://{\"PayeeMemberUuid\":\"a3d7fe8e-873d-499b-9f11-000000000000\",\"PayerMemberUuid\":null,\"TotalAmount\":\"900\",\"PayeeSiteUuid\":null,\"PayeeTransId\":\"100101-084850-6444\",\"PayeeSiteReference\":\"\",\"PayeeDescription\":null,\"ConfirmationUuid\":null,\"StpReference\":null}";
//                        posApiHelper.PrintBarcode(content, 200, 200, BarcodeFormat.QR_CODE);
//                        posApiHelper.PrintStr("   Guarde esse recibo!\n");
//                        posApiHelper.PrintStr("   Nao fornecemos segunda via!\n");
//                        posApiHelper.PrintStr("   Boa sorte!\n\n");
//                        posApiHelper.PrintStart();

//                        posApiHelper.PrintSetFont((byte) 24, (byte) 24, (byte) 0x00);
//                        posApiHelper.PrintStr("         一品佳港大店         \n\n");
//                        posApiHelper.PrintStr("#34102  收银1 03月13日    18:37\n");
//                        posApiHelper.PrintStr("-------------------------------\n");
//                        posApiHelper.PrintStr("自选快餐         1       21.00\n");
//                        posApiHelper.PrintStr("-------------------------------\n");
//                        posApiHelper.PrintSetFont((byte) 24, (byte) 24, (byte) 0x33);
//                        posApiHelper.PrintStr("支付宝   21.00\n");
//                        posApiHelper.PrintSetFont((byte) 24, (byte) 24, (byte) 0x00);
//                        posApiHelper.PrintStr("                                                \n");
//                        posApiHelper.PrintStr("               多谢惠顾        \n");
//                        posApiHelper.PrintStart();


//                        int status = 0;
//                        Log.d("PRT", "Printer is: " + status);
//
//                        try {
//                            status = posApiHelper.PrintInit(2, 24, 24, 0);
//                            if (status != 0) {
//                                do {
//                                    status = posApiHelper.PrintOpen();
//                                } while (status != 0);
//                                Log.d("PRT-OPEN", "PrinterService open success");
//                            }
//
//                            posApiHelper.PrintSetGray(5);
//                            posApiHelper.PrintSetFont((byte) 16, (byte) 16, (byte) 0x33);
//                            posApiHelper.PrintStr("Phnom Penh International University\n");
//                            posApiHelper.PrintStr("\n");
//                            posApiHelper.PrintStr("Parking slip\n");
//                            posApiHelper.PrintStr("\n");
//
//                            posApiHelper.PrintBarcode(content, 240, 240, BarcodeFormat.QR_CODE);
//
//                            status = posApiHelper.PrintStart();
//                            if (status != 0) {
//
//                                Log.e("liuhao", "Lib_PrnStart fail, ret = " + status);
//                                if (status == -1) {
//
//                                } else if(status == -2) {
//
//                                } else if(status == -3) {
//
//                                }
//                            }
//
//                        } catch (PrintInitException e) {
//                            e.printStackTrace();
//                        } finally {
//                            posApiHelper.PrintClose();
//                        }
                        break;

                    case PRINT_BMP:
                        SendMsg("PRINT_BMP");
                        msg.what = DISABLE_RG;
                        handler.sendMessage(msg);

                     //   Bitmap bmp = BitmapFactory.decodeResource(PrintActivity.this.getResources(), R.mipmap.metrolinx1bitdepth);
                        final long start_BmpD = System.currentTimeMillis();
                        Bitmap bmp1 = BitmapFactory.decodeResource(PrintActivity.this
                                .getResources(), R.mipmap.kyaps___log100);
                        final long end_BmpD = System.currentTimeMillis();
                        final long decodetime = end_BmpD - start_BmpD;
                        final long start_PrintBmp = System.currentTimeMillis();
                        ret = posApiHelper.PrintBmp(bmp1);
                        posApiHelper.PrintStr("                                         \n");
                        if (ret == 0) {
                            posApiHelper.PrintStr("\n\n\n");
                            posApiHelper.PrintStr("                                         \n");
                            posApiHelper.PrintStr("                                         \n");

                            SendMsg("Printing... ");
                            ret = posApiHelper.PrintStart();

                            msg1.what = ENABLE_RG;
                            handler.sendMessage(msg1);

                            Log.d("", "Lib_PrnStart ret = " + ret);
                            if (ret != 0) {
                                RESULT_CODE = -1;
                                Log.e("liuhao", "Lib_PrnStart fail, ret = " + ret);
                                if (ret == -1) {
                                    SendMsg("No Print Paper ");
                                } else if(ret == -2) {
                                    SendMsg("too hot ");
                                }else if(ret == -3) {
                                    SendMsg("low voltage ");
                                }else{
                                    SendMsg("Print fail ");
                                }
                            } else {
                                final long end_PrintBmp = System.currentTimeMillis();
                                RESULT_CODE = 0;
                                final long PrintTime = start_PrintBmp - end_PrintBmp;
                                SendMsg("Print Finish BMP decodetime="+decodetime + "PrintBmpTime"+PrintTime);
                            }
                        } else {
                            RESULT_CODE = -1;
                            SendMsg("Lib_PrnBmp Failed");
                        }
                        break;

                    case PRINT_BARCODE:
                        SendMsg("PRINT_BARCODE");
                        msg.what = DISABLE_RG;
                        handler.sendMessage(msg);

                        content = "com.chips.ewallet.scheme://{\"PayeeMemberUuid\":\"a3d7fe8e-873d-499b-9f11-000000000000\",\"PayerMemberUuid\":null,\"TotalAmount\":\"900\",\"PayeeSiteUuid\":null,\"PayeeTransId\":\"100101-084850-6444\",\"PayeeSiteReference\":\"\",\"PayeeDescription\":null,\"ConfirmationUuid\":null,\"StpReference\":null}";
                        posApiHelper.PrintStr("QR_CODE display " );
                        posApiHelper.PrintBarcode(content, 360, 360, BarcodeFormat.QR_CODE);
                        posApiHelper.PrintStr("PrintQrCode_Cut display " );
                        posApiHelper.PrintQrCode_Cut(content, 360, 360, BarcodeFormat.QR_CODE);
                        posApiHelper.PrintStr("PrintCutQrCode_Str display " );
                        posApiHelper.PrintCutQrCode_Str(content,"PK TXT adsad adasd sda",5, 300, 300, BarcodeFormat.QR_CODE);
                        //0 Left ,1 Middle ,2 Right
                        Print.Lib_PrnSetAlign(0);
                        posApiHelper.PrintStr("QR_CODE : " + content + "\n\n");

//					posApiHelper.PrintBarcode(content, 360, 120, BarcodeFormat.CODE_39);
//					posApiHelper.PrintStr("CODE_39 : " + content + "\n\n");
                        posApiHelper.PrintStr("                                        \n");
                        posApiHelper.PrintStr("                                        \n");

                        SendMsg("Printing... ");
                        ret = posApiHelper.PrintStart();

                        msg1.what = ENABLE_RG;
                        handler.sendMessage(msg1);

                        Log.d("", "Lib_PrnStart ret = " + ret);
                        if (ret != 0) {
                            RESULT_CODE = -1;
                            Log.e("liuhao", "Lib_PrnStart fail, ret = " + ret);
                            if (ret == -1) {
                                SendMsg("No Print Paper ");
                            } else if(ret == -2) {
                                SendMsg("too hot ");
                            }else if(ret == -3) {
                                SendMsg("low voltage ");
                            }else{
                                SendMsg("Print fail ");
                            }
                        } else {
                            RESULT_CODE = 0;
                            SendMsg("Print Finish ");
                        }

                        break;

                    default:
                        break;
                }
                m_bThreadFinished = true;

                Log.e(tag, "goToSleep2...");
            }
        }
    }


    public void SendMsg(String strInfo) {
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putString("MSG", strInfo);
        msg.setData(b);
        handler.sendMessage(msg);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case DISABLE_RG:
                    IsWorking = 1;
                    rb_high.setEnabled(false);
                    rb_middle.setEnabled(false);
                    rb_low.setEnabled(false);
                    radioButton_4.setEnabled(false);
                    radioButton_5.setEnabled(false);
                    break;

                case ENABLE_RG:
                    IsWorking = 0;
                    rb_high.setEnabled(true);
                    rb_middle.setEnabled(true);
                    rb_low.setEnabled(true);
                    radioButton_4.setEnabled(true);
                    radioButton_5.setEnabled(true);

                    break;
                default:
                    Bundle b = msg.getData();
                    String strInfo = b.getString("MSG");
                    textViewMsg.setText(strInfo);

                    break;
            }
        }
    };

    public class BatteryReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            voltage_level = intent.getExtras().getInt("level");// ��õ�ǰ����
            Log.e("wbw", "current  = " + voltage_level);
            BatteryV = intent.getIntExtra("voltage", 0);  //电池电压
            Log.e("wbw", "BatteryV  = " + BatteryV);
            Log.e("wbw", "V  = " + BatteryV * 2 / 100);
            //	m_voltage = (int) (65+19*voltage_level/100); //放大十倍
            //   Log.e("wbw","m_voltage  = " + m_voltage );
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

    // 在Activity中，我们通过ServiceConnection接口来取得建立连接与连接意外丢失的回调

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
//            MyService.MyBinder binder = (MyService.MyBinder)service;
//            binder.getService();// 获取到的Service即MyService
            MyService.MyBinder binder = (MyService.MyBinder) service;
            MyService myService = binder.getService();

            myService.setCallback(new MyService.CallBackPrintStatus() {
                @Override
                public void printStatusChange(String strStatus) {
                    SendMsg(strStatus);
                }
            });

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    public void OnClickPrintSimpleApiTest(View view) {

//        mPrintServiceIntent=new Intent(PrintActivity.this, MyService.class);
//        startService(mPrintServiceIntent);

        //绑定目标Service
//        bindService(mPrintServiceIntent,serviceConnection,Context.BIND_AUTO_CREATE);

        if (printThread != null && !printThread.isThreadFinished()) {
            Log.e(tag, "Thread is still running...");
            return;
        }


        new Thread(new Runnable() {
            @Override
            public void run() {

                Message msg = Message.obtain();
                Message msg1 = Message.obtain();

                msg.what = DISABLE_RG;
                handler.sendMessage(msg);

                ret = posApiHelper.PrintInit(2, 24, 24, 0);
                /*
                *  or
                *  No parameter defaults Api
                 */

                /*
                try {
                    ret= posApiHelper.PrintInit();
                } catch (PrintInitException e) {
                    e.printStackTrace();
                    int initRet = e.getExceptionCode();
                    Log.e(TAG,"initRer : "+initRet);
                }
                */

                if (ret != 0) {
                    return;
                }

                ret = getValue();
                Log.e(tag, "getValue():" + ret);

                posApiHelper.PrintSetGray(ret);

                posApiHelper.PrintStr("Print Tile\n");
                posApiHelper.PrintStr("\n");
//                ret = posApiHelper.PrintSetFont((byte)24, (byte)24, (byte)0);
                ret = posApiHelper.PrintSetFont((byte) 16, (byte) 16, (byte) 0x33);

                Log.e(tag, "initRer PrintSetFont: " + ret);

                if (ret != 0) {
                    return;
                }
                posApiHelper.PrintStr("- - - - - - - - - - - - - - - - - - - - - - - -\n");
                //	posApiHelper.PrintStr("\n");
                posApiHelper.PrintStr("  Print Str1 \n");
                posApiHelper.PrintStr("  Print Str2 \n");
                posApiHelper.PrintBarcode("123456789", 360, 120, BarcodeFormat.CODE_128);
                posApiHelper.PrintBarcode("123456789", 240, 240, BarcodeFormat.QR_CODE);
                posApiHelper.PrintStr("CODE_128 : " + "123456789" + "\n\n");
                posApiHelper.PrintStr("QR_CODE : " + "123456789" + "\n\n");
                posApiHelper.PrintStr("                                        \n");
                posApiHelper.PrintStr("\n");
                posApiHelper.PrintStr("\n");

                SendMsg("Printing... ");
                ret = posApiHelper.PrintStart();

                Log.e(tag, "Lib_PrnStart ret = " + ret);

                msg1.what = ENABLE_RG;
                handler.sendMessage(msg1);

                if (ret != 0) {
                    RESULT_CODE = -1;
                    Log.e("liuhao", "Lib_PrnStart fail, ret = " + ret);
                    if (ret == -1) {
                        SendMsg("No Print Paper ");
                    } else if(ret == -2) {
                        SendMsg("too hot ");
                    }else if(ret == -3) {
                        SendMsg("low voltage ");
                    }else{
                        SendMsg("Print fail ");
                    }
                } else {
                    RESULT_CODE = 0;
                    SendMsg("Print Finish ");
                }
            }
        }).start();

    }

}
