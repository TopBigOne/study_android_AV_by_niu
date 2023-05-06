package com.maniu.douyinsample;

import android.content.Context;
import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class MusicProcess {
    private static final String TAG = "MusicProcess : ";

    private Context context;
    private int     startTimeUs;
    private int     endTimeUs;
    int  videoVolume;
    int  aacVolume;
    File filesDir     = null;
    File videoPcmFile = null;
    File aacPcmFile   = null;

    MediaMuxer outMp4MediaMuxer = null;

    // 输入视频分离器
    MediaExtractor inputMediaExtractor = null;
    int            inputVideoIndex     = -1;
    int            inputAudioIndex     = -1;
    MediaFormat    inputAudioFormat    = null;
    // 要混合进去的音频Format
    MediaFormat    addMediaFormat      = null;
    MediaFormat    outVideoFormat      = null;
    int            maxBufferSize       = 0;
    int            addAudioTrack       = -1;
    MediaExtractor additionalExtractor = null;
    private String wavAudioPath = "";
    MediaCodec encoder         = null;
    int        muxerAudioIndex = -1;
    private MediaCodec.BufferInfo bufferInfo = null;


    public MusicProcess(Context context, int startTime, int endTime, int videoVolume, int aacVolume) {
        this.context = context;
        this.startTimeUs = startTime;
        this.endTimeUs = endTime;
        this.videoVolume = videoVolume;
        this.aacVolume = aacVolume;

        filesDir = context.getFilesDir();
        //        下载下来的音乐转换城pcm
        aacPcmFile = new File(filesDir, "audio" + ".pcm");
        //        视频自带的音乐转换城pcm
        videoPcmFile = new File(filesDir, "video" + ".pcm");
    }


    public void mixAudioTrack(final String videoInput, final String audioInput, final String output) {
        Log.d(TAG, "mixAudioTrack: ");
        // /storage/emulated/0/input.mp4
        Log.i(TAG, "    videoInput : " + videoInput);
        // /storage/emulated/0/music.mp3
        Log.i(TAG, "    audioInput : " + audioInput);
        // /storage/emulated/0/剪辑后的_output.mp4
        Log.i(TAG, "    output     : " + output);

        checkAudioDuration(audioInput);
        try {
            MediaExtractor audioExtractor = new MediaExtractor();
            audioExtractor.setDataSource(audioInput);
            decodeToPCM(videoInput, videoPcmFile.getAbsolutePath());

            decodeToPCM(audioInput, aacPcmFile.getAbsolutePath());

            File adjustedPcm = new File(filesDir, "混合后的" + ".pcm");
            mixPcm(videoPcmFile.getAbsolutePath(), aacPcmFile.getAbsolutePath(), adjustedPcm.getAbsolutePath(), videoVolume, aacVolume);
            File wavFile = new File(filesDir, adjustedPcm.getName() + ".wav");
            this.wavAudioPath = wavFile.getAbsolutePath();


            PcmToWavUtil pcmToWavUtil = new PcmToWavUtil(44100, AudioFormat.CHANNEL_IN_STEREO, 2, AudioFormat.ENCODING_PCM_16BIT);

            pcmToWavUtil.pcmToWav(adjustedPcm.getAbsolutePath(), wavAudioPath);
            Log.i(TAG, "    转换完毕");
            //混音的wav文件   + 视频文件   ---》  生成
            mixVideoAndMusic(videoInput, output);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void mixVideoAndMusic(String videoInputPath, String outputMp4Path) {
        Log.d(TAG, "mixVideoAndMusic: ");
        initInputMediaExtractor(videoInputPath);
        initOutMp4MediaMuxer(outputMp4Path);
        addOutVideoFormatToMediaMuxer();
        addOutAudioFormatToMediaMuxer();
        initAdditionalMediaExtractor();
        initMediaCodec();
        configEncodeAudioFormat();
        processAudio();
        //视频
        processVideo();
        startRelease();
    }


    /**
     * 1: 创建 原视频的 分离器
     * 2: 获取原视频的中，视频流和音频流的下标
     *
     * @param videoInputPath
     */
    private void initInputMediaExtractor(String videoInputPath) {
        Log.d(TAG, "initInputMediaExtractor: ");
        Log.i(TAG, "    initInputMediaExtractor: videoInputPath :" + videoInputPath);
        try {
            //  一个轨道    既可以装音频 又视频
            //  取音频轨道  wav文件取配置信息
            //  先取视频
            inputMediaExtractor = new MediaExtractor();
            inputMediaExtractor.setDataSource(videoInputPath);
            //  拿到视频轨道的索引
            inputVideoIndex = selectTrack(inputMediaExtractor, false);
            inputAudioIndex = selectTrack(inputMediaExtractor, true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建MP4视频混合器
     *
     * @param outputMp4Path
     */
    private void initOutMp4MediaMuxer(String outputMp4Path) {
        Log.d(TAG, "initOutMp4MediaMuxer: ");
        Log.i(TAG, "    outputMp4Path    : " + outputMp4Path);
        // 初始化一个视频封装容器
        try {
            outMp4MediaMuxer = new MediaMuxer(outputMp4Path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (outMp4MediaMuxer == null) {
            Log.e(TAG, "    outMp4MediaMuxer is null.");
        }
    }

    /**
     * 1: 获取输入视频的  MediaFormat ，并添加到混合器中
     */
    private void addOutVideoFormatToMediaMuxer() {
        Log.d(TAG, "addMediaFormatByMediaMuxer: ");
        // 视频配置 文件
        outVideoFormat = inputMediaExtractor.getTrackFormat(inputVideoIndex);
        // 混合器添加 videoFormat
        outMp4MediaMuxer.addTrack(outVideoFormat);
    }

    /**
     * 1: 获取原视频流中的 音频MediaFormat ,并添加到混合器中
     * 2: 利用混合器，返回，混合器中音频的下标 ： muxerAudioIndex
     * 3: 启动混合器
     */
    private void addOutAudioFormatToMediaMuxer() {
        Log.d(TAG, "addOutAudioFormatToMediaMuxer: ");
        // 视频中音频轨道   应该取自于原视频的音频参数
        inputAudioFormat = inputMediaExtractor.getTrackFormat(inputAudioIndex);
        if (inputAudioFormat == null) {
            Log.e(TAG, "    inputAudioFormat is null.");
            return;
        }

        String originalMineType = inputAudioFormat.getString(MediaFormat.KEY_MIME);
        Log.i(TAG, "    originalMineType : " + originalMineType);
        inputAudioFormat.setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_AUDIO_AAC);
        // 添加一个空的轨道  轨道格式取自 视频文件，跟视频所有信息一样
        muxerAudioIndex = outMp4MediaMuxer.addTrack(inputAudioFormat);
        //  音频轨道开辟好了  输出开始工作
        outMp4MediaMuxer.start();
    }

    private void initAdditionalMediaExtractor() {
        Log.d(TAG, "initAdditionalMediaExtractor: ");
        Log.i(TAG, "    wavAudioPath : " + wavAudioPath);

        additionalExtractor = new MediaExtractor();
        try {
            additionalExtractor.setDataSource(wavAudioPath);

            int audioTrack = selectTrack(additionalExtractor, true);
            additionalExtractor.selectTrack(audioTrack);
            addMediaFormat = additionalExtractor.getTrackFormat(audioTrack);

            //最大一帧的 大小
            if (inputAudioFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                maxBufferSize = addMediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
            } else {
                maxBufferSize = 100 * 1000;
            }
        } catch (Exception e) {
            Log.e(TAG, "    initAdditionalMediaExtractor: ERROR : " + e.getMessage());
            e.printStackTrace();
        }


    }

    /**
     * 1: 创建音频编码器
     */
    private void initMediaCodec() {
        Log.d(TAG, "initMediaCodec: ");
        try {
            encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
            bufferInfo = new MediaCodec.BufferInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 1：配置编码器的audioFormat
     * 2： 启动编码器
     */
    private void configEncodeAudioFormat() {
        Log.d(TAG, "configEncodeAudioFormat: ");
        //最终输出   后面   混音   -----》     重采样   混音
        //参数对应-> mime type、采样率、声道数
        MediaFormat encodeFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, 44100, 2);
        // 比特率
        int audioBitrate = inputAudioFormat.getInteger(MediaFormat.KEY_BIT_RATE);
        encodeFormat.setInteger(MediaFormat.KEY_BIT_RATE, audioBitrate);
        // 音质等级
        encodeFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        //
        encodeFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, maxBufferSize);
        //  配置AAC 参数  编码 pcm   重新编码     视频文件变得更小
        encoder.configure(encodeFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        // 启动码编码器
        encoder.start();

    }

    private void processAudio() {
        Log.d(TAG, "processAudio: ");
        if (encoder == null) {
            Log.e(TAG, "    the encoder is NULL.");
            return;
        }

        // 容器
        ByteBuffer buffer  = ByteBuffer.allocateDirect(maxBufferSize);
        int        timeout = 1000;

        boolean encodeDone = false;
        while (!encodeDone) {
            int inputBufferIndex = encoder.dequeueInputBuffer(timeout);
            if (inputBufferIndex >= 0) {
                long sampleTime = additionalExtractor.getSampleTime();
                if (sampleTime < 0) {
                    // pts小于0  来到了文件末尾 通知编码器  不用编码了
                    encoder.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                } else {
                    int flags = additionalExtractor.getSampleFlags();
                    //  混合器 将
                    int size = additionalExtractor.readSampleData(buffer, 0);
                    //       编辑     行 1 还是不行 2   不要去用  空的
                    ByteBuffer inputBuffer = encoder.getInputBuffer(inputBufferIndex);
                    inputBuffer.clear();
                    inputBuffer.put(buffer);
                    inputBuffer.position(0);

                    encoder.queueInputBuffer(inputBufferIndex, 0, size, sampleTime, flags);
                    // 读完这一帧
                    additionalExtractor.advance();
                }
            }
            // 获取编码完的数据

            int outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, timeout);
            // 循环从MediaCodec 上获取数据
            while (outputBufferIndex >= 0) {
                if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                    encodeDone = true;
                    break;
                }
                ByteBuffer encodeOutputBuffer = encoder.getOutputBuffer(outputBufferIndex);
                // 往混合器上音频轨道上写音频数据
                outMp4MediaMuxer.writeSampleData(muxerAudioIndex, encodeOutputBuffer, bufferInfo);
                encodeOutputBuffer.clear();
                encoder.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, timeout);
            }
        }
    }

    /**
     * 1： 通过编码器，将 分离器中的 视频数据写入混合器中的视频轨道中
     */
    private void processVideo() {
        Log.d(TAG, "processVideo: ");
        //    把音频添加好了
        if (addAudioTrack >= 0) {
            this.inputMediaExtractor.unselectTrack(addAudioTrack);
        }

        int        maxBufferSize = outVideoFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
        ByteBuffer buffer        = ByteBuffer.allocateDirect(maxBufferSize);
        inputMediaExtractor.selectTrack(inputVideoIndex);
        inputMediaExtractor.seekTo(startTimeUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);

        //添加视频轨道信息 到封装容器
        while (true) {
            long sampleTimeUs = inputMediaExtractor.getSampleTime();
            if (sampleTimeUs == -1) {
                break;
            }
            if (sampleTimeUs < startTimeUs) {
                inputMediaExtractor.advance();
                continue;
            }
            if (sampleTimeUs > endTimeUs) {
                break;
            }
            // pts      0
            bufferInfo.presentationTimeUs = sampleTimeUs - startTimeUs + 600;
            bufferInfo.flags = inputMediaExtractor.getSampleFlags();
            // 读取视频文件的数据  画面 数据
            bufferInfo.size = inputMediaExtractor.readSampleData(buffer, 0);
            if (bufferInfo.size < 0) {
                break;
            }
            // // 往混合器上音频轨道上写视频数据
            outMp4MediaMuxer.writeSampleData(inputVideoIndex, buffer, bufferInfo);
            inputMediaExtractor.advance();
        }

    }

    private void startRelease() {
        Log.d(TAG, "startRelease: ");
        try {
            additionalExtractor.release();
            inputMediaExtractor.release();
            if (encoder != null) {
                encoder.stop();
                encoder.release();
            }

            outMp4MediaMuxer.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkAudioDuration(String audioInput) {
        Log.d(TAG, "checkAudioDuration: ");
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(audioInput);
        // 读取音乐时间
        final int aacDurationMs = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        Log.i(TAG, "    aacDurationMs : " + aacDurationMs);
        mediaMetadataRetriever.release();
    }

    private static float normalizeVolume(int volume) {
        return volume / 100f * 1;
    }

    public void mixPcm(String pcm1Path, String pcm2Path, String toPath, int vol1, int vol2) throws IOException {
        Log.d(TAG, "mixPcm: ");
        Log.i(TAG, "    pcm1Path : "+pcm1Path);
        Log.i(TAG, "    pcm2Path : "+pcm2Path);
        Log.i(TAG, "    toPath   : "+toPath);
        float  volume1 = normalizeVolume(vol1);
        float  volume2 = normalizeVolume(vol2);
        byte[] buffer1 = new byte[2048];
        byte[] buffer2 = new byte[2048];
        byte[] buffer3 = new byte[2048];

        FileInputStream is1 = new FileInputStream(pcm1Path);
        FileInputStream is2 = new FileInputStream(pcm2Path);

        FileOutputStream fileOutputStream = new FileOutputStream(toPath);

        boolean end1 = false, end2 = false;
        short   temp2, temp1;
        int     temp;
        try {
            while (!end1 || !end2) {
                if (!end1) {
                    end1 = (is1.read(buffer1) == -1);

                    System.arraycopy(buffer1, 0, buffer3, 0, buffer1.length);
                }
                if (!end2) {
                    end2 = (is2.read(buffer2) == -1);
                    int           voice         = 0;
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < buffer2.length; i += 2) {
                        temp1 = (short) ((buffer1[i] & 0xff) | (buffer1[i + 1] & 0xff) << 8);
                        stringBuilder.append(temp1 + " ");
                        temp2 = (short) ((buffer2[i] & 0xff) | (buffer2[i + 1] & 0xff) << 8);
                        temp = (int) (temp2 * volume2 + temp1 * volume1);
                        if (temp > 32767) {
                            temp = 32767;
                        } else if (temp < -32768) {
                            temp = -32768;
                        }
                        buffer3[i] = (byte) (temp & 0xFF);
                        buffer3[i + 1] = (byte) ((temp >>> 8) & 0xFF);
                    }
                    //  Log.i(TAG, "mixPcm: " + stringBuilder.toString());
                }
                fileOutputStream.write(buffer3);
            }
        } finally {
            is1.close();
            is2.close();
            fileOutputStream.close();
        }
    }

    /**
     * MP3 截取并且输出  pcm
     * @param musicPath
     * @param outPath
     * @throws Exception
     */
    public void decodeToPCM(String musicPath, String outPath) throws Exception {
        Log.d(TAG, "decodeToPCM: ");
        if (endTimeUs < startTimeUs) {
            return;
        }
        Log.i(TAG, "    musicPath : "+musicPath);
        Log.i(TAG, "    outPath   : "+outPath);

        MediaExtractor mediaExtractor = new MediaExtractor();

        mediaExtractor.setDataSource(musicPath);
        int audioTrack = selectTrack(mediaExtractor, true);

        mediaExtractor.selectTrack(audioTrack);
        // 视频 和音频
        mediaExtractor.seekTo(startTimeUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
        // 轨道信息  都记录 编码器
        MediaFormat audioFormat   = mediaExtractor.getTrackFormat(audioTrack);
        int         maxBufferSize = 100 * 1000;
        if (audioFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
            maxBufferSize = audioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
        } else {
            maxBufferSize = 100 * 1000;
        }
        ByteBuffer buffer = ByteBuffer.allocateDirect(maxBufferSize);
        //        h264   H265  音频
        MediaCodec mediaCodec = MediaCodec.createDecoderByType(audioFormat.getString((MediaFormat.KEY_MIME)));
        //        设置解码器信息    直接从 音频文件
        mediaCodec.configure(audioFormat, null, null, 0);
        File        pcmFile      = new File(outPath);
        FileChannel writeChannel = new FileOutputStream(pcmFile).getChannel();
        mediaCodec.start();
        MediaCodec.BufferInfo info              = new MediaCodec.BufferInfo();
        int                   outputBufferIndex = -1;
        while (true) {
            int decodeInputIndex = mediaCodec.dequeueInputBuffer(1000);
            if (decodeInputIndex >= 0) {
                long sampleTimeUs = mediaExtractor.getSampleTime();

                if (sampleTimeUs == -1) {
                    break;
                } else if (sampleTimeUs < startTimeUs) {
                    //                    丢掉 不用了
                    mediaExtractor.advance();
                    continue;
                } else if (sampleTimeUs > endTimeUs) {
                    break;
                }
                //                获取到压缩数据
                info.size = mediaExtractor.readSampleData(buffer, 0);
                info.presentationTimeUs = sampleTimeUs;
                info.flags = mediaExtractor.getSampleFlags();

                //                下面放数据  到dsp解码
                byte[] content = new byte[buffer.remaining()];
                buffer.get(content);
                //                输出文件  方便查看
                //                FileUtils.writeContent(content);
                //                解码
                ByteBuffer inputBuffer = mediaCodec.getInputBuffer(decodeInputIndex);
                inputBuffer.put(content);
                mediaCodec.queueInputBuffer(decodeInputIndex, 0, info.size, info.presentationTimeUs, info.flags);
                //                释放上一帧的压缩数据
                mediaExtractor.advance();
            }

            outputBufferIndex = mediaCodec.dequeueOutputBuffer(info, 1_000);
            while (outputBufferIndex >= 0) {
                ByteBuffer decodeOutputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex);
                writeChannel.write(decodeOutputBuffer);//MP3  1   pcm2
                mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = mediaCodec.dequeueOutputBuffer(info, 1_000);
            }
        }
        writeChannel.close();
        mediaExtractor.release();
        mediaCodec.stop();
        mediaCodec.release();
        //        转换MP3    pcm数据转换成mp3封装格式
        //
        //        File wavFile = new File(Environment.getExternalStorageDirectory(),"output.mp3" );
        //        new PcmToWavUtil(44100,  AudioFormat.CHANNEL_IN_STEREO,
        //                2, AudioFormat.ENCODING_PCM_16BIT).pcmToWav(pcmFile.getAbsolutePath()
        //                , wavFile.getAbsolutePath());

    }

    public static int selectTrack(MediaExtractor extractor, boolean audio) {
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String      mime   = format.getString(MediaFormat.KEY_MIME);
            if (TextUtils.isEmpty(mime)) {
                continue;
            }

            if (audio) {
                if (mime.startsWith("audio/")) {
                    return i;
                }
            } else {
                if (mime.startsWith("video/")) {
                    return i;
                }
            }


        }
        return -5;
    }
}
