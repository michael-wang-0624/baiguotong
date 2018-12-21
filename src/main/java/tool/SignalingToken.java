package tool;

import io.agora.media.AccessToken;
import io.agora.media.SimpleTokenBuilder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SignalingToken {
    private static final String appId = "20aec784743645a6a48d0c2fdccc381d";
    private static final String app_cert = "addf4632548246b585ac5e640bf323dd";

    //private static final String appId = "20aec784743645a6a48d0c2fdccc381d";
    //private static final String app_cert = "addf4632548246b585ac5e640bf323dd";


    public static String getToken(   String account) throws NoSuchAlgorithmException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-mm-dd");
        Calendar cal = null;
        try {
            Date date = simpleDateFormat.parse("2019-12-30");
             cal = Calendar.getInstance();
            cal.setTime(date);
        }catch (Exception e) {
            e.printStackTrace();
        }


        int timestamp = (int)(cal.getTimeInMillis()/1000);
        StringBuilder digest_String = new StringBuilder().append(account).append(appId).append(app_cert).append(timestamp);
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(digest_String.toString().getBytes());
        byte[] output = md5.digest();
        String token = hexlify(output);
        String token_String = new StringBuilder().append("1").append(":").append(appId).append(":").append(timestamp).append(":").append(token).toString();
        return token_String;
    }

    public static String getMediaToken(String uid,String channelName){

        String result = "";
        try {
            Date date = new Date();
            int time = (int)(date.getTime()/1000) + 7200 ;
            SimpleTokenBuilder token = new SimpleTokenBuilder(appId, app_cert, channelName, uid);
            token.initPrivileges(SimpleTokenBuilder.Role.Role_Attendee);
            token.setPrivilege(AccessToken.Privileges.kJoinChannel, time);
            token.setPrivilege(AccessToken.Privileges.kPublishAudioStream, time);
            token.setPrivilege(AccessToken.Privileges.kPublishVideoStream, time);
            token.setPrivilege(AccessToken.Privileges.kPublishDataStream, time);

            result = token.buildToken();
            System.out.println(result);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String hexlify(byte[] data) {

        char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5',
                '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        char[] toDigits = DIGITS_LOWER;
        int l = data.length;
        char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return String.valueOf(out);
    }
}
