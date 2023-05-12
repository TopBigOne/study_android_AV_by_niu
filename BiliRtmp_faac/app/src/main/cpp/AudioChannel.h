//
// Created by Administrator on 2021/1/25.
//

#ifndef BILIRTMP_AUDIOCHANNEL_H
#define BILIRTMP_AUDIOCHANNEL_H

#include <faac.h>
#include "librtmp/rtmp.h"

/**
 * 指针函数，用作回调，参数是：RTMPPacket
 */
typedef void (*Callback)(RTMPPacket *);

class AudioChannel {
public:
    AudioChannel();

    ~AudioChannel();

    void openCodec(int sampleRate, int channels);

/**
 * 编码音频数据
 * @param data
 * @param len
 */
    void encode(int32_t *data, int len) const;

/**
 * 头帧
 * @return
 */
    RTMPPacket *getAudioConfig() const;

    void setCallback(Callback callback) {
        this->callback = callback;
    }

    int getInputByteNum() {
        return inputByteNum;
    }

public:
    Callback callback;
    faacEncHandle codec = 0;

    /**
     * 音频压缩成aac后最大数据量
     */
    unsigned long maxOutputBytes;


    /**
     * 输出的数据
     */
    unsigned char *outputBuffer = 0;


    /**
     *  输入容器的大小
     */
    unsigned long inputByteNum;
};


#endif //BILIRTMP_AUDIOCHANNEL_H
