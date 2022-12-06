package test.apidemo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import vpos.apipackage.ByteUtil;
import vpos.apipackage.PosApiHelper;
import vpos.apipackage.StringUtil;

/**
 * Created by Administrator on 2017/8/17.
 */

public class PciDukptActivity extends Activity {


    public static final int OPCODE_DUKPT_LOAD = 3;
    public static final int OPCODE_GET_DUKPTMAC = 4;
    private final String tag = "PciDukptActivity";

    private ReadWriteRunnable _runnable;
    TextView textView = null;


    PosApiHelper posApiHelper = PosApiHelper.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_pcidukpt);

        textView = (TextView) findViewById(R.id.textView_pci);
        textView.setMovementMethod(new ScrollingMovementMethod());
        SendMsg("Please click \"Dukpt_Load\" to load the key ", 0);
    }


    public void OnClickGetDes(View view) {
        if (_runnable != null && _runnable.IsThreadFinished() == false) {
            Log.e("", "Thread is still running, return...");
            return;
        }
        _runnable = new ReadWriteRunnable(OPCODE_DUKPT_LOAD);
        Thread requestThread = new Thread(_runnable);
        requestThread.start();
        Log.i(tag, "OnClickGetDes");
    }

    public void OnClickGetMac(View view) {
        if (_runnable != null && _runnable.IsThreadFinished() == false) {
            Log.e("", "Thread is still running, return...");
            return;
        }
        _runnable = new ReadWriteRunnable(OPCODE_GET_DUKPTMAC);
        Thread requestThread = new Thread(_runnable);
        requestThread.start();
        Log.i(tag, "OnClickGetMac");
    }



    private class ReadWriteRunnable implements Runnable {
        private int mOpCode, ret;
        byte[] keyData = null;
        byte[] key_kcv = null;
        boolean isThreadFinished = false;

        public boolean IsThreadFinished() {
            return isThreadFinished;
        }

        public ReadWriteRunnable(int OpCode) {
            mOpCode = OpCode;
        }

        @Override
        public void run() {
            isThreadFinished = false;

            switch (mOpCode) {

                case OPCODE_DUKPT_LOAD:
                    int BdkLen, KsnLen, KeyIdDATA, KeyIdPIN;
                    Log.e("Robert", "dukpt test0");
                    final  byte[] BDK = StringUtil.hexStringToBytes("0123456789ABCDEFFEDCBA9876543210");
                    final  byte[] IPEK = StringUtil.hexStringToBytes("8A861B8B13AD8F449AB521E127EDDDD6");

                //    final  byte[] KSN = StringUtil.hexStringToBytes("32222222222222222224");
                    final  byte[] KSNDATA = StringUtil.hexStringToBytes("FFFF0705160000000336");
                    final  byte[] KSNPIN = StringUtil.hexStringToBytes("FFFF0705160000000334");
                    BdkLen = BDK.length;
                    KsnLen = KSNDATA.length;
                    KeyIdDATA = 1;
                    Log.e("Robert", "dukpt test1");
                    ret = posApiHelper.PciWriteDukptBdk( KeyIdDATA,BdkLen, BDK, KsnLen, KSNDATA);
                    ret = posApiHelper.PciWriteDukptIpek( KeyIdDATA, IPEK.length, IPEK, KsnLen, KSNDATA);

                    KeyIdPIN = 2;
                    ret = posApiHelper.PciWriteDukptBdk( KeyIdPIN,BdkLen, BDK, KSNPIN.length, KSNPIN);
                    ret = posApiHelper.PciWriteDukptIpek( KeyIdPIN, IPEK.length, IPEK, KSNPIN.length, KSNPIN);
                    byte[] IV = {0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
                    byte[] InData = {

                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,

                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,

                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,

                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,
                            0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11

                    };
                    byte[] out = new byte[1000];
                    byte[] OutKsn = new byte[1000];
                    byte[] outKcv = new byte[1000];
                 //   ret = posApiHelper.PciGetDukptDataTDES(1, (byte)1,(byte)0,(short)640,InData,IV,out,OutKsn,outKcv);///////data_in16字节11，IV8字节的0，后面3个是输出
                  //  PciGetDukptDataTDES(int pinkey_n, byte mode, byte Mac_data_len, byte[] Mac_data_in, byte[] Mac_out, byte[] OutKsn, byte[] MacKcv)
                  //  SendMsg("DuktDesCal DataOUT:\n"+ByteUtil.bytearrayToHexString(out, 640), 0);
                    SendMsg("Key successfully loaded", 0);
                    break;

                case OPCODE_GET_DUKPTMAC:
                    int  Pin_index;
                    byte mode;
                    Pin_index = 1;
                    mode = 0;
                    final byte[] Mac_data_in =  new byte[]{0x31 ,0x32 ,0x33 ,0x34 ,0x35 ,0x36 ,0x37 ,0x38 ,0x39 ,0x30 ,0x31 ,0x32 ,0x33 ,0x34 ,0x35 ,0x36,0x37, 0x38};//StringUtil.hexStringToBytes("123456789012345678");
                    final byte[] Mac_Out= new byte[512];
                    final byte[] OutKsn_dukpt = new byte[10];
                    final byte[] Mac_Kcv_dukpt = new byte[3];
                    posApiHelper.SetAutoAddKSNDATA(1, 1);
                    byte Mac_data_len = 16;
                    ret = posApiHelper.PciGetDukptDataTDES( Pin_index , mode, Mac_data_len,   Mac_data_in, Mac_Out, OutKsn_dukpt, Mac_Kcv_dukpt);
                    SendMsg("DataOUT- "+ByteUtil.bytearrayToHexString(Mac_Out, Mac_data_len) +"OutKsn_dukpt- "+ByteUtil.bytearrayToHexString(OutKsn_dukpt, OutKsn_dukpt.length), 0);
                    Log.e("dukpt", "PciGetDukptMac Mac_Out- "+ByteUtil.bytearrayToHexString(Mac_Out, Mac_Out.length));
                    Log.e("dukpt", "PciGetDukptMac OutKsn_dukpt - "+ByteUtil.bytearrayToHexString(OutKsn_dukpt, OutKsn_dukpt.length));
                    break;

                default:
                    break;
            }

            isThreadFinished = true;
        }
    }

    public void SendMsg(String strInfo, int what) {
        Message msg = new Message();
        msg.what = what;
        Bundle b = new Bundle();
        b.putString("MSG", strInfo);
        msg.setData(b);
        handler.sendMessage(msg);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();
            String strInfo = b.getString("MSG");
            if (msg.what == 0) {
                textView.setText(strInfo);
            } else {
                textView.setText(textView.getText() + "\n" + strInfo);
            }
            Log.i(tag, strInfo);
        }
    };

}
