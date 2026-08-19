# 文本批量替换 (Txt Replacer) v1.0

一个**纯本地**的 Android 文本批量替换工具：粘贴/打开文本，用**多个可自定义的替换库**批量替换，支持常见编码（UTF-8 / GBK / GB2312 / Big5 等）。

**🔒 纯本地：所有数据只保存在手机本地，不联网、不上传、无广告、无追踪。**

## ✨ 功能

- 📚 **多替换库（多配置）**：可新建任意多个命名库（如「库1」「库2」「库3」…），每个库独立命名、独立保存自己的一套替换规则，适配不同场景，互不干扰
- 🔧 **批量替换**：每个库可添加任意多条规则（查找词 → 替换词），执行时按顺序全部替换，**非单次替换**
- 📝 **粘贴 / 打开文件**：可粘贴文本手动编辑，也可从手机选择 txt 文件读取（自动按所选编码解码）
- 🔤 **编码设置**：UTF-8 / GBK / GB2312 / Big5 / UTF-16LE / ASCII，读取和保存均可按所选编码
- 📖 **独立滚动编辑**：输入框、结果框各自独立滚动，长文本可上下滑动查看；支持长按选中、全选、删除
- 🧹 **清空按钮**：一键清空输入 / 清空结果
- 💾 **保存自由选路径**：保存时弹出系统文件选择器，自己选存放位置（默认 Download 等可写目录），**无需 root、无需存储权限**
- 📋 **一键复制**：替换结果可一键复制到剪贴板

## 📲 安装说明

> ⚠️ **重要：本 APK 未使用正式签名，属于"自签名/未签名"安装包。** 安装时会提示「未知来源」「未受信任的应用」等，属正常现象，按下面步骤处理。

### 方式一：直接安装（最简单）
1. 下载 APK 到手机
2. 打开时若提示「未知来源/允许安装未知应用」→ 允许
3. 若仍提示「未受信任」或无法安装，用「方式二」自行签名

### 方式二：自行签名后安装（推荐，可解决绝大多数安装失败）
APK 未签名，部分手机（尤其国内 ROM）会拦截。用电脑给 APK 签名后再安装：

1. 需要 Java 环境（JDK 8 及以上）
2. 生成签名文件（只需一次）：
   ```bash
   keytool -genkey -v -keystore mykey.jks -alias myalias -keyalg RSA -keysize 2048 -validity 10000
   ```
3. 对 APK 签名（用 apksigner，位于 Android SDK build-tools 目录）：
   ```bash
   apksigner sign --ks mykey.jks --out app-signed.apk app-unsigned.apk
   # 或旧版 jarsigner：
   # jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 -keystore mykey.jks app.apk myalias
   ```
4. 安装 `app-signed.apk` 即可

## 📦 下载

- **正式发布版**：右侧 **Releases** → `v1.0.0` → 下载 APK
- **最新构建**：Actions 页面 → 底部 **Artifacts** → `txt-replacer-apk`

## 🛠️ 构建（开发者）

```bash
./gradlew assembleDebug    # 调试版
./gradlew assembleRelease  # 发布版（需配置签名）
```
APK 输出在 `app/build/outputs/apk/<variant>/`。

云端自动构建：推送到 `main` 分支即触发 GitHub Actions 编译打包。

## 🧱 技术栈

- Kotlin + Android View (XML)
- Gradle + GitHub Actions 云端构建（CI）
- ICU4J charset（`com.ibm.icu:icu4j-charset`）支持 GBK/GB2312/Big5 等安卓原生不支持的编码

## 🔐 隐私声明

- 本应用**完全离线**运行，所有数据（替换库、规则、文本）仅保存在手机本地
- **不联网、不上传、不收集任何数据、无广告、无第三方 SDK**
- 无需任何权限即可运行
