package machine.microspin.com.microspinconsole.fragment;

import android.content.Context;
import android.nfc.Tag;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import machine.microspin.com.microspinconsole.R;
import machine.microspin.com.microspinconsole.entity.Packet;
import machine.microspin.com.microspinconsole.entity.Pattern;
import machine.microspin.com.microspinconsole.entity.SettingsCommunicator;
import machine.microspin.com.microspinconsole.entity.TLV;
import machine.microspin.com.microspinconsole.entity.Utility;

/**
 * Fragment 1 - RUN MODE STATUS
 * Shows the Status details sent by the connected ble device.
 */

public class RunModeFragment extends Fragment implements Pattern, View.OnClickListener {

    public TextView statusText, attr1Value, attr2Value, attr3Value, reasonText, reasonTypeText, errorText;
    public TextView valueValue,savedLengthText;
    public LinearLayout stopLayout, runLayout, idleLayout, statusBox, errorBox;
    public LinearLayout attr1Box, attr2Box, attr3Box, valueBox;
    public Button diagnoseBtn;
    public ImageButton resetLengthTopBtn,resetLengthIdleBtn;
    private int canEdit = 0; // 0 is nothing can be edited, 1 is everysetting can be edited, 2 is run mode settings can be edited
    private SettingsCommunicator mCallback;
    private Boolean haltMessageIsOpen = false;
    public Boolean updateMainStatus = true;
    private Toast toast = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_run_mode, container, false);
        resetLengthTopBtn = (ImageButton) rootView.findViewById(R.id.resetLengthTop);

        //=>Run Screen Layout
        statusText = (TextView) rootView.findViewById(R.id.statusText);
        attr1Value = (TextView) rootView.findViewById(R.id.attr1Value);
        attr2Value = (TextView) rootView.findViewById(R.id.attr2Value);
        attr3Value = (TextView) rootView.findViewById(R.id.attr3Value);
        //=>Stop Screen Layout
        reasonText = (TextView) rootView.findViewById(R.id.reasonText);
        reasonTypeText = (TextView) rootView.findViewById(R.id.reasonTypeLabel);
        errorText = (TextView) rootView.findViewById(R.id.motorErrorCode);
        valueValue = (TextView) rootView.findViewById(R.id.valueValue);
        //=>Idle Screen Layout
        diagnoseBtn = (Button) rootView.findViewById(R.id.diagnoseBtn);
        savedLengthText = (TextView) rootView.findViewById(R.id.savedLengthIdleText);
        resetLengthIdleBtn = (ImageButton) rootView.findViewById(R.id.resetLengthIdleBtn);
        //=>Layout Reference
        stopLayout = (LinearLayout) rootView.findViewById(R.id.stopLayout);
        runLayout = (LinearLayout) rootView.findViewById(R.id.runLayout);
        idleLayout = (LinearLayout) rootView.findViewById(R.id.idleLayout);
        statusBox = (LinearLayout) rootView.findViewById(R.id.statusBox);
        errorBox = (LinearLayout) rootView.findViewById(R.id.errorBox);
        //=>Attribute Boxes
        attr1Box = (LinearLayout) rootView.findViewById(R.id.attr1Box);
        attr2Box = (LinearLayout) rootView.findViewById(R.id.attr2Box);
        attr3Box = (LinearLayout) rootView.findViewById(R.id.attr3Box);
        valueBox = (LinearLayout) rootView.findViewById(R.id.valueBox);

        diagnoseBtn.setOnClickListener(this);
        resetLengthTopBtn.setOnClickListener(this);
        resetLengthIdleBtn.setOnClickListener(this);

        stopLayout.setVisibility(View.INVISIBLE);
        runLayout.setVisibility(View.INVISIBLE);
        idleLayout.setVisibility(View.INVISIBLE);

        return rootView;
    }

    public void updateContent(final String payload) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                Packet packet = new Packet(Packet.INCOMING_PACKET);
                updateMainStatus = true;
                //int attrCount = packet.getAttributeCount();
                if (packet.processIncomePayload(payload)) {
                    statusBox.setVisibility(View.VISIBLE);

                    TLV[] attr = packet.getAttributes();
                    if (packet.getNextScreen().equals(Screen.RUN.name())) {
                        toggleVisibility(Screen.RUN.name());
                        resetLengthTopBtn.setVisibility(View.VISIBLE);
                        attr1Value.setText(attr[2].getValue()); // CurrentLength
                        attr2Value.setText(attr[1].getValue()); // Cylinder RPM
                        attr3Value.setText(attr[0].getValue()); // Beater
                        haltMessageIsOpen = false;
                        canEdit = 2; // run mode edit settings
                    }
                    if (packet.getNextScreen().equals(Screen.STOP.name())) {
                        errorBox.setVisibility(View.INVISIBLE);
                        valueBox.setVisibility(View.INVISIBLE);
                        toggleVisibility(Screen.STOP.name());
                        canEdit = 2;//Kamar wanted setings to be editable in pause state also. so make this 2 from 0.
                        //=>Display Stop Reason & Data, if present
                        if (!attr[0].getType().isEmpty() || !attr[0].getType().equals("")) { // this is reason, rpm error etc...
                            if (attr[0].getType().equals(StopMessageType.REASON.name())) {
                                String attr0 = Utility.formatValueByPadding(attr[0].getValue(), 2);
                                reasonText.setText(Utility.formatString(Pattern.stopReasonValueMap.get(attr0)));
                                if (attr[1].getType().equals(StopMessageType.MOTOR_ERROR_CODE.name())) { // this is what kind of error
                                    reasonTypeText.setText(getString(R.string.label_stop_motor));
                                    errorBox.setVisibility(View.VISIBLE);
                                    String attr1 = Utility.formatValueByPadding(attr[1].getValue(), 2);
                                    errorText.setText(Utility.formatString(Pattern.motorErrorCodeMap.get(attr1)));
                                    resetLengthTopBtn.setVisibility(View.INVISIBLE); // MAKE THE RESET LENGTH BUTTON INVISIBLE IN THE HALT SCREENS
                                }else {
                                    reasonTypeText.setText(getString(R.string.label_stop_reason));
                                    resetLengthTopBtn.setVisibility(View.VISIBLE);
                                }

                                if (packet.getScreenSubState().equals(ScreenSubState.HALT.name())) {
                                    if (!haltMessageIsOpen) {
                                        mCallback.raiseMessage(getString(R.string.msg_halt_restart));
                                        haltMessageIsOpen = true;
                                    }
                                } else {
                                    haltMessageIsOpen = false;
                                }
                            }

                            } else { // this is what gives the USER PAUSE type of messages (only one box)
                                reasonText.setVisibility(View.INVISIBLE);
                                errorBox.setVisibility(View.INVISIBLE);
                            }
                        }
                    if (packet.getNextScreen().equals(Screen.IDLE.name())) {
                        canEdit = 1;
                        mCallback.updateIdleModeStatus(true);
                        toggleVisibility(Screen.IDLE.name());
                        resetLengthTopBtn.setVisibility(View.INVISIBLE);

                        if (mCallback.IdleLengthResetStatus()==true){ // to make the reset text zero when the reset button is pressed
                                savedLengthText.setText("0");
                        }

                        if (packet.getScreenSubState().equals(ScreenSubState.IDLE_CYL_ROTATING.name())) {
                            if (toast == null) { // first time
                                toast = Toast.makeText(getActivity(), "Please Wait Till All Motors Stop!", Toast.LENGTH_LONG);
                              }

                            try { // might be able to remove this
                                  if (toast.getView().isShown() == false) {
                                      toast.show();
                                  }
                            }
                            catch (Exception e) {

                            }

                            //RESET THE SCREEN SUBSTATE TO JUST IDLE
                            updateMainStatus = false; //we re using the same place as the idle text place to send this msg. so
                            //when we get this msg we want to skip updating the idle status.
                        }



                        haltMessageIsOpen = false;
                    }else{
                        mCallback.updateIdleModeStatus(false);
                    }

                    if (updateMainStatus) {
                        statusText.setText(Utility.formatString(packet.getScreenSubState()));
                    }
                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (SettingsCommunicator) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement SettingsCommunicator");
        }
    }

    @Override
    public void onDetach() {
        mCallback = null;
        super.onDetach();
    }

    public void toggleVisibility(String screenName) {

        if (screenName.equals(Screen.STOP.name())) {
            runLayout.setVisibility(View.INVISIBLE);
            idleLayout.setVisibility(View.INVISIBLE);
            stopLayout.setVisibility(View.VISIBLE);
        } else if (screenName.equals(Screen.RUN.name())) {
            stopLayout.setVisibility(View.INVISIBLE);
            idleLayout.setVisibility(View.INVISIBLE);
            runLayout.setVisibility(View.VISIBLE);
        } else if (screenName.equals(Screen.IDLE.name())) {
            stopLayout.setVisibility(View.INVISIBLE);
            idleLayout.setVisibility(View.VISIBLE);
            runLayout.setVisibility(View.INVISIBLE);
        }
    }

    public int canEditSettings() {
        return canEdit;
    }

    @Override
    public void onClick(View view) {
        mCallback.onViewChangeClick(view);
    }
}
