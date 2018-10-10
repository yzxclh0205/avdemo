#include <jni.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <lh-jni.h>
#include <libavformat/avformat.h>
#include <android/native_window.h>
#include <unistd.h>
#include <android/native_window_jni.h>
#include <lh_push_queue.h>
#include <x264.h>
#include <faac.h>
#include <rtmp.h>
#include <pthread.h>


#define CONNECT_FAILED 101
#define INIT_FAILED 102
x264_picture_t pic_in;
x264_picture_t pic_out;
x264_t *vido_encode_handle;
int y_len;
int u_len;
int v_len;


unsigned long nInputSamples;//输入的采样率个数
unsigned long nMaxOutputBytes;//编码输出之后的字节数
faacEncHandle audio_encode_handle;

jobject jobj_push_native;
jclass jcls_push_native;
jmethodID jmid_throw_native_error;
//流媒体地址
char* rtmp_path;

pthread_mutex_t mutex;
pthread_cond_t cond;
int is_pushing = FALSE;

JavaVM* javaVM;
int start_time = 0;
/**
 * 获取JavaVM
 */
jint JNI_OnLoad(JavaVM* vm,void* reserved){
    javaVM = vm;
    return JNI_VERSION_1_4;
}

/**
 * 向java层发送错误信息
 */
void throwNativeError(JNIEnv* env,int code){
    (*env)->CallVoidMethod(env,jobj_push_native,jmid_throw_native_error,code);
}

void lh_push_close(RTMP* rtmp){
    LOGE("释放资源");
    free(rtmp_path);
    RTMP_Close(rtmp);
    RTMP_Free(rtmp);
    (*javaVM)->DetachCurrentThread(javaVM);
}
/**
 * 加入RTMPPacket队列，等待线程发送
 * 这里没有数量限制，所以只是控制一个拿锁，其他等待，而不是死循环再次等待限额空闲出来
 */
void add_rtmp_packet(RTMPPacket *packet){
    pthread_mutex_lock(&mutex);
    if(is_pushing){
        queue_insert_last(packet);
    }
    pthread_cond_signal(&cond);
    pthread_mutex_unlock(&mutex);
}

/**
 * 添加aac头信息
 */
void add_aac_sequence_header() {
    //获取aac头信息的长度
    unsigned char* buf;
    unsigned long len;//长度
    faacEncGetDecoderSpecificInfo(audio_encode_handle,&buf,&len);
    int body_size = 2 + len;
    RTMPPacket *packet = malloc(sizeof(RTMPPacket));
    //RTMPPacket初始化
    RTMPPacket_Alloc(packet,body_size);
    RTMPPacket_Reset(packet);
    unsigned char *body = packet->m_body;
    //头信息配置
    /*AF 00 + AAC RAW data*/
    body[0] = 0xAF;//10 5 SoundFormat(4bits):10=AAC,SoundRate(2bits):3=44kHz,SoundSize(1bit):1=16-bit samples,SoundType(1bit):1=Stereo sound
    body[1] = 0x00;//AACPacketType:0表示AAC sequence header
    memcpy(&body[2],buf,len);
    packet->m_packetType = RTMP_PACKET_TYPE_AUDIO;
    packet->m_nBodySize = body_size;
    packet->m_nChannel = 0x04;
    packet->m_hasAbsTimestamp = 0;
    packet->m_nTimeStamp = 0;
    packet->m_headerType = RTMP_PACKET_SIZE_MEDIUM;
    add_rtmp_packet(packet);
    free(buf);
}

/**
 * 从队列中不断拉取RTMPPacket发送给流媒体服务器
 */
void *push_thread(void* arg){
    //在线程中 抛异常 需要java环境
    JNIEnv* env;//获取当前线程的JNIEnv
    (*javaVM)->AttachCurrentThread(javaVM,&env,NULL);

    //RTMP连接：1.分配RTMP内存空间 2.初始化 3.设置连接超时时间 4.设置流媒体地址 5.发布rtmp数据流
    //6.建立连接 失败抛异常，释放资源
    //7.成功。开始时间赋值
    //8.连接流 ，失败抛异常，释放资源
    //9.成功，开始推送。标志位赋值 ,发送AAc头信息
    //11.死循环开始发送数据包给流媒体服务器
        //1.拿数据逻辑：线程锁锁，条件变量开始等待唤醒，取队列数据，队列数据不为空，删除队列中该数据，
        //2.设置packet(RTMPPackage包的streamid 即m_nInfoField2)
        //3.发送数据包。true 是否放入librtmp队列中，并不是立即发送 断开则释放包，释放锁，释放资源。
            // 成功，释放包，释放锁，继续循环

    //RTMP连接：1.分配RTMP内存空间 2.初始化 3.设置连接超时时间 4.设置流媒体地址 5.发布rtmp数据流
    RTMP *rtmp = RTMP_Alloc();
    RTMP_Init(rtmp);
    rtmp->Link.timeout = 5;//连接的超时时间
    RTMP_SetupURL(rtmp,rtmp_path);
    RTMP_EnableWrite(rtmp);
    //6.建立连接 失败抛异常，释放资源7.成功。开始时间赋值
    // 8.连接流 ，失败抛异常，释放资源 9.成功，开始推送。标志位赋值
    if(!RTMP_Connect(rtmp,NULL)){
        LOGE("RTMP连接失败");
        throwNativeError(env,CONNECT_FAILED);
        lh_push_close(rtmp);
        return 0;
    }
    start_time = RTMP_GetTime();
    if(!RTMP_ConnectStream(rtmp,0)){//连接流
        LOGE("RTMP连接流失败");
        throwNativeError(env,CONNECT_FAILED);
        lh_push_close(rtmp);
        return 0;
    }
    is_pushing = TRUE;
    add_aac_sequence_header();
    //11.死循环开始发送数据包给流媒体服务器
    //1.拿数据逻辑：线程锁锁，条件变量开始等待唤醒，取队列数据，队列数据不为空，删除队列中该数据，
    //2.设置packet(RTMPPackage包的streamid 即m_nInfoField2)
    //3.发送数据包。true 是否放入librtmp队列中，并不是立即发送 断开则释放包，释放锁，释放资源。
    // 成功，释放包，释放锁，继续循环
    //
    while(is_pushing){
        pthread_mutex_lock(&mutex);
        pthread_cond_wait(&cond,&mutex);
        //取出队列中的RTMPPacket
        RTMPPacket *packet = queue_get_first();
        if(packet){
            queue_delete_first();
            packet->m_nInfoField2 = rtmp->m_stream_id;//RTMP协议，stream_id数据
            //发送数据
            int result = RTMP_SendPacket(rtmp, packet, TRUE);//TRUE放入librtmp队列中，并不是立即发送
            if(!result){
                LOGE("RTMP断开");
                RTMPPacket_Free(packet);
                pthread_mutex_unlock(&mutex);
                lh_push_close(rtmp);
                return 0;
            } else{
                LOGE("rtmp send packet");
                RTMPPacket_Free(packet);
            }
        }
        pthread_mutex_unlock(&mutex);

    }
    lh_push_close(rtmp);
    return  0;
}

/**
 * 设置视频参数
 */
JNIEXPORT void JNICALL
Java_com_example_applib1_pushstream_PushNative_setVideoOptions(JNIEnv *env, jobject instance,
                                                               jint width, jint height,
                                                               jint bitrate, jint fps) {
    LOGE("Java_com_example_applib1_pushstream_PushNative_setVideoOptions");
    x264_param_t param;
//    x264_param_default_preset()设置
    x264_param_default_preset(&param, "ultrafast", "zerolatency");
    //设置编码的像素格式、宽、高。  y 、u 、v 长度
    param.i_csp = X264_CSP_I420;//编码输入的像素格式YUV420P;
    param.i_width = width;
    param.i_height = height;

    y_len = width * height;
    u_len = y_len / 4;
    v_len = u_len;

    //设置码率：码率策略、码率，最大码率
    //参数i_rc_method表示控制码率：CQP(恒定质量)，CRF（恒定码率），ABR（平均码率）
    //恒定码率，会尽量控制在固定码率
    param.rc.i_rc_method = X264_RC_CRF;
    param.rc.i_bitrate = bitrate / 1000;//码率 （比特率，单位Kbps）
    param.rc.i_vbv_max_bitrate = bitrate / 1000 * 1.2;//瞬时最大码率

    //码率控制不通过timebase 和timestamp，而是fps
    param.b_vfr_input = 0;
    param.i_fps_num = fps;//帧率分子
    param.i_fps_den = 1;//帧率分母
    param.i_timebase_num = param.i_fps_num;
    param.i_timebase_den = param.i_fps_den;

    //默认是多线程0.
    param.i_threads = 1;//并行的线程数量

    //是否把SPS和PPS放入每一个关键帧.序列参数集，图像参数集
    //为了提高图像的纠错能力
    param.b_repeat_headers = 1;
    //设置level级别
    param.i_level_idc = 51;
    //设置Profile档次
    //baseline级别，没有B帧
    x264_param_apply_profile(&param, "baseline");

    //x264_picture_t (输入图像)初始化
    x264_picture_alloc(&pic_in, param.i_csp, param.i_width, param.i_height);
    //开始的显示时间是0
    pic_in.i_pts = 0;
    //打开编码器
    vido_encode_handle = x264_encoder_open(&param);
    if (vido_encode_handle) {
        LOGE("打开视频编码器成功");
    } else {
        throwNativeError(env,INIT_FAILED);
    }
}



/**
 * 发送h264 sps和pps参数集
 */
void add_264_sequence_header(unsigned char* pps, unsigned char* sps, int pps_len, int sps_len) {
    int body_size = 16 + pps_len + sps_len;//按照H264标准配置SPS和PPS，共使用16个字节
    //RTMP的使用， 分配内存空间，初始化，重置，设置m_body参数（16字节+sps和pps数据）
    // ，设置m_packetType,m_nBodySize,设置m_nTimeStamp,m_hasAbsTimeStamp,m_nChannel,m_headerTpe
    RTMPPacket *packet = malloc(sizeof(RTMPPacket));
    RTMPPacket_Alloc(packet,body_size);
    RTMPPacket_Reset(packet);

//    char *string = packet->m_body;
    unsigned char* body = packet->m_body;
    int i=0;
    //添加H264 header信息解析
    //二进制表示：0001 0111
    body[i++] = 0x17;//VideoHeaderTag:FrameType帧类型：（1=key frame）+CodecId(7=AVC)
//    VideoTagHeader之后跟着的就是VIDEODATA数据了，也就是video payload。如果视频的格式是AVC（H.264）的话，VideoTagHeader会多出4个字节的信息，全部置为0。
    body[i++] = 0x00;//AVCPacketType = 0表示设置AVCDecoderConfigurationRecord
    //composition time 0x000000 24bit ?
    body[i++] = 0x00;
    body[i++] = 0x00;
    body[i++] = 0x00;

    //AVCDecoderConfigurationRecord（AVCPacketType == 0，FrameType==1）
//    1.configurationVersion，8bit
//    2.AVCProfileIndication，8bit
//    3.profile_compatibility，8bit
//    4.AVCLevelIndication，8bit
//    5.lengthSizeMinusOne，8bit
    body[i++] = 0x01;//configurationVersion，版本为1
    body[i++] = sps[1];//AVCProfileIndication
    body[i++] = sps[2];//profile_compatibility
    body[i++] = sps[3];//configurationVersion

    //?
    body[i++] = 0xFF;//lengthSizeMinusOne,H264 视频中 NALU的长度，计算方法是 1 + (lengthSizeMinusOne & 3),实际测试时发现总为FF，计算结果为4.

    //sps
    body[i++] = 0xE1;//numOfSequenceParameterSets:SPS的个数，计算方法是 numOfSequenceParameterSets & 0x1F,实际测试时发现总为E1，计算结果为1.
    body[i++] = (sps_len >> 8 )& 0xff;//sequenceParameterSetLength:SPS的长度
    body[i++] = (sps_len)& 0xff;//sequenceParameterSetNALUnits
    memcpy(&body[i],sps,sps_len);
    i +=sps_len;

    //pps
    body[i++] = 0x01;//numOfPictureParameterSets:PPS 的个数,计算方法是 numOfPictureParameterSets & 0x1F,实际测试时发现总为E1，计算结果为1.
    body[i++] = (pps_len >> 8) & 0xff;//pictureParameterSetLength:PPS的长度
    body[i++] = (pps_len) & 0xff;//PPS
    memcpy(&body[i],pps,pps_len);
    i +=pps_len;

    //处理RTMP的类型等参数
    //Message Type ,RTMP_PACKET_TYPE_VIDEO:0x09
    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;//一个字节
    //Payload length
    packet->m_nBodySize = body_size;//三个字节表示tag中数据段的大小
    //Time Stamp:4个字节：记录了每一个tag相对于第一个tag（File Header）的相对时间，以毫秒为单位。而File Header的time stamp永远为0
    packet->m_nTimeStamp = 0;
    packet->m_hasAbsTimestamp = 0;//StreamId三个字节
    packet->m_nChannel = 0x04;//Channel ID,Audio和Vedio通道
    packet->m_headerType = RTMP_PACKET_SIZE_MEDIUM;
    //将RTMPPacket加入队列
    add_rtmp_packet(packet);
}



/**
 * 发送h264帧信息
 */
void add_264_body(unsigned char *buf, int len){
    //去掉起始码（界定符）
    if(buf[2] == 0x00){// 00 00 00 01
        buf +=4;
        len -=4;
    }
    else if(buf[2] == 0x01){
        buf +=3;
        len -=3;
    }
    int body_size = len +9;//额外添加9个参数
    //RTMP的处理，分配内存，初始化

    RTMPPacket *packet = malloc(sizeof(RTMPPacket));
    RTMPPacket_Alloc(packet,body_size);

    unsigned char * body = packet->m_body;
    //当NAL头信息中type（5位）等于5，说明这是关键帧NAL单元
    //buf[0] NAL Header与运算，获取type，根据type判断关键帧和普通帧
    //00000101 & 00011111(0x1f) = 000000101
    int type = buf[0] & 0x1f;
    //inter frame 帧间压缩
    body[0] = 0x27;// VideoHeaderTag;FrameType(2 =inter frame) + CodecId(7=AVC)
    //IDR I帧图像 关键帧
    if(type == NAL_SLICE_IDR){
        body[0] = 0x17;//VideoHeaderTag:FrameType帧类型：（1=key frame）+CodecId(7=AVC)
    }
    //AVCPacketType = 1;
    body[1] = 0x01;/*nal unit,NALUs（AVCPacketType == 1)*/
    body[2] = 0x00; //composition time 0x000000 24bit
    body[3] = 0x00;
    body[4] = 0x00;

    //写入NALU信息，右移8位，一个字节的读取？
    body[5] = (len >> 24) & 0xff;
    body[6] = (len >> 16) & 0xff;
    body[7] = (len >> 8) & 0xff;
    body[8] = (len) & 0xff;

    //复制数据
    memcpy(&body[9],buf,len);

    //Message Type ,RTMP_PACKET_TYPE_VIDEO:0x09
    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;//一个字节
    //Payload length
    packet->m_nBodySize = body_size;//三个字节表示tag中数据段的大小
    //Time Stamp:4个字节：记录了每一个tag相对于第一个tag（File Header）的相对时间，以毫秒为单位。而File Header的time stamp永远为0
    packet->m_nTimeStamp = RTMP_GetTime() - start_time;////记录了每一个tag相对于第一个tag（File Header）的相对时间
    packet->m_hasAbsTimestamp = 0;//StreamId三个字节
    packet->m_nChannel = 0x04;//Channel ID,Audio和Vedio通道
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;

    add_rtmp_packet(packet);
}
/**
 * 将采集的视频数据进行编码
 */
JNIEXPORT void JNICALL
Java_com_example_applib1_pushstream_PushNative_fireVideoData(JNIEnv *env, jobject instance,
                                                             jbyteArray data_) {
    //视频数据NV21 ->YUV420p  .即格式YYYYYYYYVUVU 转成YYYYYYYYUUVV
    jbyte *nv21_buffer = (*env)->GetByteArrayElements(env, data_, NULL);//jni类型数据jbytearray获取指针类型数据
    jbyte *u = pic_in.img.plane[1];//将uint8_t类型的数据 用jbyte 字节类型指针引用
    jbyte *v = pic_in.img.plane[2];
    //nv21 4:2:0 Formats, 12 Bits per Pixel
    //nv21与yuv420p，y个数一致，uv位置对调
    //nv21转yuv420p  y = w*h,u/v=w*h/4
    //nv21 = yvu yuv420p=yuv y=y u=y+1+1 v=y+1
    memcpy(pic_in.img.plane[0], nv21_buffer, y_len);
    for (int i = 0; i < u_len; i++) {
        *(u + i) = *(nv21_buffer + y_len + i * 2 + 1);
        *(v + i) = *(nv21_buffer + y_len + i * 2);
    }
    //使用x264编码得到NALU单元
    x264_nal_t *nal = NULL;
    int n_nal = -1;//NALU的个数
    //开始编码
    if(x264_encoder_encode(vido_encode_handle,&nal,&n_nal,&pic_in,&pic_out)<0){
        LOGE("编码失败");
        return;
    }
    //使用rtmp协议将h264编码的视频数据发送给流媒体服务器
    //帧分为关键帧和普通帧，为了提供画面的纠错率，关键帧应包含sps和pps数据
    int sps_len,pps_len;
    unsigned char sps[100];
    unsigned char pps[100];
    memset(sps,0,100);
    memset(pps,0,100);
    //处理一帧数据 则pst+1
    pic_in.i_pts +=1;//顺序累加
    //遍历NALU数组，根据NALU的类型进行判断
    for(int i=0;i<n_nal;i++){
        if(nal[i].i_type == NAL_SPS){
            //复制sps数据
            sps_len = nal[i].i_payload - 4;
            memcpy(sps,nal[i].p_payload,sps_len);
        }
        else if(nal[i].i_type == NAL_PPS){
            //复制pps数据
            pps_len = nal[i].i_payload - 4;
            memcpy(pps,nal[i].p_payload,pps_len);

            //因为NALU单元 顺序是标志符 sps pload ；标志符 pps pload
            //发送序列
            //h264关键帧会包含sps和pps数据
            add_264_sequence_header(pps,sps,pps_len,sps_len);
        }
        else{
            //发送普通帧 数据
            add_264_body(nal[i].p_payload,nal[i].i_payload);
        }
    }

    (*env)->ReleaseByteArrayElements(env, data_, nv21_buffer, 0);
}


/**
 * 音频编码器设置
 */
JNIEXPORT void JNICALL
Java_com_example_applib1_pushstream_PushNative_setAudioOptions(JNIEnv *env, jobject instance,
                                                               jint sampleRateInHz, jint channel) {
    audio_encode_handle = faacEncOpen(sampleRateInHz, channel, &nInputSamples, &nMaxOutputBytes);
    if (!audio_encode_handle) {
        LOGE("音频编码器打开失败");
        return;
    }
    //设置音频编码参数
    //1.获取当前的音频参数 2.设置mpeg版本、allowMidside、aacObjectType、outputFormat、useTns、useLfe、quantqual、bandWidth、shortctl
    faacEncConfigurationPtr p_config = faacEncGetCurrentConfiguration(audio_encode_handle);
    p_config->mpegVersion = MPEG4;
    p_config->allowMidside = 1;
    p_config->aacObjectType = LOW;
    p_config->outputFormat = 0;//输出是否包含ADTS头
    p_config->useTns = 1;//时域噪声控制，大概就是消除爆音
    p_config->useLfe = 0;
//    p_config->inputFormat = FAAC_INPUT_16BIT;//输入的格式
    p_config->quantqual = 100;
    p_config->bandWidth = 0;//频宽
    p_config->shortctl = SHORTCTL_NORMAL;//不知道啥意思

    if (!faacEncSetConfiguration(audio_encode_handle, p_config)) {
        LOGE("音频编码器配置失败...");
        throwNativeError(env,INIT_FAILED);
        return;
    }
    LOGE("音频编码器配置成功");


}

/**
 * 添加AAC 到RTMP packet中
 */
void add_aac_body(unsigned char *buf,int len){
    int body_size = 2 + len;
    //RTMPPacket 包套路：1.分配空间 2.初始化 3.重置 4.设置头信息 5.设置type、length、time、streamid信息
    RTMPPacket *packet = malloc(sizeof(RTMPPacket));
    RTMPPacket_Alloc(packet,body_size);
    RTMPPacket_Reset(packet);
    unsigned char *body = packet->m_body;
    //头信息配置
    /*AF 00 + AAC RAW data*/
    body[0] = 0xAF;//10 5 SoundFormat(4bits):10=AAC,SoundRate(2bits):3=44kHz,SoundSize(1bit):1=16-bit samples,SoundType(1bit):1=Stereo sound
    body[1]= 0x01;//AACPacketType:1表示AAC raw
    memcpy(&body[2],buf,len);/*spec_buf是AAC raw数据*/
    packet->m_packetType = RTMP_PACKET_TYPE_AUDIO;//1字节的类型
    packet->m_nBodySize = body_size;//三字节的长度
    packet->m_nChannel = 0x04;
    packet->m_hasAbsTimestamp = 0;
    packet->m_nTimeStamp = 0;
    packet->m_headerType = RTMP_PACKET_SIZE_MEDIUM;
    add_rtmp_packet(packet);
    free(buf);
}

/**
 * 对音频采样数据进行AAC编码
 */
JNIEXPORT void JNICALL
Java_com_example_applib1_pushstream_PushNative_fireAudioData(JNIEnv *env, jobject instance,
                                                             jbyteArray data_, jint len) {
    jbyte *b_buffer = (*env)->GetByteArrayElements(env, data_, NULL);
    int *pcmbuf;//转换后的pcm流数据
    unsigned char* bitbuf;//编码后的数据buf
    //这里是将字节类型数据 。 采样率* int（四个字节）的长度。
    //采样率 ：每秒采样多少次。 这里强转short* 有啥意义？接收类型还是int，这是什么操作
    pcmbuf = (short*)malloc(nInputSamples* sizeof(int));
    bitbuf = (unsigned char*)malloc(nMaxOutputBytes * sizeof(unsigned char));
    int nByteCount = 0;
    unsigned int mBufferSize = (unsigned int)len /2;//这里除以2 是个什么操作,与下面有关，char->short
    unsigned short* buf = (unsigned short*)b_buffer;//将char->short
    while(nByteCount < mBufferSize){
        int audioLength = nInputSamples;//audioLength为调用faacEncOpen得到的采样数
        if((nByteCount + nInputSamples) >=mBufferSize){
            audioLength = mBufferSize - nByteCount;//最后采样率大小的数据长度
        }
        for(int i=0;i<audioLength;i++){
            //每次从实时的pcm音频数据中读出量化数为8的pcm数据
            //把 short类型的数据 一个个赋值给int类型
            int s = ((int16_t *)buf + nByteCount)[i];
            //这个地方没明白-----------------------------------pcmbuf[i]这个 反复的不就被覆盖了么
            //这个地方只处理 一个秒钟的采样数据
            pcmbuf[i] = s << 8;//用8个二进制位表示一个采样量化点（模数转换）这是啥意思？位移8位，不就是short数据在int中间了么
        }
        nByteCount +=nInputSamples;
        //每秒的采样数据 利用faac进行编码
        //pcmbuf为转换后的pcm流数据，audioLength为调用faacEncOpen时得到的输入采样数，bitbuf为编码后的数据buf
        //nMaxOutputBytes为调用faacEncOpen时得到的最大输出字节数
        int bytesLen = faacEncOpen(audio_encode_handle,pcmbuf,audioLength,nMaxOutputBytes);
        if(bytesLen < 1){
            continue;
        }
        //从bitbuf中得到编码后的aac数据流
        add_aac_body(bitbuf,bytesLen);
    }
    if(bitbuf){
        free(bitbuf);
    }
    if(pcmbuf){
        free(pcmbuf);
    }
    (*env)->ReleaseByteArrayElements(env, data_, b_buffer, 0);
}


JNIEXPORT void JNICALL
Java_com_example_applib1_pushstream_PushNative_startPush(JNIEnv *env, jobject instance,
                                                         jstring url_) {

    //拿到java 的PushNative对象和class对象 并设置为全局引用，拿到方法id（抛异常使用）
    //jobj(PushNative对象)
    jobj_push_native = (*env)->NewGlobalRef(env, instance);//全局对象
    jclass jcls_push_native_tmp = (*env)->GetObjectClass(env, instance);
    if(jcls_push_native_tmp == NULL){
        LOGE("jcls_push_native_tmp NULL");
    }else{
        LOGE("jcls_push_native_tmp not NULL");
    }
    jcls_push_native = (*env)->NewGlobalRef(env, jcls_push_native_tmp);

    //PushNative.throwNativeError
    jmid_throw_native_error = (*env)->GetMethodID(env, jcls_push_native_tmp, "throwNativeError", "(I)V");
    
    //处理路径.初始化操作
    const char *url_cstr = (*env)->GetStringUTFChars(env, url_, NULL);
    //复制路径到全局变量中
    rtmp_path = malloc(strlen(url_cstr)+1);
    memset(rtmp_path,0,strlen(url_cstr)+1);
    memcpy(rtmp_path,url_cstr,strlen(url_cstr));

    //互斥锁和条件变量 、创建队列、启动线程
    pthread_mutex_init(&mutex,NULL);
    pthread_cond_init(&cond,NULL);

    //创建队列
    create_queue();
    //启动消费线程（从队列中不断的拉取RTMPPacket发送给流媒体服务器）
    pthread_t push_thread_id;
    pthread_create(&push_thread_id,NULL,push_thread,NULL);

    (*env)->ReleaseStringUTFChars(env, url_, url_cstr);
}

JNIEXPORT void JNICALL
Java_com_example_applib1_pushstream_PushNative_stopPush(JNIEnv *env, jobject instance) {
    is_pushing = FALSE;
}

JNIEXPORT void JNICALL
Java_com_example_applib1_pushstream_PushNative_release(JNIEnv *env, jobject instance) {
    (*env)->DeleteGlobalRef(env,jcls_push_native);
    (*env)->DeleteGlobalRef(env,jobj_push_native);
    (*env)->DeleteGlobalRef(env,jmid_throw_native_error);
}
