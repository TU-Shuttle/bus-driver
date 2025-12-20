import java.net.URI

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)

    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

// .env
val envFile = rootProject.file(".env")
val envVars = mutableMapOf<String, String>()

if (envFile.exists()) {
    envFile.readLines().forEach { line ->
        val trimmed = line.trim()
        if (trimmed.isNotEmpty() && !trimmed.startsWith("#") && trimmed.contains("=")) {
            val (key, value) = trimmed.split("=", limit = 2)
            envVars[key.trim()] = value.trim()
        }
    }
}

fun getEnv(key: String, defaultValue: String = ""): String {
    return envVars[key] ?: System.getenv(key) ?: defaultValue
}

fun extractDomainFromUrl(url: String): String? {
    return try {
        val uri = URI(url)
        uri.host
    } catch (_: Exception) {
        null
    }
}

// network_security_config.xml 생성
fun generateNetworkSecurityConfig(): String {
    val baseUrl = getEnv("BASE_URL")
    val wsUrl = getEnv("WS_URL")

    val baseDomain = extractDomainFromUrl(baseUrl)
    val wsDomain = extractDomainFromUrl(wsUrl)

    val domains = mutableSetOf<String>()

    // 에뮬레이터용
    domains.add("10.0.2.2")

    // .env에서 읽은 도메인 추가
    baseDomain?.let { domains.add(it) }
    wsDomain?.let { domains.add(it) }

    val isHttps = baseUrl.startsWith("https://") || wsUrl.startsWith("wss://")
    val cleartextPermitted = !isHttps

    val domainConfigs = domains.joinToString("\n        ") { domain ->
        "<domain includeSubdomains=\"true\">$domain</domain>"
    }

    return """<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <!-- .env에서 자동 생성됨 -->
    <domain-config cleartextTrafficPermitted="$cleartextPermitted">
        $domainConfigs
    </domain-config>
</network-security-config>
"""
}

android {
    namespace = "com.tukorea.driver"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.tukorea.driver"
        minSdk = 25
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["naverMapClientId"] = getEnv("NAVER_MAP_CLIENT_ID")
    }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"${getEnv("BASE_URL")}\"")
            buildConfigField("String", "WS_URL", "\"${getEnv("WS_URL")}\"")
        }
        release {
            isMinifyEnabled = false
            buildConfigField("String", "BASE_URL", "\"${getEnv("BASE_URL")}\"")
            buildConfigField("String", "WS_URL", "\"${getEnv("WS_URL")}\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    // network_security_config.xml 자동 생성
    val networkSecurityConfigFile = file("src/main/res/xml/network_security_config.xml")
    tasks.register("generateNetworkSecurityConfig") {
        doLast {
            networkSecurityConfigFile.parentFile.mkdirs()
            networkSecurityConfigFile.writeText(generateNetworkSecurityConfig())
        }
    }

    // 빌드 전에 network_security_config.xml 생성
    tasks.named("preBuild").configure {
        dependsOn("generateNetworkSecurityConfig")
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.annotation)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.activity)
    implementation(libs.compose.viewmodel)
    implementation(libs.compose.runtime.livedata)
    implementation(libs.compose.navigation)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.accompanist.permissions)
    debugImplementation(libs.compose.ui.tooling)

    // Retrofit
    implementation(libs.retrofit)

    // Gson Converter (JSON 직렬화/역직렬화용)
    implementation(libs.converter.gson)

    // OkHttp Logging (네트워크 로그 디버깅용, 선택 사항)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // Kotlin + Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // SplashScreen
    implementation(libs.androidx.core.splashscreen)

    // Naver Map SDK
    implementation(libs.naver.map.sdk)

    // Google Play Services Location
    implementation(libs.play.services.location)
}

// ktlint 설정
ktlint {
    version.set("1.2.1")
    debug.set(false)
    verbose.set(true)
    android.set(true)
    outputToConsole.set(true)
    outputColorName.set("RED")
    ignoreFailures.set(false)
    enableExperimentalRules.set(true)
    filter {
        exclude("**/generated/**")
        exclude("**/build/**")
        include("**/kotlin/**")
    }
}

// detekt 설정
detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(file("$rootDir/detekt.yml"))
    baseline = file("$rootDir/detekt-baseline.xml")
}

// detekt 리포트 설정 (task 레벨)
tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(false)
        sarif.required.set(true)
    }
}
