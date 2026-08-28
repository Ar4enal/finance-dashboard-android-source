# 金融工作台 Android APK 工程

这是为局域网 FastAPI 服务准备的 Android WebView 客户端。

功能：首次配置服务器 IP/端口；测试连接；SharedPreferences 持久化；下次自动连接；菜单修改服务器；WebView DOM Storage；横竖屏切换；支持 HTTP 局域网服务。

## 构建
1. 用 Android Studio 打开 `android-app`。
2. 等待 Gradle 同步并安装 SDK 35。
3. Build > Build APK(s)。
4. APK 输出目录通常为 `app/build/outputs/apk/debug/app-debug.apk`。

手机与服务器需要在同一局域网。服务器启动后填写电脑的局域网 IP，例如 `192.168.1.100`，端口 `8000`。

