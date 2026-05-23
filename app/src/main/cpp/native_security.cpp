#include <jni.h>
#include <string>
#include <fstream>
#include <sstream>
#include <vector>
#include <algorithm>
#include <cctype>
#include <android/log.h>

#define LOG_TAG "NativeSecurity"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

static std::string toLower(const std::string& input) {
    std::string output = input;
    std::transform(output.begin(), output.end(), output.begin(),
                   [](unsigned char c) { return static_cast<char>(std::tolower(c)); });
    return output;
}

static bool fileContainsAnyKeyword(
        const std::string& path,
        const std::vector<std::string>& keywords
) {
    std::ifstream file(path);
    if (!file.is_open()) {
        return false;
    }

    std::string line;
    while (std::getline(file, line)) {
        std::string lowerLine = toLower(line);

        for (const auto& keyword : keywords) {
            if (lowerLine.find(keyword) != std::string::npos) {
                return true;
            }
        }
    }

    return false;
}

static bool isTracerPidDetected() {
    std::ifstream file("/proc/self/status");
    if (!file.is_open()) {
        return false;
    }

    std::string line;
    while (std::getline(file, line)) {
        if (line.rfind("TracerPid:", 0) == 0) {
            std::istringstream iss(line);
            std::string key;
            int tracerPid = 0;

            iss >> key >> tracerPid;
            return tracerPid > 0;
        }
    }

    return false;
}

static bool isHexPortInProcNetTcp(const std::string& path, const std::vector<std::string>& hexPorts) {
    std::ifstream file(path);
    if (!file.is_open()) {
        return false;
    }

    std::string line;

    // skip header
    std::getline(file, line);

    while (std::getline(file, line)) {
        std::string lowerLine = toLower(line);

        for (const auto& hexPort : hexPorts) {
            // /proc/net/tcp local_address 형태 예:
            // 0100007F:69A2
            // 27042 = 0x69A2, 27043 = 0x69A3
            std::string pattern = ":" + toLower(hexPort);
            if (lowerLine.find(pattern) != std::string::npos) {
                return true;
            }
        }
    }

    return false;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_kyung_kung_1android_security_NativeSecurityCheck_detectFridaInMaps(
        JNIEnv* env,
        jobject thiz
) {
    std::vector<std::string> keywords = {
            "frida",
            "gum-js-loop",
            "frida-agent",
            "frida-gadget",
            "libfrida"
    };

    bool detected = fileContainsAnyKeyword("/proc/self/maps", keywords);
    return detected ? JNI_TRUE : JNI_FALSE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_kyung_kung_1android_security_NativeSecurityCheck_detectTracerPid(
        JNIEnv* env,
        jobject thiz
) {
    bool detected = isTracerPidDetected();
    return detected ? JNI_TRUE : JNI_FALSE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_kyung_kung_1android_security_NativeSecurityCheck_detectFridaPorts(
        JNIEnv* env,
        jobject thiz
) {
    std::vector<std::string> fridaPorts = {
            "69A2", // 27042
            "69A3"  // 27043
    };

    bool tcpDetected = isHexPortInProcNetTcp("/proc/net/tcp", fridaPorts);
    bool tcp6Detected = isHexPortInProcNetTcp("/proc/net/tcp6", fridaPorts);

    return (tcpDetected || tcp6Detected) ? JNI_TRUE : JNI_FALSE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_kyung_kung_1android_security_NativeSecurityCheck_detectSuspiciousLibraries(
        JNIEnv* env,
        jobject thiz
) {
    std::vector<std::string> keywords = {
            "libfrida",
            "frida-gadget",
            "gum-js-loop",
            "re.frida.server"
    };

    bool detected = fileContainsAnyKeyword("/proc/self/maps", keywords);
    return detected ? JNI_TRUE : JNI_FALSE;
}