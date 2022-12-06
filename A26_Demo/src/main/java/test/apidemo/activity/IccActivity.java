package test.apidemo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import vpos.apipackage.APDU_RESP;
import vpos.apipackage.APDU_SEND;
import vpos.apipackage.ByteUtil;
import vpos.apipackage.PosApiHelper;


/**
 * Created by Administrator on 2017/8/17.
 */

public class IccActivity extends Activity implements View.OnClickListener {

    private final String TAG = "IccActivity";
    private boolean isIccChecked = false;
    private boolean isPsam1Checked =false;
    private boolean isPsam2Checked =false;
    private RadioGroup rg = null;
    RadioButton radioButtonIcc = null;

    private Button btnSingleTest = null;

    public byte dataIn[] = new byte[512];
    public byte ATR[] = new byte[40];
    public byte vcc_mode = 1;
    private int ret;

    TextView tv_msg = null;

    PosApiHelper posApiHelper = PosApiHelper.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                WindowManager.LayoutParams. FLAG_FULLSCREEN);

        setContentView(R.layout.activity_icc);

        rg = (RadioGroup) this.findViewById(R.id.rg_card_type);
        tv_msg = (TextView) this.findViewById(R.id.tv_msg);

        btnSingleTest = (Button) findViewById(R.id.button_SingleTest);
        btnSingleTest.setOnClickListener(IccActivity.this);

        radioButtonIcc = (RadioButton) findViewById(R.id.radioButton_icc);
        radioButtonIcc.setChecked(true);
        isIccChecked = true;
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId) {
                    case R.id.radioButton_icc:
                        isIccChecked = true;
                        isPsam1Checked = false;
                        isPsam2Checked = false;
                        tv_msg.setText("Icc Checked");
                        break;
                    case R.id.RadioButton_psam1:
                        isIccChecked = false;
                        isPsam1Checked = true;
                        isPsam2Checked = false;
                        tv_msg.setText("Psam1 Checked");
                        break;
                    case R.id.RadioButton_psam2:
                        isIccChecked = false;
                        isPsam1Checked = false;
                        isPsam2Checked = true;
                        tv_msg.setText("Psam2 Checked");
                        break;
                }
            }
        });

    }


    String strInfo = "";

    void startTestIcc(byte slot) {
        ret = 1;
        if (slot == 0) {
            ret = posApiHelper.IccCheck(slot);
            if (ret != 0) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        tv_msg.setText("Check Failed");
                    }
                });
                Log.e(TAG, "Lib_IccCheck failed!");
                //add by liuhao for 0529
           //     posApiHelper.IccClose(slot);

                return ;
            }
        }

        ret = posApiHelper.IccOpen(slot, vcc_mode, ATR);
        if (ret != 0) {
            runOnUiThread(new Runnable() {
                public void run() {
                    tv_msg.setText("Open Failed");
                }
            });
            Log.e(TAG, "IccOpen failed!");

            //add by liuhao for 0529
//          posApiHelper.IccClose(slot);

            return;
        }

        Log.e(TAG, "atrString = " + ByteUtil.bytearrayToHexString(ATR, 40));
        String atrString = "";
        for (int i = 0; i < ATR.length; i++) {
            atrString += Integer.toHexString(Integer.valueOf(String.valueOf(ATR[i]))).replaceAll("f", "");
        }
        Log.e(TAG, "atrString = " + atrString);

        byte cmd[] = new byte[4];
        short lc = 0;
        short le = 0;
        if (slot == 0) {
            /*
            cmd[0] = 0x00;            //0-3 cmd
            cmd[1] = (byte) 0xA4;
            cmd[2] = 0x04;
            cmd[3] = 0x00;
            lc = 0x0E;
            le = 1;
            String sendmsg = "1PAY.SYS.DDF01";
            dataIn = sendmsg.getBytes();
            */
            cmd[0] = (byte) 0x00;   //0-3 cmd
            cmd[1] = (byte) 0xa4;
            cmd[2] = 0x04;
            cmd[3] = 0x00;
            lc = 0x05;
            le = 0x00;

            dataIn[0] = (byte)0x49;
            dataIn[1] = (byte)0x47;
            dataIn[2] = (byte)0x54;
            dataIn[3] = (byte)0x50;
            dataIn[4] = (byte)0x43;
        } else {

            cmd[0] = 0x00;            //0-3 cmd
            cmd[1] = (byte) 0x84;
            cmd[2] = 0x00;
            cmd[3] = 0x00;
            lc = 0x00;
            le = 0x08;
            String sendmsg = "";
            dataIn = sendmsg.getBytes();
            Log.e("liuhao Icc  " ,"PSAM *******");

//            cmd[0] = (byte) 0xBC;            //0-3 cmd
//            cmd[1] = (byte) 0xA4;
//            cmd[2] = 0x00;
//            cmd[3] = 0x00;
//            lc = 0x0002;
//            le = 0x00;
//            String sendmsg = "3F00";
//            dataIn = hexStringToByteArray(sendmsg);

        }
        APDU_SEND ApduSend = new APDU_SEND(cmd, lc, dataIn, le);
        APDU_RESP ApduResp = null;
        byte[] resp = new byte[516];

        ret = posApiHelper.IccCommand(slot, ApduSend.getBytes(), resp);
        if (0 == ret) {
            ApduResp = new APDU_RESP(resp);
            strInfo = ByteUtil.bytearrayToHexString(ApduResp.DataOut, ApduResp.LenOut) + "SWA:"
                    + ByteUtil.byteToHexString(ApduResp.SWA) + " SWB:" + ByteUtil.byteToHexString(ApduResp.SWB);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_msg.setText(strInfo);
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_msg.setText("Command Failed");
                }
            });
            Log.e(TAG, "Icc_Command failed!");
        }

//        posApiHelper.IccClose(slot);

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Converting a string of hex character to bytes
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2){
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }


    ICC_Thread IccThread = null;

    public void onClick(View v) {

        if (null != IccThread && !IccThread.isThreadFinished()) {
            Log.e("onClickTest", "return return");
            return;
        }

        tv_msg.setText("");

        switch (v.getId()){
            case R.id.button_SingleTest:
                if (isIccChecked) {
                    IccThread = new ICC_Thread((byte) 0);
                    IccThread.start();
                }
                if (isPsam1Checked) {
                    IccThread = new ICC_Thread((byte) 1);
                    IccThread.start();
                }
                if (isPsam2Checked) {
                    IccThread = new ICC_Thread((byte) 2);
                    IccThread.start();
                }
                break;
        }
    }

    private boolean m_bThreadFinished = false;
    public class ICC_Thread extends Thread {

        byte testMode;
        private int iWaitSecond = 1;

        public boolean isThreadFinished() {
            return m_bThreadFinished;
        }

        public ICC_Thread(byte mode) {
            testMode = mode;
        }

        byte slot = 0;

        public void run() {

            synchronized (this) {
                m_bThreadFinished = false;

                startTestIcc(testMode);

                m_bThreadFinished = true;

            }
            Log.e("ICCThread[ run ]", "run() end");
//            return;
        }
    }


    public void closeLed()  {
        try {
            PosApiHelper.getInstance().SysSetLedMode(1,0);
            Thread.sleep(20);
            PosApiHelper.getInstance().SysSetLedMode(2,0);
            Thread.sleep(20);
            PosApiHelper.getInstance().SysSetLedMode(3,0);
            Thread.sleep(20);
            PosApiHelper.getInstance().SysSetLedMode(4,0);
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        //add by liuhao 0529 close led
        closeLed();
    }
}
