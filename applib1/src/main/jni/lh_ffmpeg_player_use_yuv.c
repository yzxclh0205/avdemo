#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <android/log.h>
#include <android/native_window_jni.h>
#include <android/native_window.h>
#include <libavutil/imgutils.h>
#include <libswresample/swresample.h>
#include "lh-jni.h"
#include "libyuv.h"

//封装格式
#include "libavformat/avformat.h"
//解码
#include "libavcodec/avcodec.h"
//缩放
#include "libswscale/swscale.h"
#define MAX_AUDIO_FRME_SIZE 48000 * 4
JNIEXPORT void JNICALL
Java_com_example_applib1_JniTest_render(JNIEnv *env, jobject instance, jstring uri_,
                                        jobject surface) {

    //1.jni路径转c ，对应有释放处理
    //2.1  解码套路：1.注册组件 2.打开输入的文件 3.读取文件信息 3.查找解码器 3.打开解码器
    //2.2           4.while 读取一帧帧的数据 5.解码 ，释放编码压缩数据，释放解码压缩数据
    //2.3           6.关闭解码器 7.关闭输入文件 8.若有缓存uint_8 或者解码AVFream，也要释放
    //3.调用SurfaceView处理套路：1.window对象 2.设置配置，宽高 等 3. 播放：锁住 ，处理数据到播放缓存 3.解锁   4.释放window对象

    //1.的处理
    const char *url = (*env)->GetStringUTFChars(env, uri_,NULL );
    //2.的处理

    uint8_t *out_buff;//解码缓存 buff
    struct SwsContext *swsContext;//转换格式用

    av_register_all();
    AVFormatContext *pFormatCtx = avformat_alloc_context();//这里报错出问题 ：没有将封装格式上下文结构体初始化
    if (avformat_open_input(&pFormatCtx, url, NULL, NULL) != 0) {//@return 0 on success, a negative AVERROR on failure.
        LOGE("打开文件失败");
        return;
    }
    if (avformat_find_stream_info(pFormatCtx, NULL) < 0) {//return >=0 if OK, AVERROR_xxx on error
        LOGE("读取信息失败");
        return;
    }
    //找到视频索引
    int video_index = -1;
    for (int i = 0; i < pFormatCtx->nb_streams; i++) {
        if (pFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO) {
            video_index = i;
            break;
        }
    }
    if (video_index == -1) {
        LOGE("未找到视频流");
        return;
    } else {
        LOGE("解码器索引：%d", video_index);
    }

    AVCodecContext *pCodexCtx;//编码器上下文结构体
    pCodexCtx = pFormatCtx->streams[video_index]->codec;
    AVCodec *pCodec;//具体的编码器结构体
    pCodec = avcodec_find_decoder(pCodexCtx->codec_id);
    if (pCodec == NULL) {
        LOGE("未找到解码器");
        return;
    } else {
        LOGE("解码器名称：%s", pCodec->name);
    }
    //打开解码器
    if (avcodec_open2(pCodexCtx, pCodec, NULL) <
        0) {//@return zero on success, a negative value on error
        LOGE("打开解码器失败");
        return;
    }

    AVPacket *pkt;
    //读取解码一帧数据，先初始化pkt，frame
    pkt = (AVPacket *) av_malloc(sizeof(AVPacket));
    av_init_packet(pkt);

    AVFrame *av_frame;//解码出来的像素数据YUV格式
    av_frame = av_frame_alloc();//解码出来的yuv帧数据

    //处理视频的宽高

    //视频显示Window处理. 1.获取 2.拿宽高，处理宽高 3.设置宽高  。播放：锁，设置数据，解锁
    ANativeWindow *nativeWindow = ANativeWindow_fromSurface(env, surface);
    ANativeWindow_Buffer window_buffer;//结构
    int nativeWidth = ANativeWindow_getWidth(nativeWindow);
    int nativeHeight = ANativeWindow_getHeight(nativeWindow);
    //方式1.设置尺寸 配合数据位移可以做到原画大小播放
//    ANativeWindow_setBuffersGeometry(nativeWin dow,nativeWidth,nativeHeight,WINDOW_FORMAT_RGBA_8888);

    //从解码器中拿到宽高

    //方式2：自适应播放，处理视频宽度可能超过 屏幕宽度问题。 高度 则等比说缩放
    ANativeWindow_setBuffersGeometry(nativeWindow, pCodexCtx->width, pCodexCtx->width * nativeHeight / nativeWidth ,
                                     WINDOW_FORMAT_RGBA_8888);//// 重新计算绘制区域的高度，防止纵向变形

    int videoWidth = nativeWidth > pCodexCtx->width ? pCodexCtx->width : nativeWidth;//拿较小宽度
//    videoHeight = videoHeight * videoWidth/ nativeWidth;//等比 计算得到高度---------------------这里不对，要显示视频，取视频的宽高比
    int videoHeight = videoWidth * pCodexCtx->height / pCodexCtx->width;
    if (videoHeight > nativeHeight) {//若此时高度大于 屏幕高度，缩放高度。等比计算得到宽度
        videoHeight = nativeHeight;
        videoWidth = videoHeight * pCodexCtx->width / pCodexCtx->height;//这里其实就没有再考虑 宽度放不下了
    }


    AVFrame *rgb_frame;//转换后的rgb格式解码像素数据
    rgb_frame = av_frame_alloc();//转换后rgb帧的数据
    out_buff = av_malloc(avpicture_get_size(AV_PIX_FMT_RGBA, videoWidth, videoHeight));//计算rgb缓冲区大小
    avpicture_fill((AVPicture *)rgb_frame,out_buff,AV_PIX_FMT_RGBA,videoWidth,videoHeight);//填充rgb的data缓冲区
    //转换器。视频图像转换
    swsContext = sws_getContext(pCodexCtx->width,pCodexCtx->height,pCodexCtx->pix_fmt,videoWidth,videoHeight,AV_PIX_FMT_RGBA,SWS_BICUBIC,NULL,NULL,NULL);
    // 读取的帧数量
    int frame_count = 0;
    // 是否解封装完成
    int got_frame;
    // 循环读取每一帧的数据
    int ret;
    while (av_read_frame(pFormatCtx, pkt) >= 0) {
        // < 0 代表读到文件末尾了
        if (pkt->stream_index == video_index) {
            // 解封装每一帧的视频数据
            avcodec_decode_video2(pCodexCtx, av_frame, &got_frame, pkt);
            if(ret < 0){
                LOGI("%s","解码完成");
            }
            if (got_frame > 0) {
                // 开始绘制，锁定不让其它的线程绘制
                ANativeWindow_lock(nativeWindow, &window_buffer, NULL);
                // 解码数据 -> rgba
                sws_scale(swsContext, (const uint8_t *const *) av_frame->data, av_frame->linesize, 0,
                          av_frame->height, rgb_frame->data, rgb_frame->linesize);
                // 缓冲区的地址
                uint8_t *dst = (uint8_t *) window_buffer.bits;
                // 每行的内存大小
                int dstStride = window_buffer.stride * 4;
                // 像素区的地址
                uint8_t *src = rgb_frame->data[0];
                int srcStride = rgb_frame->linesize[0];
                for (int i = 0; i < videoHeight; i++) {
                    // 原画大小播放
                    // 逐行拷贝内存数据，但要进行偏移，否则视频会拉伸变形
                    // (i + (windowHeight - videoHeight) / 2) * dstStride 纵向偏移，确保视频纵向居中播放
                    // (dstStride - srcStride) / 2 横向偏移，确保视频横向居中播放
                    memcpy(dst + (i + (nativeHeight - videoHeight) / 2) * dstStride +
                           (dstStride - srcStride) / 2, src + i * srcStride,
                           srcStride);

                }
                ANativeWindow_unlockAndPost(nativeWindow);
                usleep(4 * 1000);
                LOGE("解码第%d帧", frame_count++);
            }
            av_free_packet(pkt);
        }
    }
    ANativeWindow_release(nativeWindow);
    free(out_buff);
    av_frame_free(&av_frame);
    av_frame_free(&rgb_frame);
    sws_freeContext(swsContext);

//    avcodec_close(pCodexCtx);
//    avformat_free_context(pFormatCtx);
    avcodec_close(pCodexCtx);
    avformat_close_input(&pFormatCtx);

    (*env)->ReleaseStringUTFChars(env, uri_, url);
}


JNIEXPORT void JNICALL
Java_com_example_applib1_JniTest_sound(JNIEnv *env, jobject instance, jstring input_,
                                       jstring output_) {
    const char *input = (*env)->GetStringUTFChars(env, input_, 0);
    const char *output = (*env)->GetStringUTFChars(env, output_, 0);
    // 套路：格式转换后存入文件
    // 1.套路打开解码器 2.获取输入文件的采样格式、采样率，声道布局。 设置输出文件的采样格式（同输入），采样率（同输入）、声道布局（立体声）
    // 2.音频格式转换结构体。 套路：1.分配内容空间 2.设置参数 3.初始化 4.使用
    // 3.业务处理：读取每一帧音频文件 2.音频格式转换 3.拿到转换后的buff数据大小 4.写入到文件 5.调用java的AudioTrack的play的write方法
    // 4.释放资源：封装格式、编码器、音频转换器、解码数据、buff
    av_register_all();
    AVFormatContext *pFormatCtx = avformat_alloc_context();
    if(avformat_open_input(&pFormatCtx,input,NULL,NULL)<0){
        LOGD("打开文件失败");
        return;
    }
    if(avformat_find_stream_info(pFormatCtx,NULL)<0){
        LOGD("获取输入文件信息失败");
        return;
    }
    int audio_index = -1;
    for(int i=0;i<pFormatCtx->nb_streams;i++){
        if(pFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO){
            audio_index = i;
            break;
        }
    }
    AVCodecContext *pCodecCtx = pFormatCtx->streams[audio_index]->codec;
    //查找解码器
    AVCodec *pCodec = avcodec_find_decoder(pCodecCtx->codec_id);
    if(pCodec == NULL){
        LOGD("未找到解码器");
        return;
    }
    //打开解码器
    if(avcodec_open2(pCodecCtx,pCodec,NULL)<0){
        LOGE("打开解码器失败");
        return;
    }
    //音频格式、采样率、声道布局获取-------------是从解码器获取

    enum AVSampleFormat in_format = pCodecCtx->sample_fmt;//采样格式
    int in_rate = pCodecCtx->sample_rate;//采样率
    uint64_t in_layout = pCodecCtx->channel_layout;//拿声道布局

    enum AVSampleFormat out_format = AV_SAMPLE_FMT_S16;//采样格式
    int out_rate = in_rate;
    uint64_t out_layout = AV_CH_LAYOUT_STEREO;//立体声

    int nb_channels = av_get_channel_layout_nb_channels(out_layout);//声道个数

    //音频格式结构体
    struct SwsContext *swrContext = swr_alloc();
    swr_alloc_set_opts(swrContext,out_layout,out_format,out_rate,in_layout,in_format,in_rate,0,NULL);
    swr_init(swrContext);

    //音频转格式后的缓存buff
    uint8_t  *out_buff = (uint8_t *) av_malloc(MAX_AUDIO_FRME_SIZE);

    //获取java端对象，jclass、jmethodid、调用方法得到jobject
    // 获取play方法并执行、获取write方法、组织两个参数（byte数组 jni创建，然后获取到c的指针引用，处理数据，同步数据）。 调用write方法
    jclass jclazz = (*env)->GetObjectClass(env, instance);
    jmethodID getInstanceId = (*env)->GetMethodID(env, jclazz, "getAudioTrack", "(II)Landroid/media/AudioTrack;");
    jobject audioTrack = (*env)->CallObjectMethod(env, instance, getInstanceId, out_rate, nb_channels);//传递采样率 和声道个数。 int类型的字节长度 与jint相同

    jclass audio_class = (*env)->GetObjectClass(env, audioTrack);
    jmethodID playId = (*env)->GetMethodID(env, audio_class, "play", "()V");
    //播放
    (*env)->CallVoidMethod(env,audioTrack,playId);

    jmethodID audio_track_write = (*env)->GetMethodID(env, audio_class, "write", "([BII)I");




    AVPacket *avPacket = av_malloc(sizeof(AVPacket));
    av_init_packet(avPacket);
    AVFrame *avFrame = av_frame_alloc();

    FILE *out_fp = fopen(output,"wb+");
    int got_frame;
    int count = 0;
    //读取每一帧
    while(av_read_frame(pFormatCtx,avPacket)>=0){
        //是否是音频
        if(avPacket->stream_index == audio_index){
            //解码一帧
            int ret = avcodec_decode_audio4(pCodecCtx, avFrame, &got_frame, avPacket);
            if(ret<0){
                LOGE("%d 解码 完毕",count);
            }
            //解码成功后处理数据
            if(got_frame>0){
                LOGE("%d 处理解码数据",count++);
                //音频格式转换
                swr_convert(swrContext,&out_buff,MAX_AUDIO_FRME_SIZE,avFrame->data,avFrame->nb_samples);//最后这个参数是 一帧包含的音频个数
                int out_buff_size = av_samples_get_buffer_size(NULL,nb_channels,avFrame->nb_samples,out_format,1);//数据大小
                //写入文件
                fwrite(out_buff,1,out_buff_size,out_fp);

                //调用java端的代码播放音乐
//                最终要调用的代码是 
//                (*env)->CallObjectMethod(env,audioTrack,audio_track_write,字节数据，位偏移，写入长度)
                //处理jni的字节数组 数据
                jbyteArray jbyteArray1 = (*env)->NewByteArray(env, out_buff_size);//jni
                jbyte *sample_bytes = (*env)->GetByteArrayElements(env, jbyteArray1, NULL);//拿到jbyte的指针（一维数组）。jbyte和c的byte字节长度一样，可以操作
//                (*env)->SetByteArrayRegion();//这个方式是把jbyte数组的值赋值到jbyteArray上
                memcpy(sample_bytes,out_buff,out_buff_size);
                //释放内存：同步数据
                (*env)->ReleaseByteArrayElements(env,jbyteArray1,sample_bytes,0);

                //写数据
                (*env)->CallIntMethod(env,audioTrack,audio_track_write,jbyteArray1,0,out_buff_size);
                (*env)->DeleteLocalRef(env,jbyteArray1);
                usleep(1000 * 16);
            }
            av_free_packet(avPacket);
        }
    }

    av_frame_free(&avFrame);
    av_free(out_buff);
    swr_free(&swrContext);
    avcodec_close(pCodecCtx);
    avformat_close_input(&pFormatCtx);

    (*env)->ReleaseStringUTFChars(env, input_, input);
    (*env)->ReleaseStringUTFChars(env, output_, output);
}