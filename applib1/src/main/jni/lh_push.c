#include <jni.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <lh-jni.h>
#include <libavformat/avformat.h>
#include <android/native_window.h>
#include <unistd.h>
#include <android/native_window_jni.h>
JNIEXPORT void JNICALL
Java_com_example_applib1_pushstream_PushNative_setVideoOptions(JNIEnv *env, jobject instance,
        jint width, jint height,
        jint bitrate, jint fps) {

// TODO

}

JNIEXPORT void JNICALL
Java_com_example_applib1_pushstream_PushNative_fireVideoData(JNIEnv *env, jobject instance,
        jbyteArray data_) {
jbyte *data = (*env)->GetByteArrayElements(env, data_, NULL);

// TODO

(*env)->ReleaseByteArrayElements(env, data_, data, 0);
}

JNIEXPORT void JNICALL
Java_com_example_applib1_pushstream_PushNative_setAudioOptions(JNIEnv *env, jobject instance,
        jint sampleRateInHz, jint channel) {

// TODO

}

JNIEXPORT void JNICALL
Java_com_example_applib1_pushstream_PushNative_fireAudioData(JNIEnv *env, jobject instance,
        jbyteArray data_, jint len) {
jbyte *data = (*env)->GetByteArrayElements(env, data_, NULL);

// TODO

(*env)->ReleaseByteArrayElements(env, data_, data, 0);
}

JNIEXPORT void JNICALL
Java_com_example_applib1_pushstream_PushNative_startPush(JNIEnv *env, jobject instance,
        jstring url_) {
const char *url = (*env)->GetStringUTFChars(env, url_, 0);

// TODO

(*env)->ReleaseStringUTFChars(env, url_, url);
}

JNIEXPORT void JNICALL
Java_com_example_applib1_pushstream_PushNative_stopPush(JNIEnv *env, jobject instance) {

// TODO

}

JNIEXPORT void JNICALL
Java_com_example_applib1_pushstream_PushNative_release(JNIEnv *env, jobject instance) {

// TODO

}
