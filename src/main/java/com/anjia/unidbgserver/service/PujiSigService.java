package com.anjia.unidbgserver.service;

import com.anjia.unidbgserver.config.UnidbgProperties;
import com.anjia.unidbgserver.utils.TempFileUtils;
import com.github.unidbg.*;
import com.github.unidbg.arm.HookStatus;
import com.github.unidbg.arm.backend.DynarmicFactory;
import com.github.unidbg.arm.context.Arm32RegisterContext;
import com.github.unidbg.arm.context.RegisterContext;
import com.github.unidbg.hook.HookContext;
import com.github.unidbg.hook.ReplaceCallback;
import com.github.unidbg.hook.hookzz.*;
import com.github.unidbg.hook.xhook.IxHook;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.XHookImpl;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.linux.android.dvm.array.ByteArray;
import com.github.unidbg.memory.Memory;
import com.github.unidbg.utils.Inspector;
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

import java.io.File;
import java.io.IOException;

@Slf4j
public class PujiSigService extends AbstractJni  {

    private final static String TT_ENCRYPT_LIB_PATH = "data/apks/so/libcore.so";
    private final AndroidEmulator emulator;
    private final VM vm;
    private final Module module;
    private final DvmClass TTEncryptUtils;
    private final Boolean DEBUG_FLAG;

    @SneakyThrows
   public  PujiSigService(UnidbgProperties unidbgProperties) {
        DEBUG_FLAG = unidbgProperties.isVerbose();
        // 创建模拟器实例，要模拟32位或者64位，在这里区分
//        emulator = AndroidEmulatorBuilder.for32Bit().setProcessName("com.kwai.thanos").build(); // 创建模拟器实例
        EmulatorBuilder<AndroidEmulator> builder = AndroidEmulatorBuilder.for32Bit().setProcessName("com.kwai.thanos");
        // 动态引擎
        if (unidbgProperties.isDynarmic()) {
            builder.addBackendFactory(new DynarmicFactory(true));
        }

//        vm = emulator.createDalvikVM(new File("unidbg-android/src/test/java/com/puji/puji.apk")); // 创建Android虚拟机
//        DalvikModule dm = vm.loadLibrary(new File("unidbg-android/src/test/java/com/puji/libcore.so"), true); // 加载so到虚拟内存
//        module = dm.getModule(); //获取本SO模块的句柄
//
//        vm.setJni(this);
////        vm.setVerbose(true);   // 打印日志
//        dm.callJNI_OnLoad(emulator);


        emulator = builder.build();
        // 模拟器的内存操作接口
        final Memory memory = emulator.getMemory();
        // 设置系统类库解析
        memory.setLibraryResolver(new AndroidResolver(23));

        // 创建Android虚拟机  传入 apk文件 可以过部分签名校验
        vm = emulator.createDalvikVM(new File("src/main/resources/apks/puji.apk"));
        // 设置是否打印Jni调用细节
        vm.setVerbose(unidbgProperties.isVerbose());

        vm.setJni(this);
        // 加载libttEncrypt.so到unicorn虚拟内存，加载成功以后会默认调用init_array等函数
        DalvikModule dm = vm.loadLibrary(TempFileUtils.getTempFile(TT_ENCRYPT_LIB_PATH), true);
        // 手动执行JNI_OnLoad函数
        dm.callJNI_OnLoad(emulator);
        // 加载好的libttEncrypt.so对应为一个模块  //获取本SO模块的句柄
        module = dm.getModule();

        dm.callJNI_OnLoad(emulator);

        TTEncryptUtils = vm.resolveClass("com/yxcorp/gifshow/util/CPU");
    }

    public void destroy() throws IOException {
        emulator.close();
        if (DEBUG_FLAG) {
            log.info("destroy");
        }
    }


    public String getClock(String str) {
//        String str = "abi=arm32androidApiLevel=30android_os=0app=0apptype=29appver=5.5.0.291boardPlatform=konabodyMd5=338940e402eb9f26a10c0af10cecf243bottom_navigation=truebrowseType=3c=ANDROID_BAIDU_JSWH_SSYQ_CPC_PUJI_LAXIN,1cdid_tag=0client_key=8d219c8dcold_launc" +
//            "h_time_ms=1711821480690country_code=cndarkMode=falseddpi=440device_abi=arm64did=ANDROID_93cb288f321199cfdid_gt=1711599804825did_tag=1earphoneMode=1egid=DFPB586E929AB7CFED30773FDF481851D5EC990D71C889D81EF7235E5601F403encoding=zstdftt=grant_browse_ty" +
//            "pe=AUTHORIZEDhotfix_ver=is_background=1isp=CUCCiuid=kcv=1464keyconfig_state=1kpf=ANDROID_PHONEkpn=THANOSkuaishou.api_st=Cg9rdWFpc2hvdS5hcGkuc3QSoAENCiKm9VfCoXRsMvrSDYf4GEkdqNghtlx9_huJcamUA-QSi3JGvu77ZZWg8OB7GhPEK9D9Zw4OfuY8gBq-nZN6ORVQH7KTRu2dAtAy" +
//            "iszudyMwMYaz_1ya_8Ofmd0FI83N7Z4QXwE0JqIAnLT4UdB_U2wUkgvVdz0e7kw38ArnijVyt-hxhMeepQxfxhbQfMOoFRUMDIbB_7X1fO_wVb7kGhJThZNu-rRPH4Mw3KnOATqUKJgiIEhwM07eJFWQdVHPFnTy5b39yVDaMAo-34Q8qo51SAiJKAUwAQlanguage=zh-cnmax_memory=256mod=Xiaomi(M2102J2SC)nbh=44net" +
//            "=WIFInewOc=ANDROID_BAIDU_JSWH_SSYQ_CPC_PUJI_LAXIN,1oDid=ANDROID_93cb288f321199cfoc=ANDROID_BAIDU_JSWH_SSYQ_CPC_PUJI_LAXIN,1os=androidpriorityType=1rdid=ANDROID_5255325a00e2c79fsbh=90sh=2340slh=0socName=Qualcomm Snapdragon 8250sw=1080sys=ANDROID_11t" +
//            "hermal=10000totalMemory=11598ud=55077737userRecoBit=0ver=5.5";
        byte[] byteArray = str.getBytes();

        // 解析并调用 JNI 方法
        DvmClass cBitmapkitUtils = vm.resolveClass("com/yxcorp/gifshow/util/CPU");
//        DvmObject context = vm.resolveClass("com/yxcorp/gifshow/App").newObject(null);
        DvmObject<?> context = vm.resolveClass("com/yxcorp/gifshow/App").newObject(null);
        StringObject ret = cBitmapkitUtils.callStaticJniMethodObject(emulator, "getClock(Landroid/content/Context;[BI)Ljava/lang/String;", new Object[]{context, byteArray, 30});
        System.out.println("***************");
        System.out.println(ret);
        System.out.println("***************");


        return ret.toString();

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
    };
//    public static void main(String[] args) throws Exception {
//        PujiSigService test = new PujiSigService();
//        System.out.println(test.getClock());
//    }


}
