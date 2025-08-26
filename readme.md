# 蓝牙遥控（Android）

一个基于 BLE 的蓝牙遥控应用。面向低功耗蓝牙设备，实现设备扫描、连接、指令下发与状态回传。适配 Android 6.0+，对 Android 12+ 的新蓝牙权限做了兼容处理。

## 功能特性
- 设备扫描与筛选：支持按名称/厂商字段/自定义规则过滤目标设备
- 连接与重连：自动重连、连接状态监听、断线恢复
- GATT 通信：读写特征值、订阅通知/指示，支持 MTU 调整与分包
- 指令协议：封装上行/下行帧格式，支持校验、粘包/拆包处理
- UI 交互：设备列表、连接状态、控制面板、日志与调试
- 适配与权限：Android 6–11 与 12+ 的权限适配、定位权限兼容策略
- 发布与签名：内置 release 签名说明与混淆配置

## 技术栈
- 语言：Java（Android）
- 依赖：AndroidX、BLE 栈（系统 `BluetoothGatt`/`BluetoothLeScanner`），第三方库 `WCHBLELibrary.jar`（位于 `app/libs`）
- 构建：Gradle/AGP

## 目录结构
- `app/`：主应用模块
  - `src/main/java/`：业务与蓝牙相关代码
  - `src/main/res/`：布局与资源
  - `src/main/AndroidManifest.xml`：权限与组件声明
  - `libs/WCHBLELibrary.jar`：蓝牙库
  - `proguard-rules.pro`：混淆规则
  - `build.gradle`：模块构建脚本
- `ui/`：可复用 UI 组件（如有）
- `build.gradle`、`settings.gradle`：项目级脚本
- `GenerateAPK.jks`：示例签名文件（仅用于本地测试）
- `release/app-release.apk`：打包产物（示例）

## 环境要求
- Android Studio Ladybug+ 或 Flamingo+
- Android Gradle Plugin 与 Gradle Wrapper（以项目脚本为准）
- Android SDK 31+（建议），含对应 Build-Tools
- 设备要求：Android 6.0+ 真实机（BLE 需硬件支持）

## 快速开始
1) 克隆项目并在 Android Studio 打开  
2) 配置 `local.properties` 的 SDK 路径（一般自动完成）
3) 同步 Gradle 并构建
4) 连接真机，运行 `app` 以 Debug 模式安装与调试

命令行构建：
```bash
# Windows PowerShell / CMD
gradlew assembleDebug
gradlew assembleRelease
```
产物位置：
- Debug：`app/build/outputs/apk/debug/`
- Release：`app/build/outputs/apk/release/`

## 签名与发布
- Keystore：示例 `GenerateAPK.jks` 位于项目根目录（仅测试用途，正式发版请更换）
- 在 `app/build.gradle` 中配置 `signingConfigs` 与 `buildTypes.release.signingConfig`
- 可通过 `gradlew assembleRelease` 生成正式包
- 混淆：在 `proguard-rules.pro` 中确保 BLE 库与反射相关类不被错误移除，建议保留第三方库公共 API

示例（参考，实际以项目脚本为准）：
```groovy
android {
    signingConfigs {
        release {
            storeFile file('../GenerateAPK.jks')
            storePassword 'your_password'
            keyAlias 'your_alias'
            keyPassword 'your_password'
        }
    }
    buildTypes {
        release {
            minifyEnabled true
            shrinkResources true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
            signingConfig signingConfigs.release
        }
    }
}
```

## 权限与适配（Android 6–14）
Android 12（API 31）起新增蓝牙运行时权限，请区分不同系统版本：

Manifest（示例）：
```xml
<!-- Android 12+ -->
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" android:usesPermissionFlags="neverForLocation" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />

<!-- Android 6–11（扫描需要定位权限） -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<!-- 或低精度 -->
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- 基础蓝牙（老版本） -->
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />

<!-- 可选：前台服务用于长连接保活 -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
```

运行时权限（示例逻辑）：
```java
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    String[] perms = new String[] {
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    };
    // request if not granted
} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    String[] perms = new String[] {
        Manifest.permission.ACCESS_FINE_LOCATION
    };
    // request if not granted
}
```

兼容性建议：
- Android 12+ 使用新权限替代定位权限；
- 若需基于广播内容判断位置，应遵循 `neverForLocation` 标记与合规提示；
- 勿在未授权时发起扫描与连接，注意处理拒绝与“不再询问”状态。

## BLE 通信设计
- 扫描策略：低功耗扫描（低频率）+ 前台界面提升扫描优先级；支持按名称前缀、厂商数据或 MAC 过滤
- 连接与重试：限制并发连接数；断开后指数退避重连；在连接成功后尽快发现服务并开启通知
- 服务/特征：
  - 自定义服务 UUID：`0000xxxx-0000-1000-8000-00805f9b34fb`（示例）
  - 写入特征：`WRITE_NO_RESPONSE` 优先，长度>MTU-3 做分包
  - 通知特征：`NOTIFY` 开启后接收下行数据
- MTU 与分包：
  - 连接建立后调用 `requestMtu(target)`，常见目标 185/247
  - 应用侧分包并附带序号/校验，设备侧回包 ACK 或错误码
- 数据帧（示例）：
  - 帧头(1B) | 命令(1B) | 长度(2B LE) | 负载(N) | 校验(1B/2B)
  - 避免粘包：接收侧按长度切割，校验后派发

## UI 与交互
- 设备列表：显示名称、RSSI、状态；支持下拉刷新与自动扫描
- 连接页：展示连接状态、服务/特征就绪、MTU 值
- 遥控面板：按钮/摇杆/滑块等控件映射为上行指令
- 调试面板：HEX 文本发送、原始日志查看、开关通知

## 配置与常量
- 设备名称前缀、服务/特征 UUID、扫描时长、重连策略等参数集中在常量类或配置文件中，便于二次定制
- 若集成 `WCHBLELibrary.jar` 的扫描/连接封装，请在初始化时设置回调与日志级别

## 日志与排错
- 建议使用结构化日志，区分模块（SCAN/CONN/GATT/PROTO）
- 捕获常见错误：`133`、`8`、`19`、`257` 等 GATT 错误码，并进行重试或清理
- 确保在主线程/合适线程操作 UI 与 GATT 回调
- 发生连接失败时：关闭扫描→延迟→重试连接；必要时清理 GATT 并重新获取实例

## 常见问题 FAQ
- 扫描不到设备？
  - 检查权限是否授权；Android 12+ 需要 `BLUETOOTH_SCAN`
  - 设备是否在广播；是否被其他手机占用连接
  - 尝试重启蓝牙或重置网络设置
- 连接频繁断开？
  - 距离/遮挡导致 RSSI 过低；供电不稳
  - 设备端空中升级/看门狗复位
  - 适当降低发送频率与 MTU，避免堆积
- 写入失败或通知收不到？
  - 检查服务/特征 UUID 是否匹配
  - 确认已开启通知（Descriptor 写入成功）
  - 拆包与校验逻辑是否一致

## 代码质量与混淆
- 对使用到的实体、回调接口保留混淆规则
- 禁止对反射或 JNI 相关类混淆
- 若使用 `WCHBLELibrary.jar`，需参考其文档添加 `-keep` 规则

## 二次开发指引
- 修改设备筛选：调整扫描回调中的过滤逻辑（名称/厂商数据）
- 更换 UUID：集中在常量类，替换服务/特征 UUID
- 扩展指令：在协议层新增命令字与编解码器
- UI 自定义：在控制面板增加控件并映射上行命令

## 许可证
- 仅用于学习或企业内部使用。第三方库版权归原作者所有，若商用请遵循相应协议与条款。
