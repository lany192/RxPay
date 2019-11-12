package com.weilu.pay.compiler;


import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.weilu.pay.annotation.WXPay;
import com.weilu.pay.compiler.utils.BaseProcessor;
import com.weilu.pay.compiler.utils.ClassEntity;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * @Description:微信支付生成处理
 * @Author: weilu
 * @Time: 2017/12/12 15:11.
 */

@AutoService(Processor.class)
public class WXProcessor extends BaseProcessor {

    /**
     * 指定注解处理器是注册给那一个注解的
     *
     * @return
     */
    @Override
    protected Class<? extends Annotation>[] getSupportedAnnotations() {
        return new Class[]{WXPay.class};
    }

    /**
     * 在这里扫描和处理你的注解并生成Java代码，信息都在参数RoundEnvironment里
     *
     * @param set
     * @param roundEnvironment
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Map<String, ClassEntity> map = entityHandler.handlerElement(roundEnvironment, this);
        for (Map.Entry<String, ClassEntity> item : map.entrySet()) {
            //entityHandler.generateCode(brewWxEntityActivity(item));
            entityHandler.generateCode(brewWxRegister(item));
            break;
        }
        return true;
    }

    private JavaFile brewWxEntityActivity(Map.Entry<String, ClassEntity> item) {
        ClassEntity classEntity = item.getValue();

        String packageName = classEntity.getElement().getAnnotation(WXPay.class).value() + ".wxapi";

        ClassName activityClazz = ClassName.get("android.app", "Activity");
        ClassName interfaceClazz = ClassName.get("com.tencent.mm.opensdk.openapi", "IWXAPIEventHandler");
        ClassName baseReqClazz = ClassName.get("com.tencent.mm.opensdk.modelbase", "BaseReq");
        ClassName baseRespClazz = ClassName.get("com.tencent.mm.opensdk.modelbase", "BaseResp");
        ClassName onNewIntentClazz = ClassName.get("android.content", "Intent");
        ClassName bundleClazz = ClassName.get("android.os", "Bundle");
        ClassName wxApiFactoryClazz = ClassName.get("com.tencent.mm.opensdk.openapi", "WXAPIFactory");
        ClassName wxApiClazz = ClassName.get("com.tencent.mm.opensdk.openapi", "IWXAPI");
        ClassName rxWxPay = ClassName.get("com.weilu.pay.api", "RxWxPay");
        ClassName busClazz = ClassName.get("com.weilu.pay.api.utils", "BusUtil");
        ClassName constantsClazz = ClassName.get("com.tencent.mm.opensdk.constants", "ConstantsAPI");

        MethodSpec onReq = MethodSpec
                .methodBuilder("onReq")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(baseReqClazz, "baseReq")
                .build();

        MethodSpec onCreate = MethodSpec
                .methodBuilder("onCreate")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(bundleClazz, "savedInstanceState")
                .addStatement("super.onCreate(savedInstanceState)")
                .addStatement("api = $T.createWXAPI(this, $T.getInstance().getAppId())", wxApiFactoryClazz, rxWxPay)
                .addStatement("api.handleIntent(getIntent(), this)")
                .build();

        MethodSpec onNewIntent = MethodSpec
                .methodBuilder("onNewIntent")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .returns(void.class)
                .addParameter(onNewIntentClazz, "intent")
                .addStatement("super.onNewIntent(intent)")
                .addStatement("setIntent(intent)")
                .addStatement("api.handleIntent(intent, this)")
                .build();

        MethodSpec onResp = MethodSpec
                .methodBuilder("onResp")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(baseRespClazz, "baseResp")
                .beginControlFlow("if (baseResp.getType() == $T.COMMAND_PAY_BY_WX)", constantsClazz)
                .addStatement("$T.getDefault().post(baseResp)", busClazz)
                .addStatement("finish()")
                .endControlFlow()
                .build();

        TypeSpec typeSpec = TypeSpec
                .classBuilder("WXPayEntryActivity")
                .addModifiers(Modifier.PUBLIC)
                .superclass(activityClazz)
                .addSuperinterface(interfaceClazz)
                .addField(wxApiClazz, "api", Modifier.PRIVATE)
                .addMethod(onCreate)
                .addMethod(onNewIntent)
                .addMethod(onReq)
                .addMethod(onResp)
                .build();

        return JavaFile.builder(packageName, typeSpec).build();
    }

    private JavaFile brewWxRegister(Map.Entry<String, ClassEntity> item) {
        ClassEntity classEntity = item.getValue();

        String packageName = classEntity.getElement().getAnnotation(WXPay.class).value();

        ClassName receiveClazz = ClassName.get("android.content", "BroadcastReceiver");
        ClassName contextClazz = ClassName.get("android.content", "Context");
        ClassName wxApiFactoryClazz = ClassName.get("com.tencent.mm.opensdk.openapi", "WXAPIFactory");
        ClassName wxApiClazz = ClassName.get("com.tencent.mm.opensdk.openapi", "IWXAPI");
        ClassName rxWxPay = ClassName.get("com.weilu.pay.api", "RxWxPay");
        ClassName intentClazz = ClassName.get("android.content", "Intent");


        MethodSpec onReceive = MethodSpec
                .methodBuilder("onReceive")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(contextClazz, "context")
                .addParameter(intentClazz, "intent")
                .addStatement("$T msgApi = $T.createWXAPI(context, null)", wxApiClazz, wxApiFactoryClazz)
                .addStatement("msgApi.registerApp($T.getInstance().getAppId())", rxWxPay)
                .build();


        TypeSpec typeSpec = TypeSpec
                .classBuilder("AppRegister")
                .addModifiers(Modifier.PUBLIC)
                .superclass(receiveClazz)
                .addMethod(onReceive)
                .build();

        return JavaFile.builder(packageName, typeSpec).build();
    }
}
