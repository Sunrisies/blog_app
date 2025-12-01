import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.sunrise.blog"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.sunrise.blog"
        minSdk = 26
        targetSdk = 36
        versionCode = generateVersionCode()
        versionName = generateVersionName()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // 禁用 APK 分割以避免构建问题
            splits.abi.isEnable = false
        }
    }
    
    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true  // 生成通用 APK
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    kotlinOptions {
        jvmTarget = "11"
    }
    
    buildFeatures {
        compose = true
        viewBinding = true
    }
    
    applicationVariants.all{
        val variant = this
        variant.outputs
            .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
            .forEach { output ->
                val outputFileName = "blog-${variant.versionName}.apk"
                output.outputFileName = outputFileName
            }
    }
    
    // 添加 GStreamer 的本地库路径
    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("src/main/jniLibs")
        }
    }
}

// 生成版本代码（基于时间戳）
fun generateVersionCode(): Int {
    // 使用当前时间的分钟数作为版本代码（确保每次构建都不同）
    val minutes = System.currentTimeMillis() / (1000 * 60)
    return minutes.toInt()
}

// 生成版本名称（包含时间和版本信息）
fun generateVersionName(): String {
    val baseVersion = "1.1"
    val date = SimpleDateFormat("yyyyMMdd-HHmm", Locale.getDefault()).format(Date())
    return "$baseVersion-$date"
}

dependencies {
    implementation(libs.androidx.room.common.jvm)
    val nav_version = "2.9.5"
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.navigation:navigation-compose:${nav_version}")
    implementation("com.github.jenly1314.UltraSwipeRefresh:refresh:1.4.2")
    implementation("com.github.jenly1314.UltraSwipeRefresh:refresh-indicator-classic:1.4.2")
    // 网络请求
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    // 图片加载
    implementation("io.coil-kt:coil-compose:2.4.0")

    // 下拉刷新
    implementation("com.google.accompanist:accompanist-swiperefresh:0.27.0")
    // Markdown 渲染
    implementation("io.noties.markwon:core:4.6.2")
    implementation("io.noties.markwon:image-coil:4.6.2")
    implementation("io.noties.markwon:html:4.6.2")
    implementation("androidx.compose.material:material-icons-extended:1.5.4")
    implementation("com.google.code.gson:gson:2.10.1")

    // Room 数据库
    implementation("androidx.room:room-runtime:2.6.0")
    implementation("androidx.room:room-ktx:2.6.0")
    ksp("androidx.room:room-compiler:2.6.0")

    // 加密存储（可选，但推荐用于密码管理）
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("androidx.core:core-ktx:1.6.0")
    
    // 视频播放器 - 使用本地 GStreamer 库
    
    // ViewModel 和 LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}