package com.iblogstreet.httpsdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * 本例子访问12306 带https的访问
 */
public class MainActivity
        extends AppCompatActivity
{

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void sendHttps(View view) {
        new Thread() {
            @Override
            public void run() {
                String path = "https://kyfw.12306.cn/otn/leftTicket/init";

                try {
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                   //读取证书文件
                    InputStream caInput = getAssets().open("12306.cer");
                    Certificate ca;
                    try {
                        ca = cf.generateCertificate(caInput);
                        System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
                    } finally {
                        caInput.close();
                    }
                    //使用默认证书
                    KeyStore keyStore     = KeyStore.getInstance(KeyStore.getDefaultType());
                    //去掉系统默认的证书
                    keyStore.load(null, null);
                    //加载自已的证书
                    keyStore.setCertificateEntry("ca", ca);

                    String              tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                    TrustManagerFactory tmf          = TrustManagerFactory.getInstance(tmfAlgorithm);
                    tmf.init(keyStore);
                    SSLContext context = SSLContext.getInstance("TLS");
                    context.init(null, tmf.getTrustManagers(), null);

                    URL url = new URL(path);
                    HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                    urlConnection.setSSLSocketFactory(context.getSocketFactory());
                    int responseCode = urlConnection.getResponseCode();
                    if (responseCode == 200) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG, "success");
                                Toast.makeText(MainActivity.this, "success", Toast.LENGTH_SHORT)
                                     .show();
                            }
                        });

                    } else {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG, "failed");
                                Toast.makeText(MainActivity.this, "failed", Toast.LENGTH_SHORT)
                                     .show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
