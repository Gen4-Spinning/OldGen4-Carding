package machine.microspin.com.microspinconsole.fragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.text.TextUtils;

import machine.microspin.com.microspinconsole.R;
import machine.microspin.com.microspinconsole.entity.DoubleInputFilter;
import machine.microspin.com.microspinconsole.entity.IntegerInputFilter;
import machine.microspin.com.microspinconsole.entity.SettingsCommunicator;
import machine.microspin.com.microspinconsole.entity.Settings;

/**
 * Fragment to handle Settings (Editable and non Editable)
 */

public class SettingsFragment extends Fragment implements View.OnClickListener {
    private SettingsCommunicator mCallback;
    public EditText setting1, setting2, setting3, setting4, setting5, setting6, setting7,setting8,setting9,setting10;
    public Button saveBtn,factorystngsBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frament_settings, container, false);

        setting1 = (EditText) rootView.findViewById(R.id.setting1);
        setting2 = (EditText) rootView.findViewById(R.id.setting2);
        setting3 = (EditText) rootView.findViewById(R.id.setting3);
        setting4 = (EditText) rootView.findViewById(R.id.setting4);
        setting5 = (EditText) rootView.findViewById(R.id.setting5);
        setting6 = (EditText) rootView.findViewById(R.id.setting6);
        setting7 = (EditText) rootView.findViewById(R.id.setting7);
        setting8 = (EditText) rootView.findViewById(R.id.setting8);
        setting9 = (EditText) rootView.findViewById(R.id.setting9);
        setting10 = (EditText) rootView.findViewById(R.id.setting10);


        saveBtn = (Button) rootView.findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(this);

        factorystngsBtn = (Button) rootView.findViewById(R.id.factorystngs);
        factorystngsBtn.setOnClickListener(this);

        setStatusInputFields(false);
        return rootView;
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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.saveBtn) {
            if (TextUtils.isEmpty(setting1.getText().toString()))
            {
                setting1.setText("0");
            }
            if (TextUtils.isEmpty(setting2.getText().toString()))
            {
                setting2.setText("0");
            }
            if (TextUtils.isEmpty(setting3.getText().toString()))
            {
                setting3.setText("0");
            }
            if (TextUtils.isEmpty(setting4.getText().toString()))
            {
                setting4.setText("0");
            }
            if (TextUtils.isEmpty(setting5.getText().toString()))
            {
                setting5.setText("0");
            }
            if (TextUtils.isEmpty(setting6.getText().toString()))
            {
                setting6.setText("0");
            }
            if (TextUtils.isEmpty(setting6.getText().toString()))
            {
                setting6.setText("0");
            }
            if (TextUtils.isEmpty(setting7.getText().toString()))
            {
                setting7.setText("0");
            }
            if (TextUtils.isEmpty(setting8.getText().toString()))
            {
                setting8.setText("0");
            }
            if (TextUtils.isEmpty(setting9.getText().toString()))
            {
                setting9.setText("0");
            }
            if (TextUtils.isEmpty(setting10.getText().toString()))
            {
                setting10.setText("0");
            }
            String validateMessage = isValidSettings();
            if( validateMessage == null) {
                String payload = Settings.updateNewSetting(
                        setting1.getText().toString(),
                        setting2.getText().toString(),
                        setting3.getText().toString(),
                        setting4.getText().toString(),
                        setting5.getText().toString(),
                        setting6.getText().toString(),
                        setting7.getText().toString(),
                        setting8.getText().toString(),
                        setting9.getText().toString(),
                        setting10.getText().toString()
                );
                mCallback.onSettingsUpdate(payload.toUpperCase());
            }else{
                mCallback.raiseMessage(validateMessage);
            }

        }
        if (v.getId() == R.id.factorystngs){
            setting1.setText(Settings.getDefaultDeliverySpeedString());
            setting2.setText(Settings.getDefaultTensionDraftString());
            setting3.setText(Settings.getDefaultCylinderSpeedString());
            setting4.setText(Settings.getDefaultCylinderFeedString());
            setting5.setText(Settings.getDefaultBeaterSpeedString());
            setting6.setText(Settings.getDefaultBeaterFeedString());
            setting7.setText(Settings.getDefaultConveyorSpeedString());
            setting8.setText(Settings.getDefaultTrunkDelayString());
            setting9.setText(Settings.getDefaultLengthLimitString());
            setting10.setText(Settings.getDefaultLengthCorrectionString());
        }
    }

    private String isValidSettings() {

        IntegerInputFilter set1 = new IntegerInputFilter(getString(R.string.label_delivery_speed), 7, 25);
        IntegerInputFilter set2 = new IntegerInputFilter(getString(R.string.label_tension_draft), 1, 30);
        IntegerInputFilter set3 = new IntegerInputFilter(getString(R.string.label_cylinder_speed), 500, 1500);
        DoubleInputFilter set4 = new DoubleInputFilter(getString(R.string.label_cylinder_feed), 1.0, 6.0);
        IntegerInputFilter set5 = new IntegerInputFilter(getString(R.string.label_beater_speed), 500, 1500);
        DoubleInputFilter set6 = new DoubleInputFilter(getString(R.string.label_beater_feed), 1.0, 6.0);
        DoubleInputFilter set7 = new DoubleInputFilter(getString(R.string.label_conveyor_speed), 1.0, 6.0); // should this be float also?
        IntegerInputFilter set8 = new IntegerInputFilter(getString(R.string.label_trunk_delay), 1, 5);
        IntegerInputFilter set9 = new IntegerInputFilter(getString(R.string.label_length_limit), 10, 2000);
        DoubleInputFilter set10 = new DoubleInputFilter(getString(R.string.label_length_correction), 0.1, 2.0);

        if(set1.filter(setting1) != null){
            return set1.filter(setting1);
        }
        if(set2.filter(setting2) != null){
            return set2.filter(setting2);
        }
        if(set3.filter(setting3) != null){
            return set3.filter(setting3);
        }
        if(set4.filter(setting4) != null){
            return set4.filter(setting4);
        }
        if(set5.filter(setting5) != null){
            return set5.filter(setting5);
        }
        if(set6.filter(setting6) != null){
            return set6.filter(setting6);
        }
        if(set7.filter(setting7) != null){
            return set7.filter(setting7);
        }
        if(set8.filter(setting8) != null){
            return set8.filter(setting8);
        }
        if(set9.filter(setting9) != null){
            return set9.filter(setting9);
        }
        if(set10.filter(setting10) != null){
            return set10.filter(setting10);
        }
        return null;
    }

    public void isEditMode(int isEdit) {
        if (isEdit == 1) {
            //Make settings editable
            saveBtn.setVisibility(View.VISIBLE);
            factorystngsBtn.setVisibility(View.VISIBLE);
            setStatusInputFields(true);
        } else if (isEdit == 0){
            //Make settings non editable.
            saveBtn.setVisibility(View.INVISIBLE);
            factorystngsBtn.setVisibility(View.INVISIBLE);
            setStatusInputFields(false);
        }
        else{ // if is Edit is 1
            saveBtn.setVisibility(View.VISIBLE);
            factorystngsBtn.setVisibility(View.INVISIBLE);
            setStatusInputFieldsRunMode();
        }
    }

    public void updateContent() {
        setting1.setText(Settings.getDeliverySpeedString());
        setting2.setText(Settings.getTensionDraftString());
        setting3.setText(Settings.getCylinderSpeedString());
        setting4.setText(Settings.getCylinderFeedString());
        setting5.setText(Settings.getBeaterSpeedString());
        setting6.setText(Settings.getBeaterFeedString());
        setting7.setText(Settings.getConveyorSpeedString());
        setting8.setText(Settings.getTrunkDelayString());
        setting9.setText(Settings.getLengthLimitString());
        setting10.setText(Settings.getLengthCorrectionString());

    }

    public void setStatusInputFields(Boolean bol) {
        setting1.setEnabled(bol);
        setting2.setEnabled(bol);
        setting3.setEnabled(bol);
        setting4.setEnabled(bol);
        setting5.setEnabled(bol);
        setting6.setEnabled(bol);
        setting7.setEnabled(bol);
        setting7.setEnabled(bol);
        setting8.setEnabled(bol);
        setting9.setEnabled(bol);
        setting10.setEnabled(bol);
    }

    public void setStatusInputFieldsRunMode() {
        setting1.setEnabled(true);
        setting2.setEnabled(true);
        setting3.setEnabled(false);
        setting4.setEnabled(true);
        setting5.setEnabled(false);
        setting6.setEnabled(true);
        setting7.setEnabled(true);
        setting7.setEnabled(true);
        setting8.setEnabled(true);
        setting9.setEnabled(true);
        setting10.setEnabled(true);
    }


}
