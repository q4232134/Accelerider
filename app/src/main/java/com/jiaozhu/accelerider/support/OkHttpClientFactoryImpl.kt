package com.jiaozhu.accelerider.support

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import zlc.season.rxdownload3.http.OkHttpClientFactory
import java.io.IOException
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class ClientFactoryImpl : OkHttpClientFactory {
    override fun build(): OkHttpClient {
        val builder = OkHttpClient().newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .sslSocketFactory(getSLLContext()?.socketFactory)
                .addInterceptor(UserAgentInterceptor("netdisk:8.2.0;android-android:4.4.4"))
        return builder.build()
    }

    inner class UserAgentInterceptor(private val userAgentHeaderValue: String) : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val requestWithUserAgent = originalRequest.newBuilder()
                    .removeHeader("User-Agent")   //移除先前默认的User-Agent
                    .addHeader("User-Agent", userAgentHeaderValue)  //设置新的User-Agent
                    .build()
            return chain.proceed(requestWithUserAgent)
        }
    }

    private fun getSLLContext(): SSLContext? {
        var sslContext: SSLContext? = null
        try {
            sslContext = SSLContext.getInstance("TLS")
            sslContext!!.init(null, arrayOf<TrustManager>(object : X509TrustManager {
                @Throws(CertificateException::class)
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {

                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {

                }

                override fun getAcceptedIssuers(): Array<X509Certificate?> {
                    return arrayOfNulls(0)
                }
            }), SecureRandom())
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: KeyManagementException) {
            e.printStackTrace()
        }

        return sslContext
    }
}