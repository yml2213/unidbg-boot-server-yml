package com.anjia.unidbgserver.service;

import com.anjia.unidbgserver.config.UnidbgProperties;
import com.anjia.unidbgserver.utils.FileUtils;
import com.anjia.unidbgserver.utils.StrUtils;
import com.anjia.unidbgserver.utils.TempFileUtils;
import com.github.unidbg.*;
import com.github.unidbg.arm.backend.DynarmicFactory;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.memory.Memory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


import com.github.unidbg.linux.android.dvm.AbstractJni;
import com.github.unidbg.AndroidEmulator;

import java.io.IOException;

@Slf4j
public class PujiSigService extends AbstractJni {

    private final static String SO_ENCRYPT_LIB_PATH = "core";

    private final AndroidEmulator emulator;
    private final VM vm;
    private final Boolean DEBUG_FLAG;

    @SneakyThrows
    public PujiSigService(UnidbgProperties unidbgProperties) {
        DEBUG_FLAG = unidbgProperties.isVerbose();
        // 创建模拟器实例，要模拟32位或者64位，在这里区分
        EmulatorBuilder<AndroidEmulator> builder = AndroidEmulatorBuilder.for32Bit().setProcessName("com.kwai.thanos");
        // 动态引擎
        if (unidbgProperties.isDynarmic()) {
            builder.addBackendFactory(new DynarmicFactory(true));
        }
        emulator = builder.build();
        // 模拟器的内存操作接口
        final Memory memory = emulator.getMemory();
        // 设置系统类库解析
        memory.setLibraryResolver(new AndroidResolver(23));
        vm = emulator.createDalvikVM(FileUtils.getTempFile(unidbgProperties.getApkPath()));
        // 创建Android虚拟机  传入 apk文件 可以过部分签名校验
        // 设置是否打印Jni调用细节
        vm.setVerbose(unidbgProperties.isVerbose());

        vm.setJni(this);
        // 加载libttEncrypt.so到unicorn虚拟内存，加载成功以后会默认调用init_array等函数
        DalvikModule dm = vm.loadLibrary(SO_ENCRYPT_LIB_PATH, true);
        // 手动执行JNI_OnLoad函数
        dm.callJNI_OnLoad(emulator);
        // 加载好的libttEncrypt.so对应为一个模块  //获取本SO模块的句柄
        dm.callJNI_OnLoad(emulator);

    }

    public void destroy() throws IOException {
        emulator.close();
        if (DEBUG_FLAG) {
            log.info("destroy");
        }
    }


    public String getClock(String str) {
        log.info("sig start");
        byte[] byteArray = str.getBytes();
        // 解析并调用 JNI 方法
        DvmClass cBitmapkitUtils = vm.resolveClass("com/yxcorp/gifshow/util/CPU");
        DvmObject<?> context = vm.resolveClass("com/yxcorp/gifshow/App").newObject(null);
        StringObject ret = cBitmapkitUtils.callStaticJniMethodObject(emulator, "getClock(Landroid/content/Context;[BI)Ljava/lang/String;", new Object[]{context, byteArray, 30});
        String retValue = StrUtils.retSaveStr(ret);
        log.info("sig={}", retValue);
        return retValue;
    }


    @Override
    public DvmObject<?> callObjectMethodV(BaseVM vm, DvmObject<?> dvmObject, String signature, VaList vaList) {
        switch (signature) {
            case "com/yxcorp/gifshow/App->getPackageName()Ljava/lang/String;":
                return new StringObject(vm, "com.kwai.thanos");
            case "com/yxcorp/gifshow/App->getPackageManager()Landroid/content/pm/PackageManager;":
                return vm.resolveClass("android/content/pm/PackageManager").newObject(null);

        }
        return super.callObjectMethodV(vm, dvmObject, signature, vaList);
    }




}
