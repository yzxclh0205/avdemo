//
// Created by lh on 2018/9/19.
//
#include <jni.h>
#include <libavformat/avformat.h>
#include <libavcodec/avcodec.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <libswresample/swresample.h>
#include <unistd.h>
#include <libavutil/time.h>
#include "queue.h"

#define  MAX_STREAM 2
#define  PACKAGE_QUEUE_SIZE 50
#define  MAX_AUDIO_FRME_SIZE 48000 * 4

#define MIN_SLEEP_TIME_US 1000ll
#define AUDIO_TIME_ADJUST_US -200000ll

typedef struct _Player Player;
typedef struct SwsContext SwsContext;
typedef struct SwrContext SwrContext;
//操作音视频的结构体
struct _Player{
    JavaVM *javaVM;//这里存的是指针
    //封装格式上下文
    AVFormatContext *input_format_ctx;
    //音视频流索引位置
    int video_stream_index;
    int audio_stream_index;
    //流的总个数
    int captrue_streams_no;

    //解码器上下文数组-------------这里是指针数组， 赋值操作是 inputxx[xx]= xx;指针操作
    AVCodecContext *input_codec_ctx[MAX_STREAM];

    //视频相关参数
    //surface对象
    ANativeWindow *nativeWindow;
    ANativeWindow_Buffer window_buffer;//结构
    SwsContext *swsContext;
    AVFrame *av_frame;
    AVFrame *rgb_frame;
    uint8_t *out_buff;
    int nativeHeight;
    int videoHeight;

    //音频相关参数
    enum AVSampleFormat in_sample_fmt;
    SwrContext *swrContext;
    int in_sample_rate;
    enum AVSampleFormat out_sample_fmt;
    int out_sample_rate;
    int out_channel_nb;
    //音频转换格式对象

    FILE *out_file;

    //jni调java方法 参数
    jobject audio_track;
    jmethodID audio_track_write_mid;

    //队列相关.音视频数组： 如果使用Queue queues[MAX_STREAM]声明会报错，指针数据则不会，前者没有给定默认值struct 结构体名 数组名[长度] = {{成员值列表},...{成员值列表}};
    Queue *queues[MAX_STREAM];//

    //线程相关
    //互斥锁、条件变量 -- 这里的操作有问题，初始化的互斥锁和条件变量 应该要创建过，而不是声明的指针
    pthread_mutex_t mutex;
    pthread_cond_t cond;

    //创建生产线程
    pthread_t thread_read_from_stream;

    //消费线程
    pthread_t decode_threads[MAX_STREAM];

    //时间同步相关
    int start_time;//
};

/**
 * 初始化封装格式上下文，获取音频视频流的索引位置
 */
int init_input_format_ctx(Player *player,const char *input_str){
    av_register_all();
    AVFormatContext *pFormatCtx = avformat_alloc_context();//这里报错出问题 ：没有将封装格式上下文结构体初始化
    if (avformat_open_input(&pFormatCtx, input_str, NULL, NULL) != 0) {//@return 0 on success, a negative AVERROR on failure.
        LOGE("打开输入文件失败");
        return -1;
    }
    if (avformat_find_stream_info(pFormatCtx, NULL) < 0) {//return >=0 if OK, AVERROR_xxx on error
        LOGE("读取信息失败");
        return -1;
    }
    player->captrue_streams_no = pFormatCtx->nb_streams;
    LOGI("captrue_streams_no:%d",player->captrue_streams_no);
    player->video_stream_index = player->audio_stream_index = -1;
    //找到视频索引
    for (int i = 0; i < pFormatCtx->nb_streams; i++) {
        if (pFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO) {
            player->video_stream_index = i;
        }else if(pFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO){
            player->audio_stream_index = i;
        }
    }
    if (player->video_stream_index == -1) {
        LOGE("未找到视频流");
        return -1;
    }
    else if (player->audio_stream_index == -1) {
        LOGE("未找到音频流");
        return -1;
    }else {
        LOGE("音视解码器索引：%d  -  %d", player->video_stream_index,player->audio_stream_index);
    }
    player->input_format_ctx = pFormatCtx;
    return 0;
}
int init_codec_ctx(Player *player,int stream_index){
    AVCodecContext *pCodexCtx;//编码器上下文结构体
    pCodexCtx = player->input_format_ctx->streams[stream_index]->codec;
    AVCodec *pCodec;//具体的编码器结构体
    pCodec = avcodec_find_decoder(pCodexCtx->codec_id);
    if (pCodec == NULL) {
        LOGE("未找到解码器");
        return -1;
    } else {
        LOGE("解码器名称：%s", pCodec->name);
    }
    //打开解码器
    if (avcodec_open2(pCodexCtx, pCodec, NULL) <
        0) {//@return zero on success, a negative value on error
        LOGE("打开解码器失败");
        return -1;
    }
    //最终的目的：拿到解码器将指针存入的Player中
    player->input_codec_ctx[stream_index] = pCodexCtx; //--------------------------------这里是指针数据，将数组指针指向解码器
    return 0;
}

int decode_video_prepare(Player *player,JNIEnv *env,jobject surface){
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
    AVCodecContext *pCodecCtx = player->input_codec_ctx[player->video_stream_index];
    ANativeWindow_setBuffersGeometry(nativeWindow,pCodecCtx ->width, pCodecCtx->width * nativeHeight / nativeWidth ,
                                     WINDOW_FORMAT_RGBA_8888);//// 重新计算绘制区域的高度，防止纵向变形

    int videoWidth = nativeWidth > pCodecCtx->width ? pCodecCtx->width : nativeWidth;//拿较小宽度
//    videoHeight = videoHeight * videoWidth/ nativeWidth;//等比 计算得到高度---------------------这里不对，要显示视频，取视频的宽高比
    int videoHeight = videoWidth * pCodecCtx->height / pCodecCtx->width;
    if (videoHeight > nativeHeight) {//若此时高度大于 屏幕高度，缩放高度。等比计算得到宽度
        videoHeight = nativeHeight;
        videoWidth = videoHeight * pCodecCtx->width / pCodecCtx->height;//这里其实就没有再考虑 宽度放不下了
    }


    AVFrame *rgb_frame;//转换后的rgb格式解码像素数据
    rgb_frame = av_frame_alloc();//转换后rgb帧的数据

    //---------------------------------这个地方没有理解，变量类型怎么来的
    uint8_t *out_buff = av_malloc(avpicture_get_size(AV_PIX_FMT_RGBA, videoWidth, videoHeight));//计算rgb缓冲区大小
    avpicture_fill((AVPicture *)rgb_frame,out_buff,AV_PIX_FMT_RGBA,videoWidth,videoHeight);//填充rgb的data缓冲区
    //转换器。视频图像转换
    SwsContext *swsContext = sws_getContext(pCodecCtx->width,pCodecCtx->height,pCodecCtx->pix_fmt,videoWidth,videoHeight,AV_PIX_FMT_RGBA,SWS_BICUBIC,NULL,NULL,NULL);

    player->nativeWindow = ANativeWindow_fromSurface(env,surface);//这里就是指针赋值
    player->window_buffer = window_buffer;
    player->av_frame = av_frame;
    player->rgb_frame = rgb_frame;
    player->out_buff = out_buff;
    player->swsContext = swsContext;
    player->nativeHeight = nativeHeight;
    player->videoHeight = videoHeight;
    return 0;
}

int decode_audio_prepare(Player *player){
    AVCodecContext *pCodecCtx = player->input_codec_ctx[player->audio_stream_index];
    enum AVSampleFormat in_format = pCodecCtx->sample_fmt;//采样格式
    int in_rate = pCodecCtx->sample_rate;//采样率
    uint64_t in_layout = pCodecCtx->channel_layout;//拿声道布局

    enum AVSampleFormat out_format = AV_SAMPLE_FMT_S16;//采样格式
    int out_rate = in_rate;
    uint64_t out_layout = AV_CH_LAYOUT_STEREO;//立体声

    int nb_channels = av_get_channel_layout_nb_channels(out_layout);//声道个数

    //音频格式结构体
    SwrContext *swr_ctx = swr_alloc();
    swr_alloc_set_opts(swr_ctx,out_layout,out_format,out_rate,in_layout,in_format,in_rate,0,NULL);
    swr_init(swr_ctx);
    //最终目的，将数据存到player中
    player->in_sample_fmt = in_format;
    player->in_sample_rate = in_rate;
    player->out_sample_fmt = out_format;
    player->out_sample_rate = out_rate;
    player->out_channel_nb = nb_channels;
    player->swrContext = swr_ctx;
    return 0;
}

int jni_audio_prepare(JNIEnv *env,jobject instance,Player *player){
    //获取java端对象，jclass、jmethodid、调用方法得到jobject
    // 获取play方法并执行、获取write方法、组织两个参数（byte数组 jni创建，然后获取到c的指针引用，处理数据，同步数据）。 调用write方法
    jclass jclazz = (*env)->GetObjectClass(env, instance);
    jmethodID getInstanceId = (*env)->GetMethodID(env, jclazz, "getAudioTrack", "(II)Landroid/media/AudioTrack;");
    jobject audioTrack = (*env)->CallObjectMethod(env, instance, getInstanceId, player->out_sample_rate, player->out_channel_nb);//传递采样率 和声道个数。 int类型的字节长度 与jint相同

    jclass audio_class = (*env)->GetObjectClass(env, audioTrack);
    jmethodID playId = (*env)->GetMethodID(env, audio_class, "play", "()V");
    //播放
    (*env)->CallVoidMethod(env,audioTrack,playId);
    //把audioTrack 这个设置成全局的引用
    player->audio_track = (*env)->NewGlobalRef(env, audioTrack);

//    jmethodID audio_track_write = (*env)->GetMethodID(env, audio_class, "write", "([BII)I");
    player->audio_track_write_mid = (*env)->GetMethodID(env, audio_class, "write", "([BII)I");
    return 0;
}
/**
 * 给AVPacket开辟空间，后面会将AVPacket栈内存数据拷贝至这里开辟的空间
 */
void* player_fill_packet(){
    //这里是返回一个指针，让指针赋值给让队列元素指针，打包赋值目的
    AVPacket *pkt = (AVPacket*)av_malloc(sizeof(AVPacket));
    return pkt;
}

int init_alloc_queue(Player *player){
    //这里初始化后 ，后面能通过索引拿到是因为 音视频所在流索引是 0,1，巧合可以通过遍历拿到
    for(int i=0;i<player->captrue_streams_no;i++){
        Queue * queue = queue_init(PACKAGE_QUEUE_SIZE,player_fill_packet);
        player->queues[i] = queue;
        //打印视频音频队列地址
        LOGE("stream index:%d,queue:%#x",i,queue);
    }
    return 0;// 这里没有返回值导致后续无法执行
}

/**
 * 这里的操作有问题，初始化的互斥锁和条件变量 应该要创建过，而不是声明的指针
 * @param player
 * @return
 */
int init_pthread(Player *player){
    LOGE("init_pthread  开始");
    pthread_mutex_init(&(player->mutex),NULL);
    pthread_cond_init(&(player->cond),NULL);
    return 0;
}

void* player_read_from_stream(void* args){
    LOGE("player_read_from_stream  开始");
    Player *player = (Player *)args;
// 读取的帧数量
    int frame_count = 0;
    // 是否解封装完成
    int got_frame;
    // 循环读取每一帧的数据
//    AVPacket *pkt;//这里值在函数栈中，将内容赋值 拷贝给 给队列中的元素后，函数执行完会自动回收

    //上一句直接使用会报错，因为指针没有分配内存空间 ---或者使用av_malloc然后初始化
        AVPacket packet,*pkt=&packet;//方式1

    //方式2
//    AVPacket *pkt;
//    //读取解码一帧数据，先初始化pkt，frame
//    pkt = (AVPacket *) av_malloc(sizeof(AVPacket));
//    av_init_packet(pkt);

    //循环读取数据，线程每次只允许一处 操作，其他等待
    while (av_read_frame(player->input_format_ctx,pkt) >= 0) {
        // < 0 代表读到文件末尾了
//        if(ret < 0 ){// 这一句不能要 因为没有没有解码，只是拿Packet
//            break;
//        }
        //生产的每一个Packet包要根据索引 放入指定定的放入队列
        //1.获取对应队列 2.给队列元素赋值
        Queue *queue = player->queues[pkt->stream_index];
        //循环读取的一帧数据存在方法栈中， 操作队列的时候要加锁
        pthread_mutex_lock(&(player->mutex));
        AVPacket *retPkt = (AVPacket*)queue_push(queue,&(player->mutex), &(player->cond));
        //拷贝（间接赋值，拷贝结构体数据）
        //-----------------------------//这里应该先于 唤醒阻塞？？？不然唤醒了却在写数据--这里锁没释放
        *retPkt = *pkt;//千万不可用retPkt = pkt.这里是指针指向了栈中数据
        av_free_packet(pkt);//释放内存
        pthread_mutex_unlock(&(player->mutex));
        LOGI("player_read_from_stream queue:%#x, packet:%#x",queue,pkt);
//        LOGI("player_read_from_stream queue:%#x, packet:%#x",*queue,*pkt);//报错-------不是地址
        }
}

typedef struct _DecoderData DecoderData;
struct _DecoderData{
    Player *player;
    int stream_index;
};

int free_player(Player *player){
    ANativeWindow_release(player->nativeWindow);
//    free(out_buff);
//    av_frame_free(&av_frame);
//    av_frame_free(&rgb_frame);
    sws_freeContext(player->swsContext);
    swr_free(player->swrContext);
    //两个解码器
    for(int i=0;i<player->captrue_streams_no;i++){
        avcodec_close(player->input_codec_ctx[i]);
    }
    avformat_close_input(&(player->input_format_ctx));
    free(player);
};

/**
 * 获取视频当前播放时间
 */
int64_t player_get_current_video_time(Player *player) {
    int64_t current_time = av_gettime();
    return current_time - player->start_time;
}

/**
 * 延迟
 */
void player_wait_for_frame(Player *player, int64_t stream_time,
                           int stream_no) {
    pthread_mutex_lock(&player->mutex);
    for(;;){
        int64_t current_video_time = player_get_current_video_time(player);
        int64_t sleep_time = stream_time - current_video_time;
        if (sleep_time < -300000ll) {
            // 300 ms late
            int64_t new_value = player->start_time - stream_time + av_gettime() - player->start_time;
            LOGI("player_wait_for_frame[%d] correcting %f to %f because late",
                 stream_no, (av_gettime() - player->start_time) / 1000000.0,
                 (av_gettime() - new_value) / 1000000.0);

            player->start_time = new_value;
            pthread_cond_broadcast(&player->cond);
        }

        if (sleep_time <= MIN_SLEEP_TIME_US) {
            // We do not need to wait if time is slower then minimal sleep time
            break;
        }

        if (sleep_time > 500000ll) {
            // if sleep time is bigger then 500ms just sleep this 500ms
            // and check everything again
            sleep_time = 500000ll;
        }
        //等待指定时长
        int timeout_ret = pthread_cond_timeout_np(&player->cond,
                                                  &player->mutex, sleep_time/1000ll);

        // just go further
        LOGI("player_wait_for_frame[%d] finish", stream_no);
    }
    pthread_mutex_unlock(&player->mutex);
}

int decode_video(Player *player,AVPacket *pkt){
    int got_frame;
    AVFormatContext *input_format_ctx = player->input_format_ctx;
    // 时基要从流中得到
    AVStream *stream = input_format_ctx->streams[pkt->stream_index];//这个索引跟player->video_stream_index一样
    // 解封装每一帧的视频数据
    int ret = avcodec_decode_video2(player->input_codec_ctx[pkt->stream_index], player, &got_frame, pkt);
    if(ret < 0){
        LOGE("一帧 decode_video 解码 完毕");
    }
    if (got_frame > 0) {
        // 开始绘制，锁定不让其它的线程绘制
        ANativeWindow_lock(player->nativeWindow, &(player->window_buffer), NULL);
        // 解码数据 -> rgba
        sws_scale(player->swsContext, (const uint8_t *const *) player->av_frame->data, player->av_frame->linesize, 0,
                  player->av_frame->height, player->rgb_frame->data, player->rgb_frame->linesize);
        // 缓冲区的地址
        uint8_t *dst = (uint8_t *) player->window_buffer.bits;
        // 每行的内存大小
        int dstStride = player->window_buffer.stride * 4;
        // 像素区的地址
        uint8_t *src = player->rgb_frame->data[0];
        int srcStride = player->rgb_frame->linesize[0];
        for (int i = 0; i < player->videoHeight; i++) {
            // 原画大小播放
            // 逐行拷贝内存数据，但要进行偏移，否则视频会拉伸变形
            // (i + (windowHeight - videoHeight) / 2) * dstStride 纵向偏移，确保视频纵向居中播放
            // (dstStride - srcStride) / 2 横向偏移，确保视频横向居中播放
            memcpy(dst + (i + (player->nativeHeight - player->videoHeight) / 2) * dstStride +
                   (dstStride - srcStride) / 2, src + i * srcStride,
                   srcStride);

        }
        //--------------------------------时间同步的处理
        //计算延迟
        //播放时间
        int64_t pts = av_frame_get_best_effort_timestamp(player->av_frame);
        //转换（不同时间基时间转换）
        int64_t time = av_rescale_q(pts,stream->time_base,AV_TIME_BASE_Q);

        player_wait_for_frame(player,time,player->video_stream_index);

        ANativeWindow_unlockAndPost(player->nativeWindow);
        usleep(4 * 1000);
//        LOGE("解码第%d帧", frame_count++);
    }
    return 0;
}
int decode_audio(Player *player,AVPacket *pkt) {

    //解码的过程在至子线程中，所以想从javaVM中拿到env环境变量，需要使用javaVM
    AVCodecContext *pCodecCtx = player->input_codec_ctx[player->audio_stream_index];
    AVStream *avStream = player->input_format_ctx->streams[player->audio_stream_index];
    AVFrame *avFrame = av_frame_alloc();//player->av_frame;
    AVPacket *avPacket = pkt;
    int got_frame;
    //解码一帧
    LOGE("got_frame 数值判断大小之前阿萨德所");
    int ret = avcodec_decode_audio4(pCodecCtx, avFrame, &got_frame, avPacket);
    if(ret<0){
        LOGE("一帧 decode_audio 解码 完毕");
    }
    //16bit 44100 PCM 数据（重采样缓冲区）
    uint8_t *out_buffer = (uint8_t *)av_malloc(MAX_AUDIO_FRME_SIZE);
    LOGE("got_frame 数据判断");
    //解码成功后处理数据
    if (got_frame > 0) {
        LOGE("got_frame 处理解码数据");
        //音频格式转换
        swr_convert(player->swrContext, out_buffer, MAX_AUDIO_FRME_SIZE, avFrame->data,
                    avFrame->nb_samples);//最后这个参数是 一帧包含的音频个数
        int out_buff_size = av_samples_get_buffer_size(NULL, player->out_channel_nb,
                                                       avFrame->nb_samples, player->out_sample_fmt,
                                                       1);//数据大小
        //写入文件
        fwrite(out_buffer, 1, out_buff_size, player->out_file);

        int64_t pts = pkt->pts;
//        int64_t pts = av_frame_get_best_effort_timestamp(player->av_frame);//这两个啥区别
        if (pts != AV_NOPTS_VALUE) {//没结束就要 等待了在播放。延时队列
            int64_t time = av_rescale_q(pts, avStream->time_base, AV_TIME_BASE_Q);// 64位就是4个字节，
            //				av_q2d(stream->time_base) * pts;
            LOGI("player_write_audio - read from pts");
            player_wait_for_frame(player, time + AUDIO_TIME_ADJUST_US, player->audio_stream_index);
        }


        //调用java端的代码播放音乐
//                最终要调用的代码是
//                (*env)->CallObjectMethod(env,audioTrack,audio_track_write,字节数据，位偏移，写入长度)
        //拿到环境变量
        //关联当前线程的JNIEnv
        JavaVM *javaVM = player->javaVM;
        JNIEnv *env;
        (*javaVM)->AttachCurrentThread(javaVM, &env, NULL);

        //处理jni的字节数组 数据
        jbyteArray jbyteArray1 = (*env)->NewByteArray(env, out_buff_size);//jni
        jbyte *sample_bytes = (*env)->GetByteArrayElements(env, jbyteArray1,
                                                           NULL);//拿到jbyte的指针（一维数组）。jbyte和c的byte字节长度一样，可以操作
//                (*env)->SetByteArrayRegion();//这个方式是把jbyte数组的值赋值到jbyteArray上
        memcpy(sample_bytes, player->out_buff, out_buff_size);
        //释放内存：同步数据
        (*env)->ReleaseByteArrayElements(env, jbyteArray1, sample_bytes, 0);

        //写数据
        (*env)->CallIntMethod(env, player->audio_track, player->audio_track_write_mid, jbyteArray1,
                              0, out_buff_size);
        (*env)->DeleteLocalRef(env, jbyteArray1);
        //退出关联
        (*javaVM)->DetachCurrentThread(javaVM);
        usleep(1000 * 16);
    }
    return 0;
}

void* decode_data(void* args){
    LOGE("decode_data 开始");
    DecoderData *decoderData = (DecoderData*)args;
    Player *player = decoderData->player;
    int stream_index = decoderData->stream_index;
    //解码：1.从指定队列中 拿到队列元素 内容AVPacket ，队列操作需要加锁 2.根据流索引 分别进行解码
    Queue *queue = player->queues[stream_index];
    //一帧一帧读取压缩的视频数据AVPacket
    int video_frame_count = 0, audio_frame_count = 0;
    //循环读取队列解码
    for(;;){//------------------无线循环-------------------那么要求在某个时刻销毁线程，join又在等待，应该数据操作完主动销毁
        //消费AVPacket
        //队列操作 需要加锁
        pthread_mutex_lock(&(player->mutex));
        AVPacket *pkt = queue_pop(queue, &(player->mutex), &(player->cond));
        pthread_mutex_unlock(&(player->mutex));
        if(pkt->stream_index == player->video_stream_index){
            decode_video(player,pkt);
            LOGI("video_frame_count:%d",video_frame_count++);
        }if(pkt->stream_index == player->audio_stream_index){
            decode_audio(player,pkt);
            LOGI("audio_frame_count:%d",audio_frame_count++);
        }
    }
}

JNIEXPORT void JNICALL
Java_com_example_applib1_JniTest_play(JNIEnv *env, jobject instance, jstring input_,
                                      jstring output_,jobject surface) {
    const char *input_str = (*env)->GetStringUTFChars(env, input_, NULL);
    const char *output_str = (*env)->GetStringUTFChars(env, output_, NULL);
    //1.Player结构体分配内存空间
    Player *player = malloc(sizeof(Player));
    player->out_file = fopen(output_str,"wb+");
    //2.通过GetJavaVM 拿到javaVM放置在Player中
    (*env)->GetJavaVM(env,&(player->javaVM));
    //3.初始化封装格式上下文，拿到音频流所在流的索引
    int ret = init_input_format_ctx(player,input_str);
    if(ret <0){ return; }
    //4.取出音视频流所在索引--直接通过player指针拿
    int video_stream_index = player->video_stream_index;
    int audio_stream_index = player->audio_stream_index;
    //5.获取音视频解码器，并打开
    ret = init_codec_ctx(player,video_stream_index);
    if(ret <0){ return; }
    ret = init_codec_ctx(player,audio_stream_index);
    if(ret <0){ return; }

    //6.视频解码准备：创建ANativeWindow
    ret = decode_video_prepare(player,env,surface);
    if(ret <0){ return; }
    //7.音频解码准备：根据解码器，拿到输入的音频采样格式、采样率、声道布局，设置输出的音频采样率，采样格式，声道布局
    ret = decode_audio_prepare(player);
    if(ret <0){ return; }
    //8.播放音频准备：要通过c调java播放音频，主线拿到AudioTrack的对象设置成全局引用，write方法只能通过全局变量的方式处理
    ret = jni_audio_prepare(env,instance,player);
    if(ret <0){ return; }
    //9.初始化队列：AVStream有几个就有几个队列，队列中的元素通过回调的方式完成内存分配
    ret = init_alloc_queue(player);
    if(ret <0){ return; }
    //10.线程 互斥锁和条件变量初始化
    ret = init_pthread(player);
    if(ret <0){
        LOGE("init_pthread");
        return; }

    //11.生产者线程
    pthread_create(&(player->thread_read_from_stream),NULL,player_read_from_stream,(void*)player);//这里要给线程指针，否则报错

    //12. 睡眠一毫秒 ------等先生产？
    sleep(1);
    player->start_time = 0;//初始化开始时间为0-------------------------------------为啥

    //13.消费者线程（两个） 1.先组装DecoderData(包括player和数据所属的流索引)
    DecoderData data1={player,video_stream_index},*decoder_data1 = &data1;
    DecoderData data2={player,audio_stream_index},*decoder_data2 = &data2;
//    pthread_create(&(player->decode_threads[video_stream_index]),NULL,decode_data,(void*)decoder_data1);
    pthread_create(&(player->decode_threads[audio_stream_index]),NULL,decode_data,(void*)decoder_data2);

    //14.阻塞等待3个线程完成pthread_join() --------这里的参数不能用 指针
    pthread_join(player->thread_read_from_stream,NULL);
    pthread_join(player->decode_threads[video_stream_index],NULL);
    pthread_join(player->decode_threads[audio_stream_index],NULL);
    //15.释放内存
    ret = free_player(player);
    if(ret <0){ return; }

    (*env)->ReleaseStringUTFChars(env,input_,input_str);
    (*env)->ReleaseStringUTFChars(env,output_,output_str);
}
