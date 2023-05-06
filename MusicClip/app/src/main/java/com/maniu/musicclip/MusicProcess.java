package com.maniu.musicclip;

import android.content.Context;
import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

//MP3-->MP31
public class MusicProcess {
    private static final String TAG = "MusicProcess : ";

    String mVideoPcmFilePath      = "";
    String mMusicPcmFilePath      = "";
    String mixPcmFileAbsolutePath = "";


    private Context context;

    public MusicProcess(Context context) {
        this.context = context;
        final File videoPcmFile = new File(context.getFilesDir(), "video.pcm");
        final File musicPcmFile = new File(context.getFilesDir(), "music.pcm");
        final File mixPcmFile   = new File(context.getFilesDir(), "mix.pcm");
        mixPcmFileAbsolutePath = mixPcmFile.getAbsolutePath();
        mVideoPcmFilePath = videoPcmFile.getAbsolutePath();
        mMusicPcmFilePath = musicPcmFile.getAbsolutePath();
    }

    /**
     * 防止精度丢失
     *
     * @param volume
     * @return
     */
    private static float normalizeVolume(int volume) {
        return volume / 100f * 1;
    }


    /**
     * @param videoInputPath
     * @param audioInputPath
     * @param output
     * @param startTimeUs 开始时间
     * @param endTimeUs   结束时间
     * @param videoVolume 视频声音大小
     * @param aacVolume   音频声音大小
     */
    public void mixAudioTrack(final String videoInputPath, final String audioInputPath, final String output, final Integer startTimeUs, final Integer endTimeUs, int videoVolume,//
                              int aacVolume) {
        Log.d(TAG, "mixAudioTrack: ");
        checkInputFiles(videoInputPath, audioInputPath);


        try {
            decodeToPCM(videoInputPath, mVideoPcmFilePath, startTimeUs, endTimeUs);
            decodeToPCM(audioInputPath, mMusicPcmFilePath, startTimeUs, endTimeUs);

            mixPcm(videoVolume, aacVolume);

            PcmToWavUtil pcmToWavUtil = new PcmToWavUtil(44100, AudioFormat.CHANNEL_IN_STEREO, 2, AudioFormat.ENCODING_PCM_16BIT);

            pcmToWavUtil.pcmToWav(mixPcmFileAbsolutePath, output);
        } catch (Exception e) {
            Log.e(TAG, "mixAudioTrack: ERROR:" + e.getMessage());
            e.printStackTrace();
        }

    }

    private void checkInputFiles(String videoInput, String audioInput) {

        Log.d(TAG, "checkInputFiles: audioInput : "+audioInput);
        Log.d(TAG, "checkInputFiles: videoInput : "+videoInput);
        final File videoInputFile = new File(videoInput);
        final File musicInputFile = new File(audioInput);
        if (!videoInputFile.exists()) {
            Log.e(TAG, "checkInputFiles: videoInputFile is not exists");
        }

        if (!musicInputFile.exists()) {
            Log.e(TAG, "checkInputFiles: musicInputFile is not exists");
        }

    }


    /**
     * MP3 截取并且输出  pcm
     *
     * @param inputPath
     * @param outPath
     * @param startTime seekTo() 要使用；
     * @param endTime
     */
    public void decodeToPCM(String inputPath, String outPath, int startTime, int endTime) {
        if (endTime < startTime) {
            return;
        }
        Log.d(TAG, "decodeToPCM: ");

        Log.i(TAG, "    decodeToPCM: inputPath : " + inputPath);
        Log.i(TAG, "    decodeToPCM: outPath   : " + outPath);
        //    MP3  （zip  rar    ） ----> aac   封装个事 1   编码格式
        //        jie  MediaExtractor = 360 解压 工具
        MediaExtractor mediaExtractor = new MediaExtractor();
        try {
            mediaExtractor.setDataSource(inputPath);
        } catch (Exception e) {
            Log.e(TAG, "decodeToPCM: ERROR : " + e.getMessage());
            e.printStackTrace();
        }


        int audioTrack = selectTrack(mediaExtractor);

        mediaExtractor.selectTrack(audioTrack);
        // core :视频 和音频 seek 到开始时间
        mediaExtractor.seekTo(startTime, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
        // 轨道信息  都记录 编码器
        MediaFormat oriAudioFormat = mediaExtractor.getTrackFormat(audioTrack);

        int         maxBufferSize  = 100 * 1000;
        if (oriAudioFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
            maxBufferSize = oriAudioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
        } else {
            maxBufferSize = 100 * 1000;
        }


        ByteBuffer buffer = ByteBuffer.allocateDirect(maxBufferSize);


        MediaCodec  mediaCodec   = null;
        FileChannel writeChannel = null;

        try {

            //  h264   H265  音频
            mediaCodec = MediaCodec.createDecoderByType(oriAudioFormat.getString((MediaFormat.KEY_MIME)));
            //        设置解码器信息    直接从 音频文件
            mediaCodec.configure(oriAudioFormat, null, null, 0);
            File pcmFile = new File(outPath);

            writeChannel = new FileOutputStream(pcmFile).getChannel();
            mediaCodec.start();

        } catch (Exception e) {
            e.printStackTrace();
        }


        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

        int outputBufferIndex = -1;
        // 从start time 开始，不断地读取数据
        while (true) {
            // - dequeueInputBuffer(timeoutUs):  获取输入流队列的ByteBuffer下标，timeoutUs为延迟，0时为立即返回
            int decodeInputIndex = mediaCodec.dequeueInputBuffer(100000);
            if (decodeInputIndex >= 0) {
                // 获取pts
                long sampleTimeUs = mediaExtractor.getSampleTime();

                if (sampleTimeUs == -1) {
                    break;
                } else if (sampleTimeUs < startTime) {
                    //                    丢掉 不用了
                    mediaExtractor.advance();
                    continue;
                } else if (sampleTimeUs > endTime) {
                    break;
                }
                //  获取到压缩数据
                bufferInfo.size = mediaExtractor.readSampleData(buffer, 0);
                bufferInfo.presentationTimeUs = sampleTimeUs;
                bufferInfo.flags = mediaExtractor.getSampleFlags();

                //   下面放数据  到dsp解码
                byte[] content = new byte[buffer.remaining()];
                buffer.get(content);
                // 输出文件  方便查看
                // FileUtils.writeContent(content);
                // 获取输入流队列，返回一个ByteBuffer
                ByteBuffer inputBuffer = mediaCodec.getInputBuffer(decodeInputIndex);
                inputBuffer.put(content);
                // - queueInputBuffer(): 输入流 入队列 ，注意，这里的时间参数
                mediaCodec.queueInputBuffer(decodeInputIndex, 0, bufferInfo.size, bufferInfo.presentationTimeUs, bufferInfo.flags);
                //                释放上一帧的压缩数据
                mediaExtractor.advance();
            }

            // 从输出队列中 取出编码操作之后的数据,返回输出流队列下标
            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 100_000);
            //
            while (outputBufferIndex >= 0) {
                // 获取编解码之后的数据输出流队列，返回的是一个ByteBuffer,
                // 需要一直循环读取，直到dequeueOutputBuffer()返回的下标小于0
                ByteBuffer decodeOutputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex);
                if (writeChannel != null) {
                    try {
                        //MP3  1   pcm2
                        //  拿到编码数据，开始写出
                        writeChannel.write(decodeOutputBuffer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 100_000);
            }
        }
        try {
            if (writeChannel != null) {
                writeChannel.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaExtractor.release();
        mediaCodec.stop();
        mediaCodec.release();
        //        转换MP3    pcm数据转换成mp3封装格式
        //
        //        File wavFile = new File(Environment.getExternalStorageDirectory(),"output.mp3" );
        //        new PcmToWavUtil(44100,  AudioFormat.CHANNEL_IN_STEREO,
        //                2, AudioFormat.ENCODING_PCM_16BIT).pcmToWav(pcmFile.getAbsolutePath()
        //                , wavFile.getAbsolutePath());
        Log.i(TAG, "mixAudioTrack: 转换完毕");
    }

    private int selectTrack(MediaExtractor mediaExtractor) {
        Log.d(TAG, "selectTrack: ");
        //获取每条轨道
        int numTracks = mediaExtractor.getTrackCount();
        Log.i(TAG, "    selectTrack: numTracks : " + numTracks);

        for (int i = 0; i < numTracks; i++) {
            //            数据      MediaFormat
            MediaFormat format = mediaExtractor.getTrackFormat(i);
            String      mime   = format.getString(MediaFormat.KEY_MIME);
            Log.i(TAG, "    selectTrack: mime : " + mime);

            if (mime.startsWith("audio/")) {
                return i;
            }
        }
        return -1;
    }

    /**
     * vol1  vol2  0-100  0静音  120
     *
     * @param volume1 视频的声音
     * @param volume2 mp3的声音
     * @throws IOException
     */
    public void mixPcm(int volume1, int volume2) throws IOException {
        Log.d(TAG, "mixPcm: ");

        // mVideoPcmFilePath, mMusicPcmFilePath, mixPcmFileAbsolutePath
        float vol1 = normalizeVolume(volume1);
        float vol2 = normalizeVolume(volume2);
        //一次读取多一点 2k
        byte[] buffer1 = new byte[2048];
        byte[] buffer2 = new byte[2048];
        //        待输出数据
        byte[] buffer3 = new byte[2048];

        FileInputStream is1 = new FileInputStream(mVideoPcmFilePath);
        FileInputStream is2 = new FileInputStream(mMusicPcmFilePath);

        //输出PCM 的
        FileOutputStream fileOutputStream = new FileOutputStream(mixPcmFileAbsolutePath);

        short temp2, temp1;
        //两个short变量相加 会大于short   声音
        int     temp;
        boolean end1 = false, end2 = false;
        while (!end1 || !end2) {

            if (!end1) {
                //
                end1 = (is1.read(buffer1) == -1);
                // 音乐的pcm数据 ： buffer1  写入到 buffer3
                System.arraycopy(buffer1, 0, buffer3, 0, buffer1.length);

            }

            if (!end2) {
                end2 = (is2.read(buffer2) == -1);
                //声音的值  跳过下一个声音的值    一个声音 2 个字节
                // 高8位+低8位 = 声音值
                int voice = 0;

                // i += 2/*跳过下一个声音的*/
                for (int i = 0; i < buffer2.length; i += 2) {
                    //   或运算
                    //  (buffer1[i + 1] & 0xff) << 8 变成高8位
                    temp1 = (short) ((buffer1[i] & 0xff) | (buffer1[i + 1] & 0xff) << 8);
                    temp2 = (short) ((buffer2[i] & 0xff) | (buffer2[i + 1] & 0xff) << 8);
                    //音乐和 视频声音 各占一半
                    temp = (int) (temp1 * vol1 + temp2 * vol2);
                    // short 的取值范围是 65535，
                    // 65535/2 = 32767
                    if (temp > 32767) {
                        temp = 32767;
                    } else if (temp < -32768) {
                        temp = -32768;
                    }
                    // temp 解析成高8位和低8位
                    // case i : 低8位
                    buffer3[i] = (byte) (temp & 0xFF);
                    // case i+1 : 高8位
                    buffer3[i + 1] = (byte) ((temp >>> 8) & 0xFF);
                }
                // 写出数据
                fileOutputStream.write(buffer3);
            }
        }
        is1.close();
        is2.close();
        fileOutputStream.close();
        Log.d(TAG, "    mixPcm end. ");
    }

}
