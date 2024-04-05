package com.anjia.unidbgserver.service;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

/**
 * @author cym
 */
@Slf4j
public class PujiClientSignService {
    public String get_ClientSign(String str) {
        byte[] bArr;
        String str2 = "";

        String[] parts = str.split("#");
        String ret = parts[1];
        String hexString = parts[2];

        String a4 = parts[0] + ret;

//        int min = (int) (System.currentTimeMillis() / TimeUnit.MINUTES.toMillis(1L));
//        int random = new Random(System.currentTimeMillis()).nextInt();
////        System.out.println(random);
//        String ret = random + "" + min;
//        System.out.println(ret);
//        System.out.println(ret.length());
//        ret = "3484513676012254641";

//        String hexString = "db2ffe042647db5110507bc612ea1fa0";

        byte[] decode = hexStringToByteArray(hexString);
        byte[] bytes = a4.getBytes(StandardCharsets.UTF_8);
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(decode, "HmacSHA256");
            Mac mac = Mac.getInstance(secretKeySpec.getAlgorithm());
            mac.init(secretKeySpec);
            bArr = mac.doFinal(bytes);
        } catch (Exception unused) {
            bArr = new byte[0];
        }
        if (bArr != null) {
            long j4 = Long.parseLong(ret);
            byte[] bArr2 = new byte[8];
            for (int i4 = 7; i4 >= 0; i4--) {
                bArr2[i4] = (byte) (255 & j4);
                j4 >>= 8;
            }
            byte[] bArr3 = new byte[bArr.length + 8];
            System.arraycopy(bArr2, 0, bArr3, 0, 8);
            System.arraycopy(bArr, 0, bArr3, 8, bArr.length);
//            str2 = Base64.encodeToString(bArr3, 11);
            System.out.println(Arrays.toString(bArr3));
            str2 = Base64.getUrlEncoder().withoutPadding().encodeToString(bArr3);
        }
//        System.out.println(str2);
        return str2;
    }


    public static byte[] hexStringToByteArray(String hexString) {
        byte[] byteArray = new byte[hexString.length() / 2];
        for (int i = 0; i < byteArray.length; i++) {
            int index = i * 2;
            int j = Integer.parseInt(hexString.substring(index, index + 2), 16);
            byteArray[i] = (byte) j;
        }
        return byteArray;
    }


}
