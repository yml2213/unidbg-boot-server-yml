package com.anjia.unidbgserver.service.unidbg;

import com.anjia.unidbgserver.config.UnidbgProperties;
import com.anjia.unidbgserver.utils.FileUtils;
import com.anjia.unidbgserver.utils.StrUtils;
import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.EmulatorBuilder;
import com.github.unidbg.arm.backend.DynarmicFactory;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.linux.android.dvm.api.AssetManager;
import com.github.unidbg.linux.android.dvm.array.ArrayObject;
import com.github.unidbg.linux.android.dvm.wrapper.DvmBoolean;
import com.github.unidbg.linux.android.dvm.wrapper.DvmInteger;
import com.github.unidbg.memory.Memory;
import com.github.unidbg.virtualmodule.android.AndroidModule;
import com.github.unidbg.virtualmodule.android.JniGraphics;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author cym
 */
@Slf4j
public class PujiSig3Service extends AbstractJni {

    private static final String SO_ENCRYPT_LIB_PATH = "libkwsgmain.so";
    private final AndroidEmulator emulator;
    private final VM vm;

    private final Boolean DEBUG_FLAG;

    public PujiSig3Service(UnidbgProperties unidbgProperties) {
        //jni细节
        DEBUG_FLAG = unidbgProperties.isVerbose();
        // 创建模拟器实例，要模拟32位或者64位，在这里区分
        EmulatorBuilder<AndroidEmulator> builder = AndroidEmulatorBuilder.for32Bit().setProcessName("com.kwai.thanos");
        emulator = builder.build();
        // 动态引擎
        if (unidbgProperties.isDynarmic()) {
            builder.addBackendFactory(new DynarmicFactory(true));
        }
        emulator.getSyscallHandler().setEnableThreadDispatcher(true);
        // 模拟器的内存操作接口
        final Memory memory = emulator.getMemory();
        // 设置系统类库解析
        memory.setLibraryResolver(new AndroidResolver(23));
        // 创建Android虚拟机  传入 apk文件 可以过部分签名校验
        vm = emulator.createDalvikVM(FileUtils.getTempFile(unidbgProperties.getApkPath()));
        // 设置是否打印Jni调用细节
        vm.setVerbose(DEBUG_FLAG);
        new JniGraphics(emulator, vm).register(memory);
        new AndroidModule(emulator, vm).register(memory);
        vm.setJni(this);
        String soPath = unidbgProperties.getSoPrefix() + SO_ENCRYPT_LIB_PATH;
        DalvikModule dm = this.vm.loadLibrary(FileUtils.getTempFile(soPath), true);
        dm.callJNI_OnLoad(this.emulator);
        initNative();
        //String str = "1:0:0:0:0:0:0";
        //String[] parts = str.split(":");
        //for (int i = 0; i < 7; i++) {
        //
        //    initNative2(str);
        //    if (i == 6){
        //        break;// 将当前位置的前一位置为 "0"，当前位置置为 "1"
        //    }
        //    parts[i] = "0";
        //    parts[i + 1] = "1";
        //
        //    // 将数组重新拼接成字符串
        //    str = String.join(":", parts);

        }


    public void initNative() {
        DvmClass dvmClass = vm.resolveClass("com/kuaishou/android/security/internal/dispatch/JNICLibrary");
        DvmObject<?> context = vm.resolveClass("com/yxcorp/gifshow/App").newObject(null);
        String methodName = "doCommandNative(I[Ljava/lang/Object;)Ljava/lang/Object;";
        DvmInteger zeroInt = DvmInteger.valueOf(vm, 0);
        dvmClass.callStaticJniMethodObject(emulator, methodName,
            10412,
            vm.addLocalObject(
                new ArrayObject(
                    zeroInt,
                    // SO文件有校验
                    new StringObject(vm, "d7b7d042-d4f2-4012-be60-d97ff2429c17"),
                    zeroInt,
                    zeroInt,
                    context,
                    zeroInt,
                    zeroInt
                )));
    }

    //public void initNative2(String str) {
    //    //log.info(str);
    //    DvmClass dvmClass = vm.resolveClass("com/kuaishou/android/security/internal/dispatch/JNICLibrary");
    //    DvmObject<?> context = vm.resolveClass("com/yxcorp/gifshow/App").newObject(null);
    //    String methodName = "doCommandNative(I[Ljava/lang/Object;)Ljava/lang/Object;";
    //    StringObject ret = dvmClass.callStaticJniMethodObject(emulator, methodName,
    //        10411,
    //        vm.addLocalObject(
    //            new ArrayObject(
    //                new ArrayObject(new StringObject(vm, str)),
    //                // SO文件有校验
    //                new StringObject(vm, "d7b7d042-d4f2-4012-be60-d97ff2429c17"),
    //                null,
    //                null,
    //                context,
    //                null,
    //                null
    //            )));
    //    log.info(ret.getValue());
    //}

    //public void initNative2(String str) {
    //    //log.info(str);
    //    DvmClass dvmClass = vm.resolveClass("com/kuaishou/android/security/internal/dispatch/JNICLibrary");
    //    DvmObject<?> context = vm.resolveClass("com/yxcorp/gifshow/App").newObject(null);
    //    String methodName = "doCommandNative(I[Ljava/lang/Object;)Ljava/lang/Object;";
    //    StringObject ret = dvmClass.callStaticJniMethodObject(emulator, methodName,
    //        10411,
    //        vm.addLocalObject(
    //            new ArrayObject(
    //                new ArrayObject(new StringObject(vm, str)),
    //                // SO文件有校验
    //                new StringObject(vm, "d7b7d042-d4f2-4012-be60-d97ff2429c17"),
    //                null,
    //                null,
    //                context,
    //                null,
    //                null
    //            )));
    //    log.info(ret.getValue());
    //}




    public String getNsSig3(String... strArr) {


         String uuid = "";
         boolean flag = false;
         //64位
         if (strArr.length > 1) {
             uuid = strArr[1];
             flag = true;
         }
         DvmClass dvmClass = vm.resolveClass("com/kuaishou/android/security/internal/dispatch/JNICLibrary");
         DvmObject<?> context = vm.resolveClass("com/yxcorp/gifshow/App").newObject(null);
         String methodSign = "doCommandNative(I[Ljava/lang/Object;)Ljava/lang/Object;";
         log.info("_NS_sig3 start");
         //通过方法名调用
        StringObject ret = dvmClass.callStaticJniMethodObject(emulator,
             methodSign,
             10413
             , vm.addLocalObject(new ArrayObject(new ArrayObject(
                 new StringObject(vm, "30")),
                 new StringObject(vm, "d7b7d042-d4f2-4012-be60-d97ff2429c17"),
                 DvmInteger.valueOf(vm, 0),
               null,
                 context,
                 null,
                    DvmBoolean.valueOf(vm, false),
                 new StringObject(vm,"")
             )));

         String retValue = StrUtils.retSaveStr(ret);
         //String encode = Base64.encode(retValue);
         log.info("_NS_sig3={}", retValue);
         return retValue;


       // String uuid = "";
       // boolean flag = false;
       // //64位
       // if (strArr.length > 1) {
       //     uuid = strArr[1];
       //     flag = true;
       // }
       // DvmClass dvmClass = vm.resolveClass("com/kuaishou/android/security/internal/dispatch/JNICLibrary");
       // DvmObject<?> context = vm.resolveClass("com/yxcorp/gifshow/App").newObject(null);
       // String methodSign = "doCommandNative(I[Ljava/lang/Object;)Ljava/lang/Object;";
       // log.info("_NS_sig3 start");
       // //通过方法名调用
       //StringObject ret = dvmClass.callStaticJniMethodObject(emulator,
       //     methodSign,
       //     10418
       //     , vm.addLocalObject(new ArrayObject(new ArrayObject(
       //         new StringObject(vm, strArr[0])),
       //         new StringObject(vm, "d7b7d042-d4f2-4012-be60-d97ff2429c17"),
       //         DvmInteger.valueOf(vm, -1),
       //         DvmBoolean.valueOf(vm, false),
       //         context,
       //         null,
       //            DvmBoolean.valueOf(vm, ),
       //         new StringObject(vm, "010a11c6-f2cb-4016-887d-0d958aef1534")
       //     )));
       //
       // String retValue = StrUtils.retSaveStr(ret);
       // //String encode = Base64.encode(retValue);
       // log.info("_NS_sig3={}", retValue);
       // return retValue;
    }

    public void destroy() throws IOException {
        emulator.close();
        if (DEBUG_FLAG) {
            log.info("destroy");
        }
    }

    @Override
    public DvmObject<?> callObjectMethodV(BaseVM vm, DvmObject<?> dvmObject, String signature, VaList vaList) {
        switch (signature) {
            case "com/yxcorp/gifshow/App->getPackageName()Ljava/lang/String;":
                return new StringObject(vm, "com.kwai.thanos");
            case "com/yxcorp/gifshow/App->getPackageManager()Landroid/content/pm/PackageManager;":
                return vm.resolveClass("android/content/pm/PackageManager").newObject(null);
            case "com/yxcorp/gifshow/App->getPackageCodePath()Ljava/lang/String;": {
                return new StringObject(vm, "/data/app/com.kwai.thanos-q14Fo0PSb77vTIOM1-iEqQ==/base.apk");
            }
            case "com/yxcorp/gifshow/App->getAssets()Landroid/content/res/AssetManager;": {
//                return new Long(vm, "3817726272");
                return new AssetManager(vm, signature);
            }

        }
        return super.callObjectMethodV(vm, dvmObject, signature, vaList);
    }


    @Override
    public boolean callBooleanMethodV(BaseVM vm, DvmObject<?> dvmObject, String signature, VaList vaList) {
        switch (signature) {
            case "java/lang/Boolean->booleanValue()Z":
                DvmBoolean dvmBoolean = (DvmBoolean) dvmObject;
                return dvmBoolean.getValue();
        }
        return super.callBooleanMethodV(vm, dvmObject, signature, vaList);
    }

    @Override
    public DvmObject<?> callStaticObjectMethodV(BaseVM vm, DvmClass dvmClass, String signature, VaList vaList) {
        switch (signature) {
            case "com/kuaishou/android/security/internal/common/ExceptionProxy->getProcessName(Landroid/content/Context;)Ljava/lang/String;":
                return new StringObject(vm, "com.kwai.thanos");
            case "com/meituan/android/common/mtguard/NBridge->getSecName()Ljava/lang/String;":
                return new StringObject(vm, "ppd_com.sankuai.meituan.xbt");
            case "com/meituan/android/common/mtguard/NBridge->getAppContext()Landroid/content/Context;":
                return vm.resolveClass("android/content/Context").newObject(null);
            case "com/meituan/android/common/mtguard/NBridge->getMtgVN()Ljava/lang/String;":
                return new StringObject(vm, "4.4.7.3");
            case "com/meituan/android/common/mtguard/NBridge->getDfpId()Ljava/lang/String;":
                return new StringObject(vm, "");
        }
        return super.callStaticObjectMethodV(vm, dvmClass, signature, vaList);
    }


}
