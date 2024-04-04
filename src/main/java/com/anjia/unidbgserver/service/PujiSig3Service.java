package com.anjia.unidbgserver.service;

import com.anjia.unidbgserver.config.UnidbgProperties;
import com.anjia.unidbgserver.utils.TempFileUtils;
import com.github.unidbg.*;
import com.github.unidbg.arm.HookStatus;
import com.github.unidbg.arm.backend.DynarmicFactory;
import com.github.unidbg.arm.context.Arm32RegisterContext;
import com.github.unidbg.arm.context.RegisterContext;
import com.github.unidbg.file.linux.AndroidFileIO;
import com.github.unidbg.hook.HookContext;
import com.github.unidbg.hook.ReplaceCallback;
import com.github.unidbg.hook.hookzz.*;
import com.github.unidbg.hook.xhook.IxHook;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.XHookImpl;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.linux.android.dvm.api.AssetManager;
import com.github.unidbg.linux.android.dvm.api.PackageInfo;
import com.github.unidbg.linux.android.dvm.api.Signature;
import com.github.unidbg.linux.android.dvm.array.ArrayObject;
import com.github.unidbg.linux.android.dvm.array.ByteArray;
import com.github.unidbg.linux.android.dvm.wrapper.DvmBoolean;
import com.github.unidbg.linux.android.dvm.wrapper.DvmInteger;
import com.github.unidbg.memory.Memory;
import com.github.unidbg.spi.SyscallHandler;
import com.github.unidbg.utils.Inspector;
import com.github.unidbg.virtualmodule.android.AndroidModule;
import com.github.unidbg.virtualmodule.android.JniGraphics;
import com.sun.jna.Pointer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


import com.github.unidbg.linux.android.dvm.AbstractJni;
import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Module;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.memory.Memory;
import net.dongliu.apk.parser.bean.CertificateMeta;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author cym
 */
@Slf4j
public class PujiSig3Service extends AbstractJni {

    private final static String TT_ENCRYPT_LIB_PATH = "data/apks/so/libkwsgmain.so";

    private final static String PUJI_APK_PATH = "apks/puji.apk";
    private final AndroidEmulator emulator;
    private final VM vm;
    private final Boolean DEBUG_FLAG;

    @SneakyThrows
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

        // 创建Android虚拟机
        // vm = emulator.createDalvikVM();
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        Resource pujiApkPath = resourcePatternResolver.getResource(PUJI_APK_PATH);
        Resource soPath = resourcePatternResolver.getResource(TT_ENCRYPT_LIB_PATH);
        // 创建Android虚拟机  传入 apk文件 可以过部分签名校验
        vm = emulator.createDalvikVM(pujiApkPath.getFile());
        // 设置是否打印Jni调用细节
        vm.setVerbose(DEBUG_FLAG);
        new JniGraphics(emulator, vm).register(memory);
        new AndroidModule(emulator, vm).register(memory);
        vm.setJni(this);


        List<String> denyList = Arrays.asList(TT_ENCRYPT_LIB_PATH);

        for (File file : Objects.requireNonNull(new File("src/main/resources/data/apks/so/").listFiles())) {
            if (denyList.contains(file.getName())) {
                continue;
            }
            try {
                //手动执行JNI_OnLoad函数
                DalvikModule dm = vm.loadLibrary(file.getName().replaceAll("lib|\\.so", ""), true);
                dm.callJNI_OnLoad(emulator);
            } catch (Exception ignored) {
                log.info("error：{}", file.getName());
            }
        }


    }


    public void initNative() {
        DvmClass dvmClass = vm.resolveClass("com/kuaishou/android/security/internal/dispatch/JNICLibrary");
        DvmObject<?> context = vm.resolveClass("com/yxcorp/gifshow/App").newObject(null);
        String methodName = "doCommandNative(I[Ljava/lang/Object;)Ljava/lang/Object;";
        List<Object> list = new ArrayList<>(2);
        DvmInteger zeroInt = DvmInteger.valueOf(vm, 0);
        dvmClass.callStaticJniMethodObject(emulator, methodName,
            10412,
            vm.addLocalObject(
                new ArrayObject(
                    zeroInt,
                    new StringObject(vm, "d7b7d042-d4f2-4012-be60-d97ff2429c17"),// SO文件有校验
                    zeroInt,
                    zeroInt,
                    context,
                    zeroInt,
                    zeroInt
                )));
    }


    @Override
    public DvmObject<?> getObjectField(BaseVM vm, DvmObject<?> dvmObject, String signature) {

        switch (signature) {
            case "android/content/pm/PackageInfo->signatures:[Landroid/content/pm/Signature;": {
                PackageInfo packageInfo = (PackageInfo) dvmObject;
                System.out.println("packageInfo===>  " + packageInfo);
                System.out.println("vm.getPackageName===>  " + vm.getPackageName());
                if (packageInfo.getPackageName().equals(vm.getPackageName())) {
                    CertificateMeta[] metas = vm.getSignatures();
                    if (metas != null) {
                        Signature[] signatures = new Signature[metas.length];
                        for (int i = 0; i < metas.length; i++) {
                            signatures[i] = new Signature(vm, metas[i]);
                        }
                        return new ArrayObject(signatures);
                    }
                }
            }

        }

        throw new UnsupportedOperationException(signature);
    }

    @Override
    public DvmObject<?> callObjectMethodV(BaseVM vm, DvmObject<?> dvmObject, String signature, VaList vaList) {
        switch (signature) {
            case "com/yxcorp/gifshow/App->getPackageCodePath()Ljava/lang/String;": {
                return new StringObject(vm, "/data/app/com.kwai.thanos-q14Fo0PSb77vTIOM1-iEqQ==/base.apk");
            }
            case "com/yxcorp/gifshow/App->getAssets()Landroid/content/res/AssetManager;": {
//                return new Long(vm, "3817726272");
                return new AssetManager(vm, signature);
            }
            case "com/yxcorp/gifshow/App->getPackageName()Ljava/lang/String;": {
                return new StringObject(vm, "com.kwai.thanos");
            }
            case "com/yxcorp/gifshow/App->getPackageManager()Landroid/content/pm/PackageManager;": {
                DvmClass clazz = vm.resolveClass("android/content/pm/PackageManager");
                return clazz.newObject(signature);
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

    public String get_NS_sig3(String str) {
        initNative();
        DvmClass dvmClass = vm.resolveClass("com/kuaishou/android/security/internal/dispatch/JNICLibrary");
        DvmObject<?> context = vm.resolveClass("com/yxcorp/gifshow/App").newObject(null);
        String methodSign = "doCommandNative(I[Ljava/lang/Object;)Ljava/lang/Object;";
        log.info("_NS_sig3 start");
        //通过方法名调用
        DvmObject<String> obj = dvmClass.callStaticJniMethodObject(emulator,
            methodSign,
            10418
            , vm.addLocalObject(new ArrayObject(new ArrayObject(
                new StringObject(vm, str)),
                new StringObject(vm, "d7b7d042-d4f2-4012-be60-d97ff2429c17"),
                DvmInteger.valueOf(vm, -1),
                DvmBoolean.valueOf(vm, false),
                context,
                null,
                DvmBoolean.valueOf(vm, false),
                new StringObject(vm, "")


            )));
        log.info("_NS_sig3={}", obj.toString());
        return obj.getValue();
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
