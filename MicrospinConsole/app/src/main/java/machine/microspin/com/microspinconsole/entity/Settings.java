package machine.microspin.com.microspinconsole.entity;

import android.bluetooth.BluetoothDevice;

/**
 * Settings Repo for storing storing throughout the application lifetime
 */

public class Settings {

    public static BluetoothDevice device;
    //===== Settings Parameters =======
    private static int deliverySpeed;
    private static float tensionDraft;
    private static int cylinderSpeed;
    private static float cylinderFeed;
    private static int beaterSpeed;
    private static float beaterFeed;
    private static float conveyorSpeed;
    private static int trunkDelay;
    private static int lengthLimit;
    private static float lengthCorrection;

    private static int savedLength;

    private static String machineId = "01"; //Default
    //==== END: Settings Parameters ====

    final private static String ATTR_COUNT = "01";
    final private static String ATTR_MACHINE_TYPE_CARDING = "01";
    final private static String ATTR_MSG_TYPE_BACKGROUND = "02";
    final private static String ATTR_SCREEN_SUB_STATE_NONE = "00";
    final private static String ATTR_TYPE_BLOWCARD_PATTERN = "10";
    final private static String ATTR_SCREEN_SETTING = "04";
    final private static String ATTR_PACKET_LENGTH = "21"; // HexCode
    final private static int ATTR_LENGTH = 30;

    //Default settings for factory settings
    private static int defaultdeliverySpeed = 10;
    private static float defaulttensionDraft = 10.0f;
    private static int defaultcylinderSpeed = 1500;
    private static float defaultcylinderFeed = 3.5f;
    private static int defaultbeaterSpeed = 950;
    private static float defaultbeaterFeed = 2.5f;
    private static float defaultconveyorSpeed = 2.0f;
    private static int defaulttrunkDelay = 3;
    private static int defaultlengthLimit = 1500;
    private static float defaultlengthCorrection = 1.0f;

    public static String getMachineId (){
        return machineId;
    }
    public static Boolean processSettingsPacket(String payload) {
        if (payload.length() < 4) {
            return false;
        }

        String SOF = payload.substring(0, 2);
        int payloadLength = payload.length();
        String EOF = payload.substring(payloadLength - 2, payloadLength);
        if (!SOF.equals(Packet.START_IDENTIFIER) || !EOF.equals(Packet.END_IDENTIFIER)) {
            return false;
        }

        String sender = payload.substring(2, 4);
        if (!sender.equals(Packet.SENDER_MACHINE)) {
            return false;
        }

        machineId = payload.substring(6, 8);
        /*
        int packetLength = Integer.parseInt(payload.substring(4, 6));
        String machineType = payload.substring(8, 10);
        String msgType = payload.substring(10, 12);
        String nextScreen = payload.substring(12, 14);
        String screenSubState = payload.substring(14, 16);
        int attributeCount = Integer.parseInt(payload.substring(16, 18));
        String attributeType = payload.substring(18, 20);
        int attributeLength = Integer.parseInt(payload.substring(20, 22));
        */

        // Mapping Setting Parameters.
        deliverySpeed = Utility.convertHexToInt(payload.substring(22, 26));
        //float fl = Utility.convertHexToFloat(payload.substring(22, 26));
        tensionDraft = Utility.convertHexToFloat(payload.substring(26, 34));
        cylinderSpeed = Utility.convertHexToInt(payload.substring(34, 38));
        cylinderFeed = Utility.convertHexToFloat(payload.substring(38, 46));
        beaterSpeed = Utility.convertHexToInt(payload.substring(46, 50));
        beaterFeed = Utility.convertHexToFloat(payload.substring(50, 58));
        conveyorSpeed = Utility.convertHexToFloat(payload.substring(58, 66));
        trunkDelay = Utility.convertHexToInt(payload.substring(66, 70));
        lengthLimit = Utility.convertHexToInt(payload.substring(70, 74));
        lengthCorrection = Utility.convertHexToFloat(payload.substring(74, 82));
        savedLength =  Utility.convertHexToInt(payload.substring(82, 86));

        return true;
    }

    public static String updateNewSetting(String s1, String s2, String s3, String s4, String s5, String s6, String s7,String s8, String s9 , String s10) {
        // Update new values in Repo.
        deliverySpeed = Integer.parseInt(s1);
        tensionDraft = Float.parseFloat(s2);
        cylinderSpeed = Integer.parseInt(s3);
        cylinderFeed = Float.parseFloat(s4);
        beaterSpeed = Integer.parseInt(s5);
        beaterFeed = Float.parseFloat(s6);
        conveyorSpeed = Float.parseFloat(s7);
        trunkDelay = Integer.parseInt(s8);
        lengthLimit = Integer.parseInt(s9); ;
        lengthCorrection = Float.parseFloat(s10); ;

        // Construct payload String
        StringBuilder payload = new StringBuilder();

        //Delimiters
        String SOF = Packet.START_IDENTIFIER;
        String EOF = Packet.END_IDENTIFIER;

        String sender = Packet.SENDER_HMI;

        //Getting Packet length
        /*int packetLength =
                machineId.length() +
                        machineType.length() +
                        msgType.length() +
                        nextScreen.length() +
                        screenSubState.length() +
                        attributeCount.length() +
                        attributeLength;*/

        //Construct Attribute payload String
        StringBuilder attrPayload = new StringBuilder();

        attrPayload.append(ATTR_TYPE_BLOWCARD_PATTERN).
                append(String.format("%02d", ATTR_LENGTH));

        String attr = Utility.convertIntToHexString(deliverySpeed);
        attrPayload.append(Utility.formatValueByPadding(attr,2));
        attr = Utility.convertFloatToHex(tensionDraft);
        attrPayload.append(Utility.formatValueByPadding(attr,4));
        attr = Utility.convertIntToHexString(cylinderSpeed);
        attrPayload.append(Utility.formatValueByPadding(attr,2));
        attr = Utility.convertFloatToHex(cylinderFeed);
        attrPayload.append(Utility.formatValueByPadding(attr,4));
        attr = Utility.convertIntToHexString(beaterSpeed);
        attrPayload.append(Utility.formatValueByPadding(attr,2));
        attr = Utility.convertFloatToHex(beaterFeed);
        attrPayload.append(Utility.formatValueByPadding(attr,4));
        attr = Utility.convertFloatToHex(conveyorSpeed);
        //send out conveyor delay and dwell even though we dont use it.
        attrPayload.append(Utility.formatValueByPadding(attr,4));
        attr = Utility.convertIntToHexString(trunkDelay);
        attrPayload.append(Utility.formatValueByPadding(attr,2));
        attr = Utility.convertIntToHexString(lengthLimit);
        attrPayload.append(Utility.formatValueByPadding(attr,2));
        attr = Utility.convertFloatToHex(lengthCorrection);
        attrPayload.append(Utility.formatValueByPadding(attr,4));

        //Construct payload string
        payload.append(SOF).
                append(sender).
                append(ATTR_PACKET_LENGTH).
                append(machineId).
                append(ATTR_MACHINE_TYPE_CARDING).
                append(ATTR_MSG_TYPE_BACKGROUND).
                append(ATTR_SCREEN_SETTING).
                append(ATTR_SCREEN_SUB_STATE_NONE).
                append(ATTR_COUNT).
                append(attrPayload.toString()).
                append(EOF);

        return payload.toString();

    }

    //==========GETTERS============
    public static int getDeliverySpeed() {
        return deliverySpeed;
    }

    public static String getDeliverySpeedString() {
        return String.format("%d", deliverySpeed);
    }

    public static float getTensionDraft() {
        return tensionDraft;
    }

    public static String getTensionDraftString() {
        String s = String.format("%f", tensionDraft);
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }

    public static int getCylinderSpeed() {
        return cylinderSpeed;
    }

    public static String getCylinderSpeedString() {
        return String.format("%d", cylinderSpeed);
    }

    public static float getCylinderFeed() {
        return cylinderFeed;
    }

    public static String getCylinderFeedString() {
        String s = String.format("%f", cylinderFeed);
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }

    public static int getBeaterSpeed() {
        return beaterSpeed;
    }

    public static String getBeaterSpeedString() {
        return String.format("%d", beaterSpeed);
    }

    public static float getBeaterFeed() {
        return beaterFeed;
    }

    public static String getBeaterFeedString() {
        String s = String.format("%f", beaterFeed);
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }

    public static float getConveyorSpeed() {
        return conveyorSpeed;
    }

    public static String getConveyorSpeedString() {
        String s = String.format("%f", conveyorSpeed);
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");

    }

    public static int getTrunkDelay() {
        return trunkDelay;
    }

    public static String getTrunkDelayString() {
        return String.format("%d", trunkDelay);
    }

    public static int getLengthLimit() {
        return lengthLimit;
    }

    public static String getLengthLimitString() {
        return String.format("%d", lengthLimit);
    }

    public static float getLengthCorrection() {
        return lengthLimit;
    }

    public static String getLengthCorrectionString() {

        String s = String.format("%f", lengthCorrection);
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }


    //Default settings calls

    public static String getDefaultDeliverySpeedString() {
        return String.format("%d", defaultdeliverySpeed);
    }

    public static String getDefaultTensionDraftString() {
        String s = String.format("%f", defaulttensionDraft);
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }

    public static String getDefaultCylinderSpeedString() {
        return String.format("%d", defaultcylinderSpeed);
    }

    public static String getDefaultCylinderFeedString() {
        String s = String.format("%f", defaultcylinderFeed);
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }

    public static String getDefaultBeaterSpeedString() {
        return String.format("%d", defaultbeaterSpeed);
    }

    public static String getDefaultBeaterFeedString() {
        String s = String.format("%f", defaultbeaterFeed);
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }

    public static String getDefaultConveyorSpeedString() {
        String s = String.format("%f", defaultconveyorSpeed);
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }

    public static String getDefaultTrunkDelayString() {
        return String.format("%d", defaulttrunkDelay);
    }

    public static String getDefaultLengthLimitString() {
        return String.format("%d", defaultlengthLimit);
    }

    public static String getDefaultLengthCorrectionString() {
        String s = String.format("%f", defaultlengthCorrection);
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }

    public static String getSavedLengthString() {
        return String.format("%d", savedLength);
    }


}
