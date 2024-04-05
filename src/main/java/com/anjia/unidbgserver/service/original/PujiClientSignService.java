package com.anjia.unidbgserver.service.original;

import cn.hutool.core.codec.Base64;
import com.anjia.unidbgserver.utils.StrUtils;
import com.github.unidbg.linux.android.dvm.StringObject;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;


/**
 * @author cym
 */
@Slf4j
public class PujiClientSignService {


    public String getClientSign(String str) {
        byte[] bArr;
        String str2 = "";
        String[] parts = str.split("#");
        String ret = parts[1];
        String hexString = parts[2];

        String a4 = parts[0] + ret;


        byte[] decode = StrUtils.hexStringToByteArray(hexString);
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
            log.info(Arrays.toString(bytes));
            str2 = Base64.encode(bArr3);
        }
        return str2;
    }


}
