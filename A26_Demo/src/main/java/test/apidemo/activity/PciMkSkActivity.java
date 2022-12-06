package test.apidemo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import vpos.apipackage.ByteUtil;
import vpos.apipackage.PosApiHelper;
import vpos.apipackage.StringUtil;

/**
 * Created by Administrator on 2017/8/17.
 */

public class PciMkSkActivity extends Activity {

    public static final int OPCODE_Write_MKEY = 0;
    public static final int OPCODE_Write_SKey = 1;
    private static final int OPCODE_GET_KCV = 2;
    public static final int OPCODE_GET_DES = 3;
    public static final int OPCODE_GET_MAC = 4;

    private final String tag = "PciMkSkActivity";

    private ReadWriteRunnable _runnable;
    private int RESULT_CODE = 0;

    TextView textView = null;
    byte[] inData = null;
    byte[] poutData = null;
    byte[] KeyData = null;
    byte[] desOut = null;
    byte[] macOut = null;

    byte keyNo = 9;
    byte mkeyNo = 9;
    byte keyLen = 16;
    byte mode = 0;
    short inLen = 8;

    PosApiHelper posApiHelper = PosApiHelper.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_pcimksk);

        textView = (TextView) findViewById(R.id.textView_pci);
//		textView.setText("keyNo = " + keyNo);
        Spinner spinnerKeyNo = (Spinner) findViewById(R.id.spinner_key_no);
        ArrayAdapter<?> adapterKeyNo = ArrayAdapter.createFromResource(this,
                R.array.keyNo, R.layout.spinner_item);
        adapterKeyNo.setDropDownViewResource(R.layout.dropdown_stytle);
        spinnerKeyNo.setAdapter(adapterKeyNo);

        spinnerKeyNo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView tv = (TextView) view;
                tv.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
                Log.e("onItenSelect position", "onItenSelect  " + Integer.toString(position));
                keyNo = (byte) position;
                mkeyNo = (byte) position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void OnClickMkey(View view) {
        if (_runnable != null && _runnable.IsThreadFinished() == false) {
            Log.e("", "Thread is still running, return...");
            return;
        }

        _runnable = new ReadWriteRunnable(OPCODE_Write_MKEY);
        Thread requestThread = new Thread(_runnable);
        requestThread.start();
    }


    public void OnClickGetKcv(View view) {
        _runnable = new ReadWriteRunnable(OPCODE_GET_KCV);
        Thread requestThread = new Thread(_runnable);
        requestThread.start();
    }

    public void OnClickGetDes(View view) {
        if (_runnable != null && _runnable.IsThreadFinished() == false) {
            Log.e("", "Thread is still running, return...");
            return;
        }
        _runnable = new ReadWriteRunnable(OPCODE_GET_DES);
        Thread requestThread = new Thread(_runnable);
        requestThread.start();
        Log.i(tag, "OnClickGetDes");
    }

    public void OnClickGetMac(View view) {
        if (_runnable != null && _runnable.IsThreadFinished() == false) {
            Log.e("", "Thread is still running, return...");
            return;
        }
        _runnable = new ReadWriteRunnable(OPCODE_GET_MAC);
        Thread requestThread = new Thread(_runnable);
        requestThread.start();
        Log.i(tag, "OnClickGetMac");
    }

    public void OnClick_WorkKey(View view) {
        if (_runnable != null && _runnable.IsThreadFinished() == false) {
            Log.e("heyp-kcv", "Thread is still running, return...");
            return;
        }
        _runnable = new ReadWriteRunnable(OPCODE_Write_SKey);
        Thread requestThread = new Thread(_runnable);
        requestThread.start();
        Log.i(tag, "OnClickHost_WorkKey");
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
                case OPCODE_Write_MKEY: { // -----write KLK 16byte 0x31 plantext
                    key_kcv = new byte[32];
                    for (int i = 0; i < 32; i++) {  //-------host 3. We write all other encrypted on master's.
                        key_kcv[i] = (byte) 0x00;
                    }
                    keyData = StringUtil.hexStringToBytes("55555555555555551111111111111111");
                    mode = 0;
                    keyNo = 0;
                    keyLen = 16;
                    ret = posApiHelper.PciWritePIN_MKey(keyNo, keyLen, keyData, mode);
                    Log.e(tag, "OPCODE_KLK_KEY Succeed1\nkey_kcv: " + ByteUtil.bytearrayToHexString(key_kcv, 32));
                    if (ret == 0) {
                        RESULT_CODE = 0;
                        Log.d(tag, "Pci_WritePinMKey success");
                        SendMsg("Pci_WritePinMKey Succeed\n", 0);
                    } else {
                        RESULT_CODE = -1;
                        Log.e(tag, "Pci_WritePinMKey failed, ret = " + ret);
                        SendMsg("Pci_WritePinMKey Failed, ret = " + ret + "\n", 0);
                        break;
                    }

                    ret = posApiHelper.PciWriteMAC_MKey(keyNo, keyLen, keyData, mode);
                    Log.e(tag, "OPCODE_KLK_KEY Succeed2\nkey_kcv: " + ByteUtil.bytearrayToHexString(key_kcv, 32));
                    if (ret == 0) {
                        RESULT_CODE = 0;
                        Log.d(tag, "Pci_WriteMacMKey success");
                        SendMsg("Pci_WriteMacMKey Succeed\n", 1);
                    } else {
                        RESULT_CODE = -1;
                        Log.e(tag, "Pci_WriteMacMKey failed, ret = " + ret);
                        SendMsg("Pci_WriteMacMKey Failed, ret = " + ret + "\n", 1);
                        break;
                    }
                    ret = posApiHelper.PciWriteDES_MKey(keyNo, keyLen, keyData, mode);
                    Log.e(tag, "OPCODE_KLK_KEY Succeed3\nkey_kcv: " + ByteUtil.bytearrayToHexString(key_kcv, 32));
                    if (ret == 0) {
                        RESULT_CODE = 0;
                        Log.d(tag, "Pci_WriteDesMKey success");
                        SendMsg("Pci_WriteDesMKey Succeed\n", 1);
                    } else {
                        RESULT_CODE = -1;
                        Log.e(tag, "Pci_WriteDesMKey failed, ret = " + ret);
                        SendMsg("Pci_WriteDesMKey Failed, ret = " + ret + "\n", 1);
                        break;
                    }
                }
                break;

                case OPCODE_Write_SKey:
                    Log.e("heyp-kcv", "OPCODE_GET_KCV-0");
                    final  byte[] work_pinkey = StringUtil.hexStringToBytes("11111111111111111111111111111112");
                    final  byte[] work_deskey = StringUtil.hexStringToBytes("411A41F9CA1ABE09411A41F9CA1ABE09");
                    final  byte[] work_mackey = StringUtil.hexStringToBytes("41540F929B4E6D71F376D20F68B1BFC7");

                    key_kcv = new byte[32];
                    for (int i = 0; i < 32; i++) {  //-------host 3. We write all other encrypted on master's.
                        key_kcv[i] = (byte) 0x00;
                    }
                    keyNo=0;
                    mkeyNo=0;
                    keyLen=16;
                    mode = 1;
                    ret = posApiHelper.PciWritePinKey(keyNo, keyLen, work_pinkey, mode,mkeyNo);
                  //  Log.e(tag, "OPCODE_GET_HostWorkKey0 Succeed\nkey_kcv: " + ByteUtil.bytearrayToHexString(key_kcv, 32));
                    if (ret == 0) {
                        RESULT_CODE = 0;
                        Log.d(tag, "Pci_WritePinKey success");
                        SendMsg("Pci_WritePinKey Succeed\n", 0);
                    } else {
                        RESULT_CODE = -1;
                        Log.e(tag, "Pci_WritePinKey failed, ret = " + ret);
                        SendMsg("Pci_WritePinKey Failed, ret = " + ret + "\n", 0);
                        break;
                    }

                    ret = posApiHelper.PciWriteMacKey(keyNo, keyLen, work_mackey, mode, mkeyNo);
                    if (ret == 0) {
                        RESULT_CODE = 0;
                        Log.d(tag, "Pci_WriteMacKey success");
                        SendMsg("Pci_WriteMacKey Succeed\n", 1);
                    } else {
                        RESULT_CODE = -1;
                        Log.e(tag, "Pci_WriteMacKey failed, ret = " + ret);
                        SendMsg("Pci_WriteMacKey Failed, ret = " + ret + "\n", 1);
                        break;
                    }

                    ret = posApiHelper.PciWriteDesKey(keyNo, keyLen, work_deskey, mode, mkeyNo);
                    if (ret == 0) {
                        RESULT_CODE = 0;
                        Log.d(tag, "Pci_WriteDesKey success");
                        SendMsg("Pci_WriteDesKey Succeed\n", 1);
                    } else {
                        RESULT_CODE = -1;
                        Log.e(tag, "Pci_WriteDesKey failed, ret = " + ret);
                        SendMsg("Pci_WriteDesKey Failed, ret = " + ret + "\n", 1);
                        break;
                    }
                    break;

                case OPCODE_GET_DES:
                    Log.i(tag, "OPCODE_GET_DES");
                    inData = StringUtil.hexStringToBytes("8EB4B045F6C10642");
                    inLen = 8;
                    keyNo = 0;
                    inData = new byte[inLen];
                    poutData = new byte[inLen];
                    mode = 0; //decryption

                    int Flag = 0;
                    int Mode = 1;
                    byte[] IV= new byte[64];
                    byte[] key= new byte[64];
                    int KeyType =0x00;
                    byte[] Src= new byte[512];
                    byte[] Out= new byte[512];
                    int SrcLen ;
                    IV = StringUtil.hexStringToBytes("12345678");
                    key = StringUtil.hexStringToBytes("1234567890123456ABCDEFGH");
                    Src = StringUtil.hexStringToBytes("E6B6345F1015380284481BBCFFB9052A227FC14F73072E8D5007AC01DFEDCC2BCBCE1EB14A95ED60BA1A44700F4E18AE");
                    SrcLen = 48;
                    ret = posApiHelper.PciTriDes( Flag, Mode,  IV, key,  KeyType,  Src, SrcLen, Out);
                    Log.e(tag, "Pci_GetDes Succeed\npoutData: " + ByteUtil.bytearrayToHexString(Out, Out.length));
                    if (ret == 0) {
                        RESULT_CODE = 0;
                        SendMsg("Pci_GetDes Succeed\nCBCDesOut: " + ByteUtil.bytearrayToHexString(Out,  Out.length), 0);
                    } else {
                        RESULT_CODE = -1;
                        Log.e(tag, "Pci_GetDes failed, ret = " + ret);
                        SendMsg("Pci_GetDes Failed, ret = " + ret + "\n", 0);
                    }
                    break;

                case OPCODE_GET_MAC:
                    Log.i(tag, "OPCODE_GET_MAC");
                    final  byte[] macdata = StringUtil.hexStringToBytes("2681E49B7613EC47252605630FE2CBA227538032618C2A692503C142C88B61BC");
                    byte macdata_Len = 32;
                    macOut = new byte[inLen];
                    mode = 0; //ANSI9.19
                    keyNo = 0;

                    ret = posApiHelper.PciGetMac(keyNo, macdata_Len, macdata, macOut, mode);
                    if (ret == 0) {
                        RESULT_CODE = 0;
                        if(mode == 4)
                            SendMsg("Pci_GetMac Succeed\nmacOut: " + ByteUtil.bytearrayToHexString(macOut, 4), 0);  //get 4 bytes
                        else
                            SendMsg("Pci_GetMac Succeed\nmacOut: " + ByteUtil.bytearrayToHexString(macOut, 8), 0);
                    } else {
                        RESULT_CODE = -1;
                        Log.e(tag, "Pci_GetMac failed, ret = " + ret);
                        SendMsg("Pci_GetMac Failed, ret = " + ret + "\n", 0);
                    }
                    break;

                case OPCODE_GET_KCV:

                    byte MAIN_PINKcv[] = new byte[8];
                    byte MAIN_DESKcv[] = new byte[8];
                    byte MAIN_MACKcv[] = new byte[8];
                    byte MAIN_Key_No = 0;
                    byte MAIN_PIN_Type = 0x0A;
                    byte MAIN_MAC_Type = 0x0B;
                    byte MAIN_DES_Type = 0x0C;


                    byte WORKPINKcv[] = new byte[8];
                    byte WORKDESKcv[] = new byte[8];
                    byte WORKMACKcv[] = new byte[8];
                    byte WORKKey_No = 0;
                    byte WORKKey_PIN_Type = 0x0D;
                    byte WORKKey_MAC_Type = 0x0E;
                    byte WORKKey_DES_Type = 0x0F;

                    ret = posApiHelper.PciReadKcv(MAIN_Key_No, MAIN_PIN_Type, MAIN_PINKcv);
                    ret = posApiHelper.PciReadKcv(MAIN_Key_No, MAIN_DES_Type, MAIN_DESKcv);
                    ret = posApiHelper.PciReadKcv(MAIN_Key_No, MAIN_MAC_Type, MAIN_MACKcv);


                    ret = posApiHelper.PciReadKcv(WORKKey_No, WORKKey_PIN_Type, WORKPINKcv);
                    ret = posApiHelper.PciReadKcv(WORKKey_No, WORKKey_MAC_Type, WORKMACKcv);
                    ret = posApiHelper.PciReadKcv(WORKKey_No, WORKKey_DES_Type, WORKDESKcv);

                    if (ret == 0) {
                        RESULT_CODE = 0;
                        SendMsg("MAIN_PINKcv: " + ByteUtil.bytearrayToHexString(MAIN_PINKcv, 3)+"\r\nMAIN_DESKcv: " + ByteUtil.bytearrayToHexString(MAIN_DESKcv, 3)+
                                "\r\nMAIN_MACKcv: " + ByteUtil.bytearrayToHexString(MAIN_MACKcv, 3)
                                +"\r\n\nWORKPINKcv: " + ByteUtil.bytearrayToHexString(WORKPINKcv, 3)+"\r\nWORKMACKcv: " + ByteUtil.bytearrayToHexString(WORKMACKcv, 3)
                                +"\r\nWORKDESKcv: " + ByteUtil.bytearrayToHexString(WORKDESKcv, 3), 0);
                    } else {
                        RESULT_CODE = -1;
                        Log.e(tag, "PciReadKcv failed, ret = " + ret);
                        SendMsg("PciReadKcv Failed, ret = " + ret + "\n", 0);
                    }
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
