#include <jni.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <lh-jni.h>
#include <libavformat/avformat.h>
//#include "lh-jni.h"
//#include "libavformat/avformat.h"

//#define __STDC_CONSTANT_MACROS



JNIEXPORT jstring JNICALL
Java_com_example_applib1_JniTest_getStrFromC__(JNIEnv *env, jclass type) {
    return (*env)->NewStringUTF(env, "String from C");
}

JNIEXPORT jstring JNICALL
Java_com_example_applib1_JniTest_getStrFromC__I(JNIEnv *env, jobject instance, jint i) {
    return (*env)->NewStringUTF(env, "String from C getStrFromC__I");
}
//基本数据
//Java基本数据类型与JNI数据类型的映射关系（在C/C++中用特定的类型来表示Java的数据类型）
/*
boolean jboolean
byte jbyte
char jchar
short jshort
int jint
long jlong
float jfloat
double jdouble
void void
*/

//引用类型（对象）
//String jstring
//object jobject
//基本类型的数组也是引用类型
//byte[] jByteArray
//Class  jclass

//内存释放相关：函数处理后
// 1.释放本地引用：释放本地的c 指针指向内存
// 2.若返回jni类型数据则无需处理，不返回则手动删除Local Refrence

//1.访问属性：修改属性key的字符串
JNIEXPORT jstring JNICALL
Java_com_example_applib1_JniTest_accessField(JNIEnv *env, jobject instance) {
    //类似java反射的方法
    //拿属性值套路：1.得到jclass 2.得到方法id（env,jclass,名称，签名（参数+返回值签名串）） 3.得到属性值jstring
    //修改jstring并返回套路：1.将jni的jstring类型转成c的char *类型 2.c语言char类型操作 3.将c的char类型转成jni的jsting类型
                //4.将修改后的值改设置到 属性值中
    //套路1-拿属性值：
    jclass clazz = (*env)->GetObjectClass(env, instance);//从jobject拿jclass
        //拿id的方法
    jfieldID fid = (*env)->GetFieldID(env,clazz, "key", "Ljava/lang/String;");//通过唯一参数找属性id
        //获取属性key对应的值
        //注意规则(*env)->Get<Type>Field(); 这里的Type的jni的类型
    jstring jstr = (*env)->GetObjectField(env,instance,fid);//从jobject中拿属性值

    //套路2-使用c处理完字符串，然后转jni类型jstring
    char *strs = (*env)->GetStringUTFChars(env,jstr,NULL);//c转jni
    char text[50] = "super";//------------------------------------------ 这里为啥不能用指针
    strcat(text,strs);//c处理逻辑. 这里是将strs追加到text后面
    (*env)->ReleaseStringChars(env,jstr,strs);// 释放本地引用：释放本地的c 指针指向内存
    jstr = (*env)->NewStringUTF(env,text);

    //修改jobject属性值。也就是java层对象的属性值。等待自动释放
    (*env)->SetObjectField(env,instance,fid,jstr);

//    (*env)->ReleaseStringChars(env,jstr,text);// 这里不能释放，invalid address or address of corrupt block 0x7ffb650934 passed to dlfree

    //-------------------------疑问：这里直接创建jstring返回了，那前面的c的多指针指向内存和传入的jsting如何回收
    return jstr;
}

JNIEXPORT jstring JNICALL
Java_com_example_applib1_JniTest_accessStaticField(JNIEnv *env, jobject instance) {
    //类似java反射的方法
    //拿属性值套路：1.得到jclass 2.得到方法id（env,jclass,名称，签名（参数+返回值签名串）） 3.得到属性值jstring
    //修改jstring并返回套路：1.将jni的jstring类型转成c的char *类型 2.c语言char类型操作 3.将c的char类型转成jni的jsting类型
    //4.将修改后的值改设置到 属性值中
    //套路1-拿属性值：
    jclass clazz = (*env)->GetObjectClass(env, instance);//从jobject拿jclass
    //拿id的方法
    jfieldID fid = (*env)->GetStaticFieldID(env, clazz, "count", "I");//通过唯一参数找属性id
    //获取属性key对应的值
    //注意规则(*env)->Get<Type>Field(); 这里的Type的jni的类型
    jint count = (*env)->GetStaticIntField(env,clazz,fid);//从jobject中拿属性值

    count++;

    //修改jobject属性值。也就是java层对象的属性值。等待自动释放
    (*env)->SetStaticIntField(env,clazz,fid,count);
    //-------------------------疑问：这里直接创建jstring返回了，那前面的c的多指针指向内存和传入的jsting如何回收
    return NULL;
}

JNIEXPORT jint JNICALL
Java_com_example_applib1_JniTest_accessMethod(JNIEnv *env, jobject instance) {
    //类似java反射的方法
    //拿属性值套路：1.得到jclass 2.得到方法id（env,jclass,名称，签名（参数+返回值签名串）） 3.得到属性值jstring
    //修改jstring并返回套路：1.将jni的jstring类型转成c的char *类型 2.c语言char类型操作 3.将c的char类型转成jni的jsting类型
    //4.将修改后的值改设置到 属性值中
    //套路1-拿属性值：
    jclass clazz = (*env)->GetObjectClass(env, instance);//从jobject拿jclass
    //拿id的方法
    jmethodID fid = (*env)->GetMethodID(env, clazz, "getRandomInt", "(I)I");//通过唯一参数找属性id
    //获取属性key对应的值
    //注意规则(*env)->Get<Type>Field(); 这里的Type的jni的类型
    jint random = (*env)->CallIntMethod(env,instance,fid,5);//从jobject中拿属性值

    return random;
//    FILE *fp = fopen("D://log.txt","w");
//    char str[50];
//    sprintf(str,"%d",random);
//    fputs(str,fp);
//    fclose(fp);
}

JNIEXPORT jstring JNICALL
Java_com_example_applib1_JniTest_accessStaticMethod(JNIEnv *env, jobject instance) {
    jclass pVoid = (*env)->GetObjectClass(env, instance);
    jmethodID pID = (*env)->GetStaticMethodID(env, pVoid, "getUUID", "()Ljava/lang/String;");
    jstring uuid = (*env)->CallStaticObjectMethod(env, pVoid, pID);
    //转c 类型
    char *string = (*env)->GetStringUTFChars(env, uuid, NULL);

//    char filename[100] = {0};
//    sprintf(filename,"D://%s.txt",string);
//    FILE *fp = fopen(filename,"w");
//    fputs(string,fp);
//    fclose(fp);
    return  (*env)->NewStringUTF(env,string);

}

JNIEXPORT jstring JNICALL
Java_com_example_applib1_JniTest_getStrFromC__ILjava_lang_String_2(JNIEnv *env, jobject instance,
                                                                   jint i, jstring j_) {
    const char *j = (*env)->GetStringUTFChars(env, j_, 0);

    // TODO

    (*env)->ReleaseStringUTFChars(env, j_, j);

//    return (*env)->NewStringUTF(env, returnValue);
    return NULL;
}

JNIEXPORT jobject JNICALL
Java_com_example_applib1_JniTest_getDate(JNIEnv *env, jobject instance) {
    //类似反射的套路 1.class 2.构造方法id 3.执行NewObject方法，传递class、mid 4.拿到执行方法mid 5.执行mid
    jclass clazz = (*env)->FindClass(env,"java/util/Date");
    jmethodID constructorID = (*env)->GetMethodID(env, clazz, "<init>", "()V");
    jobject jobj = (*env)->NewObject(env, clazz, constructorID);

    //拿到Date的 long getTime()方法的id
    jmethodID getTimeId = (*env)->GetMethodID(env, clazz, "getTime", "()J");
    jlong i = (*env)->CallLongMethod(env, jobj, getTimeId);
    printf("\ntime:%lld\n",i);
    return jobj;
}

JNIEXPORT jstring JNICALL
Java_com_example_applib1_JniTest_callNonvirtualMethod(JNIEnv *env, jobject instance) {
    //1.拿到属性对象 2.拿到属性对象 要调用的方法id 3.指定 是调用当前对象方法还是父类方法
    //套路1开始
    jclass clazz = (*env)->GetObjectClass(env, instance);
    jfieldID fid = (*env)->GetFieldID(env, clazz, "human", "Lcom/example/applib1/Human;");
    jobject fidObj = (*env)->GetObjectField(env, instance,fid);

    //这里使用当前类的方法会失败，得到的Class需要时父类的class
    jclass humanClazz = (*env)->GetObjectClass(env,fidObj);
//    jclass humanClazz = (*env)->FindClass(env,"com/example/applib1/Human");

    jmethodID methodid = (*env)->GetMethodID(env, humanClazz, "sayHi", "()Ljava/lang/String;");
    
    //执行当前对象的的方法
    jstring jstr = (*env)->CallObjectMethod(env, fidObj, methodid);
    //执行父类对象的方法
    jstring jstr2 = (*env)->CallNonvirtualObjectMethod(env, fidObj, humanClazz, methodid);
    char chars[100] = "";
    const char *string = (*env)->GetStringUTFChars(env,jstr,NULL);
    strcat(chars,string);//这里不能直接使用 c的函数拼接jni类型的jstring
    (*env)->ReleaseStringChars(env,jstr,string);
    const char *string2 = (*env)->GetStringUTFChars(env,jstr2,NULL);
    strcat(chars,string2);
    (*env)->ReleaseStringChars(env,jstr2,string2);
    jstring pVoid = (*env)->NewStringUTF(env, chars);

    return pVoid;
}

JNIEXPORT jstring JNICALL
Java_com_example_applib1_JniTest_chineseChars(JNIEnv *env, jobject instance, jstring str_) {
    const char *str = (*env)->GetStringUTFChars(env, str_, NULL);
//    char *str ="好啊prefix";
////    str = aaa;
////    strcat(aaa,str);
//
    //通过java层的String对象，有构造方法public String(byte bytes[],String charsetName)
    //1.class 2.构造id 3.参数1 、参数2 4 构造对象
    jclass strClazz = (*env)->FindClass(env, "java/lang/String");
    jmethodID pID = (*env)->GetMethodID(env, strClazz, "<init>", "([BLjava/lang/String;)V");//方法的签名要括起来

    //参数1 。参数最终要的是jni的jbyteArray类型，jString类型
    //创建jbyteArray
    jbyteArray jbyteArray1 = (*env)->NewByteArray(env, strlen(str));
    //给jbyteArray赋值
    (*env)->SetByteArrayRegion(env,jbyteArray1,0,strlen(str),str);
    jstring charset = (*env)->NewStringUTF(env, "GB2312");


    jstring pVoid = (*env)->NewObject(env, strClazz, pID, jbyteArray1, charset);
    (*env)->ReleaseStringChars(env,pVoid,str);
    return pVoid;

    //C 返回 java 字符串
//    char *c_str = "C 字符串";
//    jclass str_cls = (*env)->FindClass(env, "java/lang/String");
//    jmethodID jmid = (*env)->GetMethodID(env, str_cls, "<init>", "([BLjava/lang/String;)V");
//
//    //jstring -> jbyteArray
//    jbyteArray bytes = (*env)->NewByteArray(env, strlen(c_str));
//    // 将Char * 赋值到 bytes
//    (*env)->SetByteArrayRegion(env, bytes, 0, strlen(c_str), c_str);
//    jstring charsetName = (*env)->NewStringUTF(env, "GB2312");
//
//    return (*env)->NewStringUTF(env,c_str);

}

JNIEXPORT jstring JNICALL
Java_com_example_applib1_JniTest_chineseChars1(JNIEnv *env, jobject instance, jbyteArray bytes_) {
    jbyte *bytes = (*env)->GetByteArrayElements(env, bytes_, NULL);

    (*env)->ReleaseByteArrayElements(env, bytes_, bytes, 0);

    return NULL;//(*env)->NewStringUTF(env, returnValue);
}

int compare(int *a,int *b){
    return (*b) - (*a);
}

JNIEXPORT jintArray JNICALL
Java_com_example_applib1_JniTest_giveArray(JNIEnv *env, jobject instance, jintArray array_) {
    jint *pInt = (*env)->GetIntArrayElements(env, array_, NULL);

    int len = (*env)->GetArrayLength(env,array_);//--------------------------注意要传入的参数是jarray
    qsort(pInt,len,sizeof(jint),compare);

    //开始同步
    //0 ，java数组进行鞥更新，并且释放c/c++数据
    //JNI_ABORT ，Java数据进行不更新，但是释放c/c++是数组
    //JNI_COMMIT，Java数组尽心刚更新，不释放c/c++数组（函数执行完成会释放数组）
    (*env)->ReleaseIntArrayElements(env,array_,pInt,0);
    return array_;
}

JNIEXPORT jintArray JNICALL
Java_com_example_applib1_JniTest_getArray(JNIEnv *env, jobject instance, jint len) {
    //1.new一个jni类型的数组-jintArray,2.转成c指针 3.c赋值， 4.同步
    jintArray jint_arr = (*env)->NewIntArray(env, len);//
    jint *elems = (*env)->GetIntArrayElements(env, jint_arr, NULL);
    for(int i=0;i<len;i++){
//        *(elems++)=i; //这句为什么报错
        elems[i]=i;
    }
    (*env)->ReleaseIntArrayElements(env,jint_arr,elems,0);
    return jint_arr;
}

JNIEXPORT jobjectArray JNICALL
Java_com_example_applib1_JniTest_getArray1(JNIEnv *env, jobject instance, jint len) {
    jclass jclazz = (*env)->FindClass(env, "java/lang/String");
//    jmethodID constructorId = (*env)->GetMethodID(env, jclazz, "<init>", "()V");
//    jobject jobject1 = (*env)->NewObject(env, jclazz, constructorId);

    jstring jobjectArray1 = (*env)->NewObjectArray(env, len, jclazz, 0);//jobject1);
    len = (*env)->GetArrayLength(env, jobjectArray1);
//    jstring *jstr = (*env)->GetObjectArrayElement(env, jobjectArray1, len);
    for(int i=0;i<len;i++){
//        const char *string = (*env)->GetStringUTFChars(env,*jstr, NULL);
        char chars[100] = "i   ";
//        strcat(chars,string);
        jstring  jstr = (*env)->NewStringUTF( env, chars );
//        (*env)->ReleaseStringChars(env,jstr,chars);//这里不能释放，用的是NewStringUTF，而不是GetStringUTF invalid address or address of corrupt block 0x7ffb650904 passed to dlfree
        (*env)->SetObjectArrayElement(env,jobjectArray1,i,jstr);

    }
    return jobjectArray1;
}

JNIEXPORT void JNICALL
Java_com_example_applib1_JniTest_localRef(JNIEnv *env, jobject instance) {
    jclass pVoid = (*env)->FindClass(env, "java/util/Date");
    jmethodID pID = (*env)->GetMethodID(env, pVoid, "<init>", "()V");
    
    for(int i=0;i<5;i++){
        jobject object = (*env)->NewObject(env, pVoid, pID);
        jobjectArray array = (*env)->NewObjectArray(env, 5, pVoid, object);

        (*env)->DeleteLocalRef(env,object);
        (*env)->DeleteLocalRef(env,array);
    }
}

jstring global_str;

JNIEXPORT void JNICALL
Java_com_example_applib1_JniTest_createGlobalRef(JNIEnv *env, jobject instance) {
    jstring  jstring1 = (*env)->NewStringUTF(env,"哈喽，我是全局变量");
    global_str = (*env)->NewGlobalRef(env,jstring1);
    //

}

JNIEXPORT jstring JNICALL
Java_com_example_applib1_JniTest_getGlobalRef(JNIEnv *env, jobject instance) {
    return global_str;
}

JNIEXPORT void JNICALL
Java_com_example_applib1_JniTest_deleteGlobalRef(JNIEnv *env, jobject instance) {
    (*env)->DeleteGlobalRef(env,global_str);
//    (*env)->DeleteGlobalRef(env,global_str);//连续删除会报错
    global_str = NULL;
}

//java无法直接捕捉native 异常，c可以检测是否出现异常，然后使用ThrowNew抛出一个java异常给到java层处理
JNIEXPORT void JNICALL
Java_com_example_applib1_JniTest_exception(JNIEnv *env, jobject instance) {
    jclass pVoid = (*env)->GetObjectClass(env, instance);
    jfieldID pID = (*env)->GetFieldID(env, pVoid, "key2", "Ljava/lang/String;");//这个属性id不存在
    //检测是否出现异常
    jthrowable occurred = (*env)->ExceptionOccurred(env);
    if(occurred!=NULL){
        
        //非常重要：为了让java代码正常运行，清空错误信息
        (*env)->ExceptionClear(env);
        pID = (*env)->GetFieldID(env,pVoid,"key","Ljava/lang/String;");
    }
    jstring field = (*env)->GetObjectField(env, instance, pID);
//    if(strcmp(field,"super xxx")!= 0){ //这句报错，jni的类型jstring不能直接与c语言类型做比较
    const char *string = (*env)->GetStringUTFChars(env, field, NULL);
    //若不满足条件，抛出异常
    if(strcmp(string,"super xxx")!= 0){
        (*env)->ReleaseStringChars(env,field,string);
//        抛出异常1.拿到java的异常jclass 2.使用ThrowNew
        jclass aClass = (*env)->FindClass(env, "java/lang/IllegalArgumentException");
        (*env)->ThrowNew(env,aClass,"key's value is invalid!");
    }

}
jint count = 0;

JNIEXPORT jint JNICALL
Java_com_example_applib1_JniTest_cached(JNIEnv *env, jobject instance) {
    static jfieldID  jfieldID1 = NULL;//静态的，其实相对于函数是长期保存值的
    if(jfieldID1 == NULL){
        jclass pVoid = (*env)->GetObjectClass(env, instance);
        jfieldID1 = (*env)->GetFieldID(env,pVoid,"key","Ljava/lang/String;");
        count++;
    }
    return count;
}

char password[] = "mynameisjason888";
JNIEXPORT void JNICALL
Java_com_example_applib1_JniTest_ndkfilecrypt_1crypt(JNIEnv *env, jobject instance, jstring in_,
                                                     jstring out_) {
//    LOGD("Java_com_example_applib1_JniTest_ndkfilecrypt_1crypt");
    const char *string = (*env)->GetStringUTFChars(env, in_, NULL);
    const char *chars = (*env)->GetStringUTFChars(env, out_, NULL);
    FILE *fin = fopen(string, "rb+");
    FILE *fout = fopen(chars,"wb+");

    int ch;
    int i=0;
    int pwd_len = strlen(password);

    while((ch = fgetc(fin)!=EOF)){
        fputc(ch^password[i % pwd_len],fout);
        i++;
    }
    fclose(fin);
    fclose(fout);
    (*env)->ReleaseStringChars(env,in_,string);
    (*env)->ReleaseStringChars(env,out_,chars);

}

JNIEXPORT void JNICALL
Java_com_example_applib1_JniTest_ndkfilecrypt_1decrypt(JNIEnv *env, jobject instance, jstring in_,
                                                       jstring out_) {
    const char *string = (*env)->GetStringUTFChars(env, in_, NULL);
    const char *chars = (*env)->GetStringUTFChars(env, out_, NULL);
    FILE *fin = fopen(string, "rb+");
    FILE *fout = fopen(chars,"wb+");

    int ch;
    int i=0;
    int pwd_len = strlen(password);
    while((ch = fgetc(fin)!=EOF)){
        fputc(ch ^ password[i%pwd_len],fout);
        i++;
    }
    fclose(fin);
    fclose(fout);


    (*env)->ReleaseStringChars(env,in_,string);
    (*env)->ReleaseStringChars(env,out_,chars);
}

JNIEXPORT void JNICALL
Java_com_example_applib1_JniTest_logFFmpegConfig(JNIEnv *env, jobject instance, jstring url_) {
    LOGE("Java_com_example_applib1_JniFFmpegConfig_logFFmpe gCon  fig");
    const char *string = (*env)->GetStringUTFChars(env, url_,NULL);


    LOGE("url：%s",string);
    av_register_all();
    AVCodec *c_temp = av_codec_next(NULL);
    while(c_temp!=NULL){
        switch (c_temp->type){
            case AVMEDIA_TYPE_VIDEO:
                LOGE("[VIDEO ]：%s",c_temp->name);
                break;
            case AVMEDIA_TYPE_AUDIO:
                LOGE("[AUDIO]：%s",c_temp->name);
                break;
            default:
                LOGE("[Other]：%s",c_temp->name);
                break;
        }
        c_temp = c_temp->next;

    }
    (*env)->ReleaseStringChars(env,url_,string);
}