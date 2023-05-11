//
// Created by Administrator on 2021/1/18.
//

#ifndef BILIRTMP_VIDEOCHANNEL_H
#define BILIRTMP_VIDEOCHANNEL_H

#include <inttypes.h>
#include <jni.h>
#include <x264.h>

class VideoChannel {
public:
    VideoChannel();

    ~VideoChannel();

    //创建x264编码器
    void setVideoEncInfo(int width, int height, int fps, int bitrate);

//真正开始编码一帧数据
    void encodeData(int8_t *data);

private:
    int mWidth;
    int mHeight;
    int mFps;
    int mBitrate;
//    yuv-->h264 平台 容器 x264_picture_t=bytebuffer
    x264_picture_t *pic_in = 0;
    int ySize;
    int uvSize;
//    编码器
    x264_t *videoCodec = 0;


};

#endif
