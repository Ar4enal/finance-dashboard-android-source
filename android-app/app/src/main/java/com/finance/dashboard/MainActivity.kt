package com.finance.dashboard

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.webkit.*
import android.widget.*
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : android.app.Activity() {
    private val prefs by lazy { getSharedPreferences("finance_dashboard", MODE_PRIVATE) }
    private lateinit var webView: WebView
    private lateinit var root: FrameLayout
    private var serverUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root = FrameLayout(this)
        setContentView(root)
        serverUrl = prefs.getString("server_url", null)
        if (serverUrl.isNullOrBlank()) showSettings() else showWeb(serverUrl!!)
    }

    private fun normalize(ip: String, port: String): String? {
        val host = ip.trim().removePrefix("http://").removePrefix("https://").trimEnd('/')
        val p = port.trim().toIntOrNull() ?: return null
        if (host.isBlank() || p !in 1..65535) return null
        return "http://$host:$p"
    }

    private fun showSettings() {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(48, 64, 48, 48) }
        val title = TextView(this).apply { text = "金融工作台\n服务器设置"; textSize = 26f; setTextColor(Color.rgb(20,60,110)); setPadding(0,0,0,32) }
        val ip = EditText(this).apply { hint = "服务器 IP，例如 192.168.1.100"; setText(serverUrl?.replace(Regex("https?://|:[0-9]+$"), "") ?: "") }
        val port = EditText(this).apply { hint = "端口，例如 8000"; inputType = 2; setText(serverUrl?.substringAfterLast(':') ?: "8000") }
        val status = TextView(this).apply { setPadding(0,24,0,24); text = "请输入运行金融工作台后端的局域网 IP 和端口" }
        val test = Button(this).apply { text = "测试连接" }
        val save = Button(this).apply { text = "保存并进入"; isEnabled = false }
        test.setOnClickListener {
            val url = normalize(ip.text.toString(), port.text.toString())
            if (url == null) { status.text = "IP 或端口格式不正确"; return@setOnClickListener }
            status.text = "正在连接…"; save.isEnabled = false
            thread {
                val ok = try { val c = URL(url).openConnection() as HttpURLConnection; c.connectTimeout=4000; c.readTimeout=4000; c.requestMethod="GET"; c.connect(); c.responseCode in 200..499 } catch (_: Exception) { false }
                runOnUiThread { status.text = if (ok) "连接成功：$url" else "无法连接，请确认电脑服务已启动、IP/端口正确且在同一局域网"; save.isEnabled = ok }
            }
        }
        save.setOnClickListener {
            val url = normalize(ip.text.toString(), port.text.toString()) ?: return@setOnClickListener
            prefs.edit().putString("server_url", url).apply(); serverUrl=url; showWeb(url)
        }
        box.addView(title); box.addView(ip); box.addView(port); box.addView(status); box.addView(test); box.addView(save)
        root.removeAllViews(); root.addView(box)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun showWeb(url: String) {
        webView = WebView(this).apply {
            settings.javaScriptEnabled = true; settings.domStorageEnabled = true; settings.databaseEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false; settings.loadWithOverviewMode = true; settings.useWideViewPort = true
            webViewClient = object : WebViewClient() { override fun shouldOverrideUrlLoading(v: WebView, r: WebResourceRequest): Boolean { v.loadUrl(r.url.toString()); return true } }
            webChromeClient = WebChromeClient()
            loadUrl(url)
        }
        root.removeAllViews(); root.addView(webView)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean { menu.add("服务器设置").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER); return true }
    override fun onOptionsItemSelected(item: MenuItem): Boolean { if (item.title == "服务器设置") { showSettings(); return true }; return super.onOptionsItemSelected(item) }
    override fun onBackPressed() { if (::webView.isInitialized && webView.canGoBack()) webView.goBack() else super.onBackPressed() }
}
