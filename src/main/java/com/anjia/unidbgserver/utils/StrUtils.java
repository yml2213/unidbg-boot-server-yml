package com.anjia.unidbgserver.utils;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.anjia.unidbgserver.exception.BusinessException;
import com.anjia.unidbgserver.response.enums.ErrorCodeEnum;
import com.github.unidbg.linux.android.dvm.DvmObject;
import com.github.unidbg.linux.android.dvm.StringObject;
import org.apache.commons.lang3.ObjectUtils;

/**
 * @author: cym
 * @create: 2024-04-05 16:47
 * @description:
 * @version: 0.0.1
 **/

public class StrUtils {
    private StrUtils() {
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

    public static String retSaveStr(StringObject strObj){
        if (ObjectUtils.isEmpty(strObj)){
            throw new BusinessException(ErrorCodeEnum.GET_SIGN_ERROR);
        }
        return CharSequenceUtil.isNotBlank(strObj.getValue()) ? strObj.getValue() :"";
    }

}
