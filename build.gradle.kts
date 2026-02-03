import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.gradleup.shadow") version "9.3.1"
}

// 项目基础信息配置
val groupName = "io.starflow.client"
val versionStr = "v1.0-SNAPSHOT"
val lastChangeTime = "20260203"
val lwjglVersion = "3.3.6"
val lwjglNatives = "natives-windows"
val mixinVersion = "0.8.7"

group = groupName
version = versionStr

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(8)
    options.compilerArgs.add("-Xlint:-options")
    options.encoding = "UTF-8"

    options.isIncremental = true
    options.isFork = true
    options.forkOptions.jvmArgs?.addAll(listOf("-XX:+UseZGC"))

    // Mixin 注解处理器配置
    options.compilerArgs.addAll(listOf(
        "-AoutRefMapFile=${project.layout.buildDirectory.get()}/resources/main/mixins.starflow.refmap.json",
        "-AreferencedMapFile=${project.projectDir}/src/main/resources/mixins.starflow.refmap.json",
        "-AdefaultObfuscationEnv=notch"
    ))
}

repositories {
    mavenCentral()
    maven { url = uri("${projectDir}/libs/mavens") }
    maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
//    maven { url = uri("https://maven.lwjgl.org") }
    maven { url = uri("https://maven.cleanroommc.com") }
    maven { url = uri("https://repo.spongepowered.org/repository/maven-public/") }
    maven { url = uri("https://nexus.gtnewhorizons.com/repository/public/") }
    maven { url = uri("https://oss.sonatype.org/service/local/repositories/releases/content/") }
    maven { url = uri("https://maven.aliyun.com/repository/public/") }
    maven { url = uri("https://libraries.minecraft.net/") }
    maven { url = uri("https://repo.marcloud.net/") }
}

dependencies {
    implementation(fileTree("libs") { include("*.jar") })

    val lwjglModules = listOf("lwjgl", "lwjgl-glfw", "lwjgl-openal", "lwjgl-opengl", "lwjgl-nfd", "lwjgl-stb")
    lwjglModules.forEach { module ->
        implementation("org.lwjgl:$module:$lwjglVersion")
        runtimeOnly("org.lwjgl:$module:$lwjglVersion:$lwjglNatives")
    }

    // Mixin 框架与注入支持
    implementation("org.spongepowered:mixin:$mixinVersion") { isTransitive = false }
    annotationProcessor("org.spongepowered:mixin:$mixinVersion:processor")
    implementation("zone.rong:mixinbooter:10.7")
    annotationProcessor("zone.rong:mixinbooter:10.7")
    annotationProcessor("com.google.guava:guava:32.1.3-jre")
    annotationProcessor("com.google.code.gson:gson:2.10.1")

    // 系统与底层访问
    implementation("com.github.oshi:oshi-core:4.4.2")
    implementation("net.java.dev.jna:jna:5.14.0")
    implementation("net.java.dev.jna:jna-platform:5.14.0")

    // 工具库
    implementation("com.google.guava:guava:32.1.3-jre")
    implementation("org.apache.commons:commons-lang3:3.18.0")
    implementation("commons-io:commons-io:2.16.1")
    implementation("commons-codec:commons-codec:1.17.1")
    implementation("org.apache.commons:commons-compress:1.26.0")
    implementation("org.joml:joml:1.10.5")

    // 网络处理
    implementation("io.netty:netty-all:4.0.23.Final")

    // 数据处理与日志
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.apache.logging.log4j:log4j-api:2.25.3")
    implementation("org.apache.logging.log4j:log4j-core:2.25.3")

    // HTTP 客户端
    implementation("org.apache.httpcomponents:httpclient:4.5.14")
    implementation("org.apache.httpcomponents:httpcore:4.4.16")

    // 输入支持
    implementation("net.java.jinput:jinput:2.0.5")
    implementation("net.java.jinput:jinput-platform:2.0.5")
    implementation("net.java.jutils:jutils:1.0.0")

    // Mojang 官方依赖
    implementation("com.mojang:icu4j-core-mojang:51.2")
    implementation("net.sf.jopt-simple:jopt-simple:5.0.4")
    implementation("com.mojang:authlib:1.5.21")
    implementation("com.mojang:patchy:1.7.7")
}

tasks.named<ShadowJar>("shadowJar") {
    archiveFileName.set("StarFlow-$versionStr-$lastChangeTime.jar")
    configurations.set(listOf(project.configurations.runtimeClasspath.get()))
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    exclude("META-INF/versions/**")
    exclude("**/module-info.class")
    mergeServiceFiles()
    isZip64 = true

    manifest {
        attributes(
            "Main-Class" to "net.minecraft.client.main.Main",
            "MixinConfigs" to "mixins.starflow.json",
            "FMLCorePluginContainsFMLMod" to "true",
            "ForceLoadAsMod" to "true",
            "Implementation-Version" to versionStr,
            "Multi-Release" to "false"
        )
    }
}

tasks.register<JavaExec>("RunClient") {
    group = "application"
    description = "RunClient"

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("Start")
    workingDir = file("run")
    args("--username", "Developer")

    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(25))
    })

    jvmArgs("--enable-native-access=ALL-UNNAMED", "-Dmixin.debug.export=true", "-XX:+UseZGC")
}