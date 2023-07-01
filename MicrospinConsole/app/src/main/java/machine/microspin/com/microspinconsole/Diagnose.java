package machine.microspin.com.microspinconsole;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.text.TextUtils;

import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothStatus;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothWriter;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import machine.microspin.com.microspinconsole.entity.IntegerInputFilter;
import machine.microspin.com.microspinconsole.entity.Packet;
import machine.microspin.com.microspinconsole.entity.Pattern;
import machine.microspin.com.microspinconsole.entity.Settings;
import machine.microspin.com.microspinconsole.entity.TLV;
import machine.microspin.com.microspinconsole.entity.Utility;

public class Diagnose extends AppCompatActivity implements View.OnClickListener, BluetoothService.OnBluetoothEventCallback, AdapterView.OnItemSelectedListener, View.OnFocusChangeListener {

    private Spinner testType;
    private Spinner motorCode;
    private EditText runTime;
    private EditText signalValue;
    private EditTextCustom targetRPMPercent;
    private TextView motorCodeLive;
    private TextView maxRpmText;
    private TextView targetRPMOut;

    private TextView signalValueLive;
    private TextView actualRPMLive;
    private TextView testTypeLive;
    private TextView targetTextLive;
    private TextView targetLabelLive;
    private Button runDiagnose;
    private LinearLayout menuLayout;
    private LinearLayout liveLayout;

    private static Boolean isDiagnoseRunning = false;

    //harsha added
    private Snackbar snackbarComplete ;
    private int iCurrentSelection = 0;
    private int maxRPM = 0;
    private int actualRPM = 0;
    private int targetRpmCalc = 0;
    private int targetSignalVoltage = 0;

    private static boolean firstInit = false;
    private static boolean isSnackbarOn = false;
    //=================== STATIC Codes ========================
    final private static String SPINNER_TEST_TYPE = "TEST_TYPE";
    final private static String SPINNER_MOTOR_CODE = "MOTOR_TYPE";

    final private static String LAYOUT_MENU = "MENU";
    final private static String LAYOUT_LIVE = "LIVE";

    private BluetoothService mService;
    private BluetoothWriter mWriter;
    private static final String TAG = "Diagnose";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnose);

        //******* Release Change v2
        if(Settings.device != null) {
            setTitle(Settings.device.getName());
        }

        testType = (Spinner) findViewById(R.id.testType);
        testType.setOnItemSelectedListener(this);

        motorCode = (Spinner) findViewById(R.id.motorValue);
        motorCode.setOnItemSelectedListener(this);

        runTime = (EditText) findViewById(R.id.testRunTime);
        signalValue = (EditText) findViewById(R.id.signalValue);

        targetRPMPercent = (EditTextCustom) findViewById(R.id.targetRpmPercent);
        targetRPMPercent.setOnFocusChangeListener(this);


        maxRpmText = (TextView) findViewById(R.id.maxRpmVal);
        targetRPMOut =  (TextView) findViewById(R.id.targetRPMout);

        testTypeLive = (TextView) findViewById(R.id.typeOfTestLive);
        motorCodeLive = (TextView) findViewById(R.id.motorCodeLive);
        //runTimeLive = (TextView) findViewById(R.id.testTimeLive);
        signalValueLive = (TextView) findViewById(R.id.signalVoltgaeLive);
        actualRPMLive = (TextView) findViewById(R.id.actualRPMLive);
        targetTextLive = (TextView) findViewById(R.id.targetText);
        targetLabelLive = (TextView) findViewById(R.id.targetlabel);

        runDiagnose = (Button) findViewById(R.id.runDiagnose);
        runDiagnose.setOnClickListener(this);

        menuLayout = (LinearLayout) findViewById(R.id.diagnoseMenu);
        liveLayout = (LinearLayout) findViewById(R.id.diagnoseLive);

        List<String> motorCodeList = getValueListForSpinner(SPINNER_MOTOR_CODE);
        motorCode.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, motorCodeList));
        List<String> testTypeList = getValueListForSpinner(SPINNER_TEST_TYPE);
        testType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, testTypeList));

        mService = BluetoothService.getDefaultInstance();
        mWriter = new BluetoothWriter(mService);

        setDefaultValue();
        toggleViewOn(LAYOUT_MENU);

        mWriter.writeln(Pattern.DISABLE_MACHINE_START_DIAGNOSE.toUpperCase());
    }

    //====================================== OTHER EVENTS ==========================================
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.runDiagnose:
                signalValue.setEnabled(true);
                targetRPMPercent.setEnabled(true);
                runDiagnose();
                break;
        }
    }

    //Spinner TestType Events
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        if (firstInit == false) {
            firstInit = true;
            return;
        } else {
            if (parent.getId() == R.id.testType) {
                if (pos == 0) {
                    signalValue.setEnabled(true);
                    targetRPMPercent.setEnabled(false);
                    targetRPMPercent.setText("0");
                    targetRPMOut.setText("0");
                } else {
                    signalValue.setEnabled(false);
                    signalValue.setText("0");
                    targetRPMPercent.setEnabled(true);
                }
            }
            if (parent.getId() == R.id.motorValue) {
                String motorSelectedType = Utility.formatStringCode((motorCode.getSelectedItem().toString()));
                //put the logic here only for what the maxRpm should be
                //COILER and CAGE  maxRPM is 3000
                //EVERYONE ELSE max RPM  is 1500
                maxRPM = GetMaxRPM(motorSelectedType);
                maxRpmText.setText(Utility.formatString(Integer.toString(maxRPM)));
            }
        }
    }


        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }


    //==================================== CUSTOM FUNCTIONS ========================================
    private void UpdateEnabledTestType(){
        String testTypeSelectedType = Utility.formatStringCode((testType.getSelectedItem().toString()));
        if (testTypeSelectedType.equals(Pattern.DiagnosticTestTypes.OPEN_LOOP.toString())){
            signalValue.setEnabled(true);
            targetRPMPercent.setEnabled(false);
        }
        if (testTypeSelectedType.equals(Pattern.DiagnosticTestTypes.CLOSED_LOOP.toString())){
            signalValue.setEnabled(false);
            targetRPMPercent.setEnabled(true);
        }
    }

    private int GetMaxRPM(String motorSelected)
    {   int maxRpm1 = 1500;
        /*if ((motorSelected.equals(Pattern.MotorTypes.CAGE.toString())) || (motorSelected.equals(Pattern.MotorTypes.COILER.toString()))) {
            maxRpm1 = 3000;
        }else {
            maxRpm1 = 1500;
        }*/
        return maxRpm1;
    }

    private void runDiagnose() {
        Packet diagnosePacket = new Packet(Packet.OUTGOING_PACKET);
        TLV[] attributes = new TLV[5];  //Specified in the requirements
        String attrType;
        String attrValue;
        String attrLength;

        //****** Handling=> testType Attribute
        attrType = Pattern.diagnoseAttrTypesMap.get(Pattern.DiagnosticAttrType.KIND_OF_TEST.name());
        attrLength = Pattern.ATTR_LENGTH_02;
        String testTypeSelected = Utility.formatStringCode(testType.getSelectedItem().toString());
        attrValue = Pattern.diagnoseTestTypesMap.get(testTypeSelected);
        TLV testType = new TLV(attrType, attrLength, attrValue);
        attributes[0] = testType;

        //****** Handling=> MotorCode Attribute
        attrType = Pattern.diagnoseAttrTypesMap.get(Pattern.DiagnosticAttrType.MOTOR_ID.name());
        attrLength = Pattern.ATTR_LENGTH_02;
        String motorCodeSelected = Utility.formatStringCode(motorCode.getSelectedItem().toString());
        attrValue = Pattern.motorMap.get(motorCodeSelected);
        TLV motorCode = new TLV(attrType, attrLength, attrValue);
        attributes[1] = motorCode;

        //****** Handling=> Signal Voltage Attribute
        attrType = Pattern.diagnoseAttrTypesMap.get(Pattern.DiagnosticAttrType.SIGNAL_VOLTAGE.name());
        attrLength = Pattern.ATTR_LENGTH_02;
        if (TextUtils.isEmpty(signalValue.getText().toString()))
        {
            signalValue.setText("0");
        }
        targetSignalVoltage = Integer.parseInt(signalValue.getText().toString());
        attrValue = Utility.convertIntToHexString(targetSignalVoltage);
        Log.d(TAG,attrValue);
        TLV signalValue = new TLV(attrType, attrLength, attrValue);
        attributes[2] = signalValue;

        //****** Handling=> Target RPM Attribute
        attrType = Pattern.diagnoseAttrTypesMap.get(Pattern.DiagnosticAttrType.TARGET_RPM.name());
        attrLength = Pattern.ATTR_LENGTH_02;
        if (TextUtils.isEmpty(targetRPMOut.getText().toString()))
        {
            targetRPMPercent.setText("0");
            targetRPMOut.setText("0");
        }
        Integer i2 = Integer.parseInt(targetRPMOut.getText().toString());
        attrValue = Utility.convertIntToHexString(i2);
        TLV targetRPM = new TLV(attrType, attrLength, attrValue);
        attributes[3] = targetRPM;

        //******* Handling=> RunTime Attribute
        attrType = Pattern.diagnoseAttrTypesMap.get(Pattern.DiagnosticAttrType.TEST_TIME.name());
        attrLength = Pattern.ATTR_LENGTH_02;
        if (TextUtils.isEmpty(runTime.getText().toString()))
        {
            runTime.setText("0");
        }
        Integer i3 = Integer.parseInt(runTime.getText().toString());
        attrValue = Utility.convertIntToHexString(i3);
        
        TLV runTime = new TLV(attrType, attrLength, attrValue);
        attributes[4] = runTime;

        String screen = Utility.ReverseLookUp(Pattern.screenMap, Pattern.Screen.DIAGNOSTICS.name());
        String machineId = Settings.getMachineId();
        String machineType = Utility.ReverseLookUp(Pattern.machineTypeMap, Pattern.MachineType.CARDING_MACHINE.name());
        String messageType = Utility.ReverseLookUp(Pattern.messageTypeMap, Pattern.MessageType.BACKGROUND_DATA.name());
        String screenSubState = Pattern.COMMON_NONE_PARAM;

        //****CHANGE FOR RELEASE V2
        //Data Validation for Test RPM & Signal Voltage
        String validateMessage = isValidData();
        if (validateMessage == null) {
            String payload = diagnosePacket.makePacket(screen,
                    machineId,
                    machineType,
                    messageType,
                    screenSubState,
                    attributes);

            mWriter.writeln(payload.toUpperCase());
        } else {
            Snackbar.make(getWindow().getDecorView().getRootView(), validateMessage, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            UpdateEnabledTestType();
        }
    }

    private List<String> getValueListForSpinner(final String entity) {
        List<String> list = new ArrayList<>();
        int length;
        switch (entity) {
            case "MOTOR_TYPE":
                length = Pattern.MotorTypes.values().length;
                while (length > 0) {
                    String value = Pattern.MotorTypes.values()[length - 1].name();
                    list.add(Utility.formatString(value));
                    length--;
                }
                break;
            case "TEST_TYPE":
                length = Pattern.DiagnosticTestTypes.values().length;
                while (length > 0) {
                    String value = Pattern.DiagnosticTestTypes.values()[length - 1].name();
                    list.add(Utility.formatString(value));
                    length--;
                }
                break;
        }
        return list;
    }

    private void toggleEnableMode(final Boolean bol) {
        runOnUiThread(new Runnable() {
            public void run() {
                testType.setEnabled(bol);
                motorCode.setEnabled(bol);
                runTime.setEnabled(bol);
                signalValue.setEnabled(bol);
                targetRPMPercent.setEnabled(bol);
                if (bol) {
                    setDefaultValue();
                    runDiagnose.setVisibility(View.VISIBLE);
                } else {
                    runDiagnose.setVisibility(View.INVISIBLE);
                }
            }
        });

    }

    private void toggleViewOn(final String layout) {
        runOnUiThread(new Runnable() {
            public void run() {
                switch (layout) {
                    case LAYOUT_LIVE:
                        if (liveLayout.getVisibility() == View.INVISIBLE) {
                            menuLayout.setVisibility(View.INVISIBLE);
                            liveLayout.setVisibility(View.VISIBLE);
                        }
                        break;
                    case LAYOUT_MENU:
                        menuLayout.setVisibility(View.VISIBLE);
                        liveLayout.setVisibility(View.INVISIBLE);
                        break;
                }
            }
        });

    }

    private void setDefaultValue() {
        runOnUiThread(new Runnable() {
            public void run() {
                runTime.setText("0");
                signalValue.setText("0");
                targetRPMPercent.setText("0");
                targetRPMPercent.setEnabled(false); // on start the default is open loop
                targetRPMOut.setText("0");
                //-----------------------//
                // set the correct max Rpm in the menu screen.
                String motorSelectedType = Utility.formatStringCode((motorCode.getSelectedItem().toString()));
                //put the logic here only for what the maxRpm should be
                //everyone is 1500 for philli2
                maxRPM = GetMaxRPM(motorSelectedType);
                maxRpmText.setText(Utility.formatString(Integer.toString(maxRPM)));
                //----------------------//
                testTypeLive.setText("0");
                motorCodeLive.setText("0");
                //runTimeLive.setText("0");
                signalValueLive.setText("0");
                actualRPMLive.setText("0");

            }
        });

    }

    private void updateLiveData(final TLV[] attributes) {
        runOnUiThread(new Runnable() {
            public void run() {
                String testType = Utility.formatValueByPadding(attributes[0].getValue(), 1);
                testType = Utility.ReverseLookUp(Pattern.diagnoseTestTypesMap, testType);
                String motorCode = Utility.formatValueByPadding(attributes[1].getValue(), 1);
                motorCode = Utility.ReverseLookUp(Pattern.motorMap, motorCode);
                String signalVoltage = attributes[2].getValue();
                String targetRPM = attributes[3].getValue();
                //String testRunTime = attributes[0].getValue();

                testTypeLive.setText(Utility.formatString(testType));
                motorCodeLive.setText(Utility.formatString(motorCode));

                if (testType.equals(Pattern.DiagnosticTestTypes.OPEN_LOOP.toString())){
                    targetLabelLive.setText("Target Signal Voltage %");
                    targetTextLive.setText(Integer.toString(targetSignalVoltage));
                }
                if (testType.equals(Pattern.DiagnosticTestTypes.CLOSED_LOOP.toString())){
                    targetLabelLive.setText("Target RPM ");
                    targetTextLive.setText(Integer.toString(targetRpmCalc));
                }

                signalValueLive.setText(signalVoltage);
                actualRPMLive.setText(targetRPM);
                //runTimeLive.setText(testRunTime);
            }
        });

    }

    //================================== BLUETOOTH EVENT CALLBACKS =================================
    @Override
    public void onDataRead(byte[] bytes, int i) {

        if (isDiagnoseRunning) {
            try {
                final String payload = new String(bytes, "UTF-8").replaceAll("(\\r|\\n)", "");

                if (!payload.isEmpty()) {
                    String packPayload;
                    switch (payload) {
                        case "XXDLIVEC":
                            packPayload = "7E0112010102050004010200010202000103020000030201097E";
                            break;
                        case "XXDLIVEO":
                            packPayload = "7E011201010205000401020002020200020302001C030204B07E";
                            break;
                        case "XXDSUCCESS":
                            packPayload = "7E010B010102059800000000007E";
                            break;
                        case "XXDFAIL":
                            packPayload = "7E010B010102059700000000007E";
                            break;
                        case "XXDINCOMPLETE":
                            packPayload = "7E010B010102059600000000007E";
                            break;
                        default:
                            packPayload = payload;
                    }
                    //packPayload = payload;
                    if (packPayload.length() >= 20) { //size of header is 20 . Min Size of packet 20
                        if (Packet.getHeadersScreen(packPayload).equals(Pattern.Screen.DIAGNOSTICS.name())) {
                            if (Packet.getHeadersSubScreen(packPayload).equals(Pattern.ScreenSubState.DIAG_SUCCESS.name())) {
                                isDiagnoseRunning = false;
                                snackbarComplete = Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_diagnose_complete_success, Snackbar.LENGTH_INDEFINITE);
                                // change snackbar text color
                                View snackbarView = snackbarComplete.getView();
                                int snackbarTextId = android.support.design.R.id.snackbar_text;
                                TextView textView = (TextView)snackbarView.findViewById(snackbarTextId);
                                textView.setTextColor(getResources().getColor(R.color.colorPrimary));

                                snackbarComplete.setAction("Action", null).show();
                                isSnackbarOn = true;
                                /*toggleEnableMode(true);
                                toggleViewOn(LAYOUT_MENU);*/ //Moved to onBackPressed
                            }
                            else if (Packet.getHeadersSubScreen(packPayload).equals(Pattern.ScreenSubState.DIAG_FAIL.name())) {
                                isDiagnoseRunning = false;
                                snackbarComplete = Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_diagnose_complete_fail, Snackbar.LENGTH_INDEFINITE);

                                // change snackbar text color
                                View snackbarView = snackbarComplete.getView();
                                int snackbarTextId = android.support.design.R.id.snackbar_text;
                                TextView textView = (TextView)snackbarView.findViewById(snackbarTextId);
                                textView.setTextColor(getResources().getColor(R.color.colorAccent));

                                snackbarComplete.setAction("Action", null).show();
                                isSnackbarOn = true;
                                /*toggleEnableMode(true);
                                toggleViewOn(LAYOUT_MENU);*/ //Moved to onBackPressed
                            }
                            else if (
                                Packet.getHeadersSubScreen(packPayload).equals(Pattern.ScreenSubState.DIAG_INCOMPLETE.name())) {
                                isDiagnoseRunning = false;
                                snackbarComplete = Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_diagnose_Incomplete, Snackbar.LENGTH_INDEFINITE);
                                // change snackbar text color
                                View snackbarView = snackbarComplete.getView();
                                int snackbarTextId = android.support.design.R.id.snackbar_text;
                                TextView textView = (TextView)snackbarView.findViewById(snackbarTextId);
                                textView.setTextColor(getResources().getColor(R.color.darkOrange));

                                snackbarComplete.setAction("Action", null).show();
                                isSnackbarOn = true;

                                /*toggleEnableMode(true);
                                toggleViewOn(LAYOUT_MENU);*/ //Moved to onBackPressed
                            }

                            else {
                                toggleViewOn(LAYOUT_LIVE);
                                Packet packet = new Packet(Packet.INCOMING_PACKET);
                                if (packet.processIncomePayload(packPayload)) {
                                    TLV[] attr = packet.getAttributes();
                                    updateLiveData(attr);
                                }
                            }
                        }
                    }
                }
            } catch (UnsupportedEncodingException | NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }
        }
    }

    @Override
    public void onStatusChange(BluetoothStatus bluetoothStatus) {
        if (bluetoothStatus == BluetoothStatus.NONE) {
            Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_bluetooth_disconnected, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        }
    }

    @Override
    public void onDeviceName(String s) {

    }

    @Override
    public void onToast(String s) {

    }

    @Override
    public void onDataWrite(byte[] bytes) {
        try {
            String payload = new String(bytes, "UTF-8").replaceAll("(\\r|\\n)", "");
            if (!payload.equals(Pattern.DISABLE_MACHINE_START_DIAGNOSE)) {
                Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_diagnose_running, Snackbar.LENGTH_INDEFINITE)
                        .setAction("Action", null).show();

                toggleEnableMode(false);
                isDiagnoseRunning = true;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private String isValidData() {
        IntegerInputFilter set1 = new IntegerInputFilter(getString(R.string.label_diagnose_ip_signal), 10, 90);
        IntegerInputFilter set2 = new IntegerInputFilter(getString(R.string.label_diagnose_target_rpm_percent), 10, 90);
        IntegerInputFilter set3 = new IntegerInputFilter(getString(R.string.label_diagnose_run_time), 30, 300);

        String testTypeSelected = Utility.formatStringCode((testType.getSelectedItem().toString()));
        //only check the box you want to use
        if (testTypeSelected.equals(Pattern.DiagnosticTestTypes.OPEN_LOOP.toString())) {
            if (set1.filter(signalValue) != null) {
                return set1.filter(signalValue);
            }
        }

        if (testTypeSelected.equals(Pattern.DiagnosticTestTypes.CLOSED_LOOP.toString())) {
            if (set2.filter(targetRPMPercent) != null) {
                return set2.filter(targetRPMPercent);
            }
        }

        if (set3.filter(runTime) != null) {
            return set3.filter(runTime);
        }
        return null;
    }


    //===================================== ACTIVITY EVENTS ========================================
    @Override
    protected void onResume() {
        super.onResume();
        mService.setOnEventCallback(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       // mService.disconnect();
    }

    @Override
    public void onBackPressed() {
        //Back navigation to Menu Screen.
        //Dont care if the snack bar is shown or not, cos we are closing the app.
        /*if (isSnackbarOn == true) {
            if (snackbarVersion.isShown()) {
                snackbarVersion.dismiss();
            }
        }*/
        // always go back to idle mode on one press
        mWriter.writeln(Pattern.ENABLE_MACHINE_START.toUpperCase());
        //super.onBackPressed();

        //finish this activity
        finish();

    }


    @Override
    public void onFocusChange(View view, boolean b) {
        if (!b) {
            if (TextUtils.isEmpty(targetRPMPercent.getText().toString())) {
                targetRPMOut.setText(Integer.toString(0));
                targetRPMPercent.setText(Integer.toString(0));
            } else {
                int currentTargetPercent = Integer.parseInt(targetRPMPercent.getText().toString());
                targetRpmCalc = (currentTargetPercent * maxRPM) / 100;
                targetRPMOut.setText(Integer.toString(targetRpmCalc));
            }
        }
    }

}


