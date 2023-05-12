//
// Created by Administrator on 2021/1/25.
//

#include <malloc.h>
#include <cstring>
#include "AudioChannel.h"
#include "maniulog.h"

AudioChannel::AudioChannel() {

}

AudioChannel::~AudioChannel() {

}

void AudioChannel::openCodec(int sampleRate, int channels) {

    //输入样本的容器 大小： 要送给编码器编码的样本数

//    maxOutputBytes 编码  一帧的最大大小
    unsigned long inputSamples;
//    实例化faac编码器
    /**
     unsigned long   nSampleRate,        // 采样率，单位是bps
    unsigned long   nChannels,          // 声道，1为单声道，2为双声道
    unsigned long   &nInputSamples,     // 传引用，得到每次调用编码时所应接收的原始数据长度
    unsigned long   &nMaxOutputBytes    // 传引用，得到每次调用编码时生成的AAC数据的最大长度
     */
    codec = faacEncOpen(sampleRate, channels, &inputSamples, &maxOutputBytes);

//输入容器真正大小
    inputByteNum = inputSamples * 2;

//实例化 输出的容器
    outputBuffer = static_cast<unsigned char *>(malloc(maxOutputBytes));
    LOGE("初始化-----------》%d  inputByteNum %d  maxOutputBytes:%d ", codec, inputByteNum,
         maxOutputBytes);
//参数
    faacEncConfigurationPtr configurationPtr = faacEncGetCurrentConfiguration(codec);
//编码  MPEG AAC
    configurationPtr->mpegVersion = MPEG4;
//    编码等级
    configurationPtr->aacObjectType = LOW;
    // 输出aac裸流数据
    configurationPtr->outputFormat = 0;
    // 采样位数
    configurationPtr->inputFormat = FAAC_INPUT_16BIT;
    // 将我们的配置生效
    faacEncSetConfiguration(codec, configurationPtr);
}


/**
 *
 * @param data  原始数据
 * @param len
 */
void AudioChannel::encode(int32_t *data, int len) const {
    // 音频的数据   data   原始数据  1 编码 = 压缩 数据2  检查  bug   编码初始化成功
    LOGE("发送音频%d", len);

    // 将pcm数据编码成aac数据
    int byte_len = faacEncEncode(codec, data, len, outputBuffer, maxOutputBytes);
    //outputBuffer   压缩1   原始 2
    if (byte_len > 0) {
      // 拼装packet  数据   NDK
        auto *packet = new RTMPPacket;
        RTMPPacket_Alloc(packet, byte_len + 2);
        // AF+01+aac裸流
        // AF+01 占用2个字节
        packet->m_body[0] = 0xAF;
        // 普通音频帧
        packet->m_body[1] = 0x01;
        // 将编码的数据copy到  packet 中
        memcpy(&packet->m_body[2], outputBuffer, byte_len);
        packet->m_hasAbsTimestamp = 0;
        packet->m_nBodySize = byte_len + 2;
        packet->m_packetType = RTMP_PACKET_TYPE_AUDIO;
        packet->m_nChannel = 0x11;
        packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
        // 将audio packet 放到队列中
        callback(packet);
    }
}

RTMPPacket *AudioChannel::getAudioConfig() const{
    //    视频帧的sps pps
    u_char *buf;
    u_long len;
    // 得到头帧的内容   {0x12 0x08}
    faacEncGetDecoderSpecificInfo(codec, &buf, &len);
    LOGE("getAudioConfig buf content %s", buf);

    //头帧的  rtmpdump  实时录制  实时给时间戳
    auto *packet = new RTMPPacket;
    RTMPPacket_Alloc(packet, len + 2);

    // 0xAF : 2 字节，16位
    packet->m_body[0] = 0xAF;
    // 0x00 : 表示是一个head帧
    packet->m_body[1] = 0x00;
    // m_body[2]: 放数据
    memcpy(&packet->m_body[2], buf, len);

    packet->m_hasAbsTimestamp = 0;
    packet->m_nBodySize = len + 2;
    // 包类型
    packet->m_packetType = RTMP_PACKET_TYPE_AUDIO;
    packet->m_nChannel = 0x11;
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    return packet;
}
//休息十分钟