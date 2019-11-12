package com.github.lany192.pay.sample;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.github.lany192.pay.ali.PayResult;
import com.github.lany192.pay.exception.PayFailedException;
import com.github.lany192.pay.wx.WxPayResult;
import com.weilu.pay.BuildConfig;
import com.weilu.pay.R;
import com.github.lany192.pay.annotation.WXPay;
import com.github.lany192.pay.RxAliPay;
import com.github.lany192.pay.RxWxPay;

import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

@WXPay(BuildConfig.APPLICATION_ID)
public class MainActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RxWxPay.init(this.getApplication());
    }
    
    public void aLiPay(View view){
        new RxAliPay()
                .with(MainActivity.this, "")
                .requestPay()
                .subscribe(new Observer<PayResult>() {
                    @Override
                    public void onSubscribe(Disposable d) {}

                    @Override
                    public void onNext(PayResult payResult) {
                        Toast.makeText(MainActivity.this, "支付成功！" , Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("ErrCode:", ((PayFailedException)e).getErrCode());
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {}
                });        
    }

    public void wechatPay(View view){

        RxWxPay.WXPayBean payBean = new RxWxPay.WXPayBean("",
                "", "",
                "", "",
                "");

        RxWxPay.getInstance()
                .withWxPayBean(payBean)
                .requestPay()
                .subscribe(new Observer<WxPayResult>() {
                    @Override
                    public void onSubscribe(Disposable d) {}

                    @Override
                    public void onNext(WxPayResult wxPayResult) {
                        Toast.makeText(MainActivity.this, "支付成功！" , Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("ErrCode:", ((PayFailedException)e).getErrCode());
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {}
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxWxPay.getInstance().onDestroy();
    }
}
