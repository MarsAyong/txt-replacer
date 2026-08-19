# 文本批量替换 (Txt Replacer)

一个**纯本地**的 Android 应用：粘贴/打开文本，用**多个可自定义的替换库**批量替换，支持常见编码。

## 功能

- 📚 **多替换库（多配置）**：可新建任意多个命名库（如「戒色1号库」「其他2号库」「3号库」…），每个库独立命名、独立管理自己的替换规则，适配不同场景
- 🔧 **批量替换**：每个库可添加任意多条规则（查找词 → 替换词），执行时按顺序全部替换，非单次替换
- 📝 **粘贴 / 打开文件**：可粘贴文本手动编辑，也可从手机选择 txt 文件读取
- 🔤 **编码设置**：UTF-8 / GBK / GB2312 / Big5 / UTF-16LE / ASCII，读取和保存均可按所选编码
- 💾 **保存**：替换结果可复制，或保存为 .txt 文件（应用目录内，无需存储权限）
- 🔒 **纯本地**：所有数据只存在手机本地，不联网、不上传

## 下载

APK 在右侧 **Releases**（发布版）或 Actions 构建产物里下载。
（本次为测试版，直接在 Actions 的 artifacts 中下载 `txt-replacer-apk`。）

## 构建

```bash
./gradlew assembleRelease
```

APK 输出在 `app/build/outputs/apk/release/`。

## 技术栈

- Kotlin + Android View (XML)
- Gradle + GitHub Actions 云端构建
- ICU4J 支持 GBK/Big5 等编码
