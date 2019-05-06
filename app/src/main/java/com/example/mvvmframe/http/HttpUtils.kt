package com.example.mvvmframe.http

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.text.TextUtils
import com.example.mvvmframe.BuildConfig
import com.google.gson.FieldNamingPolicy
import com.google.gson.FieldNamingStrategy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.io.File
import java.io.IOException
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.HashMap
import java.util.concurrent.TimeUnit
import javax.net.ssl.*
import org.apache.commons.lang.StringUtils
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Field

class HttpUtils private constructor() {
    companion object {
        val instance: HttpUtils by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            HttpUtils()
        }
    }

    private var gson: Gson? = null
    private var context: Context? = null
    internal val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        @Throws(CertificateException::class)
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        @Throws(CertificateException::class)
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf()
        }
    })

    fun init(context: Context) {
        this.context = context
    }

    fun getBuilder(apiUrl: String): Retrofit.Builder {
        val b = Retrofit.Builder()
        b.client(getUnsafeOkHttpClient())
        b.baseUrl(apiUrl)
        b.addConverterFactory(NullOnEmptyConverterFactory())
        b.addConverterFactory(GsonConverterFactory.create(getGson()))
        b.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        return b
    }

    private fun getUnsafeOkHttpClient(): OkHttpClient {
        try {
            // Install the all-trusting trust manager TLS
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, SecureRandom())
            //cache url
            val httpCacheDirectory = File(context?.getCacheDir(), "responses")
            // 50 MiB
            val cacheSize = 50 * 1024 * 1024
            val cache = Cache(httpCacheDirectory, cacheSize.toLong())
            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory = sslContext.socketFactory
            val okBuilder = OkHttpClient.Builder()
            okBuilder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            okBuilder.readTimeout(30, TimeUnit.SECONDS)
            okBuilder.connectTimeout(30, TimeUnit.SECONDS)
            okBuilder.writeTimeout(30, TimeUnit.SECONDS)
            okBuilder.addInterceptor(HttpHeadInterceptor())
            // 持久化cookie
            okBuilder.addInterceptor(ReceivedCookiesInterceptor(context))
            okBuilder.addInterceptor(AddCookiesInterceptor(context))
            // 添加缓存，无网访问时会拿缓存,只会缓存get请求
            okBuilder.addInterceptor(AddCacheInterceptor(context))
            okBuilder.cache(cache)
            okBuilder.addInterceptor(
                HttpLoggingInterceptor()
                    .setLevel(
                        if (BuildConfig.DEBUG)
                            HttpLoggingInterceptor.Level.BODY
                        else
                            HttpLoggingInterceptor.Level.NONE
                    )
            )
            okBuilder.hostnameVerifier { hostname, session -> true }
            return okBuilder.build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }

    private fun getGson(): Gson? {
        if (gson == null) {
            val builder = GsonBuilder()
            builder.setLenient()
            builder.setFieldNamingStrategy(AnnotateNaming())
            builder.serializeNulls()
            gson = builder.create()
        }
        return gson
    }

    private class AnnotateNaming : FieldNamingStrategy {
        override fun translateName(field: Field): String {
            val a = field.getAnnotation(ParamNames::class.java)
            return if (a != null) a.value else FieldNamingPolicy.IDENTITY.translateName(field)
        }
    }

    private inner class HttpHeadInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val builder = request.newBuilder()
            builder.addHeader("Accept", "application/json;versions=1")
            if (isNetworkConnected(context)) {
                val maxAge = 60
                builder.addHeader("Cache-Control", "public, max-age=$maxAge")
            } else {
                val maxStale = 60 * 60 * 24 * 28
                builder.addHeader("Cache-Control", "public, only-if-cached, max-stale=$maxStale")
            }
            return chain.proceed(builder.build())
        }
    }

    fun isNetworkConnected(context: Context?): Boolean {
        context.let {
            val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val info = cm.activeNetworkInfo
            return info != null && info.isConnected
        }
        /**如果context为空，就返回false，表示网络未连接 */
        return false
    }

    private inner class ReceivedCookiesInterceptor internal constructor(private val context: Context?) : Interceptor {

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {

            val originalResponse = chain.proceed(chain.request())
            //这里获取请求返回的cookie
            if (!originalResponse.headers("Set-Cookie").isEmpty()) {

                val d = originalResponse.headers("Set-Cookie")

                // 返回cookie
                if (!TextUtils.isEmpty(d.toString())) {

                    val sharedPreferences = context?.getSharedPreferences("config", Context.MODE_PRIVATE)
                    val editorConfig = sharedPreferences?.edit()
                    val oldCookie = sharedPreferences?.getString("cookie", "")

                    val stringStringHashMap = HashMap<String, String>()

                    // 之前存过cookie
                    if (!TextUtils.isEmpty(oldCookie)) {
                        val substring = oldCookie!!.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        for (aSubstring in substring) {
                            if (aSubstring.contains("=")) {
                                val split =
                                    aSubstring.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                                stringStringHashMap[split[0]] = split[1]
                            } else {
                                stringStringHashMap[aSubstring] = ""
                            }
                        }
                    }
                    val join = StringUtils.join(d, ";")
                    val split = join.split(";".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()

                    // 存到Map里
                    for (aSplit in split) {
                        val split1 = aSplit.split("=".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                        if (split1.size == 2) {
                            stringStringHashMap[split1[0]] = split1[1]
                        } else {
                            stringStringHashMap[split1[0]] = ""
                        }
                    }

                    // 取出来
                    val stringBuilder = StringBuilder()
                    if (stringStringHashMap.size > 0) {
                        for (key in stringStringHashMap.keys) {
                            stringBuilder.append(key)
                            val value = stringStringHashMap[key]
                            if (!TextUtils.isEmpty(value)) {
                                stringBuilder.append("=")
                                stringBuilder.append(value)
                            }
                            stringBuilder.append(";")
                        }
                    }
                    editorConfig?.putString("cookie", stringBuilder.toString())
                    editorConfig?.apply()
                    //                    Log.e("jing", "------------处理后的 cookies:" + stringBuilder.toString());
                }
            }
            return originalResponse
        }

    }

    private inner class AddCookiesInterceptor internal constructor(private val context: Context?) : Interceptor {

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {

            val builder = chain.request().newBuilder()
            val sharedPreferences = context?.getSharedPreferences("config", Context.MODE_PRIVATE)
            val cookie = sharedPreferences?.getString("cookie", "")
            builder.addHeader("Cookie", cookie!!)
            return chain.proceed(builder.build())
        }
    }

    private inner class AddCacheInterceptor internal constructor(private val context: Context?) : Interceptor {

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {

            val cacheBuilder = CacheControl.Builder()
            cacheBuilder.maxAge(0, TimeUnit.SECONDS)
            cacheBuilder.maxStale(365, TimeUnit.DAYS)
            val cacheControl = cacheBuilder.build()
            var request = chain.request()
            if (!isNetworkConnected(context)) {
                request = request.newBuilder()
                    .cacheControl(cacheControl)
                    .build()
            }
            val originalResponse = chain.proceed(request)
            if (isNetworkConnected(context)) {
                // read from cache
                val maxAge = 0
                return originalResponse.newBuilder()
                    .removeHeader("Pragma")
                    .header("Cache-Control", "public ,max-age=$maxAge")
                    .build()
            } else {
                // tolerate 4-weeks stale
                val maxStale = 60 * 60 * 24 * 28
                return originalResponse.newBuilder()
                    .removeHeader("Pragma")
                    .header("Cache-Control", "public, only-if-cached, max-stale=$maxStale")
                    .build()
            }
        }
    }

}

