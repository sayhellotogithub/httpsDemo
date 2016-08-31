#一、HTTPS介绍

> 安全

* 1.数据持久化
* 2.传输
* 3.服务器

> 加密算法

* 1.对称加密
* 2.非对称加密

> 从对称加密到非对称加密，分析如何保证安全

----------



#二、HTTPS 单向认证
## 1. 给服务器生成密钥

```
keytool -genkeypair -alias iblogstreet -keyalg RSA -validity 3650 -keypass 123456 -storepass 123456 -keystore iblogstreet.keystore
```

## 2. 给Tomcat服务器配置Https
* tomcat/config/server.xml修改connector配置

```
<Connector port="8443" protocol="org.apache.coyote.http11.Http11Protocol"
               maxThreads="150" SSLEnabled="true" scheme="https" secure="true"
               clientAuth="false" sslProtocol="TLS"
			   keystoreFile="conf/iblogstreet.keystore"
			   keystorePass="123456"/>
```

* 别忘了关闭一个监听器：注释掉下面

```
<Listener className="org.apache.catalina.core.AprLifecycleListener" SSLEngine="on" />
```

* 访问：**https**://192.168.33.26:**8443**

[附上官方配置文档](https://tomcat.apache.org/tomcat-7.0-doc/ssl-howto.html)

[或者谷歌搜索结果](https://www.google.com.sg/?gws_rd=cr&ei=vH2VV7GkI4byvAS07rOoBg#q=comcat+https+config)




## 3. 给客户端导出公钥(证书)

* 1.浏览器导出证书方式
* 2.命令方式
```
keytool -exportcert -alias iblogstreet -file iblogstreet.crt -keystore iblogstreet.keystore -storepass 123456
```

## 4. Android端编码
	InputStream fis = getAssets().open("iblogstreet.cer");
	SSLContext sslContext = SSLContext.getInstance("TLS");
	//使用默认证书
	KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
	//去掉系统默认证书
	keyStore.load(null);
	Certificate cert = CertificateFactory.getInstance("X.509").generateCertificate(fis);
	//设置自己的证书
	keyStore.setCertificateEntry("iblogstreet", cert);
	String algorithm = TrustManagerFactory.getDefaultAlgorithm();
	TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
	tmf.init(keyStore);
	sslContext.init(null, tmf.getTrustManagers(), null);
	SSLSocketFactory sf = sslContext.getSocketFactory();
	//设置信任管理器
	HttpsURLConnection.setDefaultSSLSocketFactory(sf);
	//https
	URL url = new URL("https://192.168.1.188:8443/HttpsDemo/test");
	HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
	//设置ip授权认证：如果已经安装该证书，可以不设置，否则需要设置
	urlConnection.setHostnameVerifier(new HostnameVerifier() {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	});
	InputStream is = urlConnection.getInputStream();
	String content = Util.inputStream2String(is);
	is.close();
	System.out.println(content);
