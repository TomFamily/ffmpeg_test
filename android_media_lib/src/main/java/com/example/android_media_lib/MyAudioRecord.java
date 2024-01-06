package com.example.android_media_lib;

import static com.example.base.utils.YKFileUtilsKt.createFile;
import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

// 参考链接：https://www.cnblogs.com/renhui/p/7457321.html
public class MyAudioRecord {
    private static final String TAG = "WavRecorder";

    /**
     * 采样率，人类能听到的频率范围 20 Hz到20000 Hz；所以 44100 Hz的采样率足以捕捉到人类能听到的频率范围
     */
    private static final int SAMPLE_RATE = 44100;
    /**
     * 声道数
     */
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    /**
     * 表示：音频样本以 16 位的线性 PCM（脉冲编码调制）格式进行编码
     * PCM 是一种用于数字音频表示的编码方式，它将模拟音频信号转换为数字形式。在 PCM 中，
     * 音频波形被分割成一系列的小时间片（采样），每个时间片用数字值表示。
     */
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    /**
     * AudioRecord 能接受的最小的buffer大小
     */
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);

    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private FileOutputStream fileOutputStream;

    @SuppressLint("MissingPermission")
    public void startRecording(String dir, String fileName) {
        try {
            File file = createFile(dir, fileName);
            if (file == null) return;
            Log.d(TAG, "startRecording: " + file.getPath());
            fileOutputStream = new FileOutputStream(file.getPath());
            // 当你在 AudioRecord 中选择 AudioSource.VOICE_DOWNLINK 作为音频源时，
            // 系统会尝试捕获远程通信对端发送给设备的语音信号，而不是捕获设备自身麦克风的声音。
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE);
            audioRecord.startRecording();

            isRecording = true;

            new Thread(() -> {
                try {
                    writeWavHeader(fileOutputStream, BUFFER_SIZE);
                    byte[] buffer = new byte[BUFFER_SIZE];

                    while (isRecording) {
                        int bytesRead = audioRecord.read(buffer, 0, BUFFER_SIZE);
                        fileOutputStream.write(buffer, 0, bytesRead);
                    }

                    audioRecord.stop();
                    fileOutputStream.close();
                    updateWavHeader(file.getPath());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        isRecording = false;
        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
        }
    }

    private void writeWavHeader(FileOutputStream outputStream, int bufferSize) throws IOException {
        // WAV文件头部的相关信息，根据需要设置
        short channels = 1;
        int bitsPerSample = 16;
        int byteRate = SAMPLE_RATE * channels * bitsPerSample / 8;

        byte[] header = new byte[44];

        // RIFF标识：标志着这是一个 WAV 文件
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';

        /**
         * 文件大小：用 4 个字节来表示，存储的是整个文件的大小（不包括前 8 个字节）
         *
         * bufferSize 是一个整数变量，表示音频数据的大小或者整个文件的大小（取决于上下文，这里可能是整个文件的大小）。
         * & 运算符是位与运算符，它用于按位与操作。
         * 0xff 是一个 8 位二进制数，所有位都被设置为 1。这个数常用于将其他数的高 24 位清零，保留低 8 位的值。
         * (bufferSize & 0xff) 表示将 bufferSize 的所有位与上述 0xff 进行位与操作，这样可以得到 bufferSize 的最低 8 位的值。
         */
        header[4] = (byte) (bufferSize & 0xff);
        // >> 是右移位运算符，它将二进制表示的数向右移动指定的位数。这样可以得到 bufferSize 的次低 8 位的值。
        header[5] = (byte) ((bufferSize >> 8) & 0xff);
        header[6] = (byte) ((bufferSize >> 16) & 0xff);
        header[7] = (byte) ((bufferSize >> 24) & 0xff);

        // WAVE标识：表示这是一个 WAV 文件格式
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';

        // 'fmt ' chunk：包含了格式信息，比如 PCM 编码、声道数、采样率、比特率等。
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';

        // 数据长度：用 4 个字节表示音频数据的长度。
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;

        header[20] = 1; // format = 1 for PCM
        header[21] = 0;

        // 声道数
        header[22] = (byte) channels;
        header[23] = 0;

        // 采样率
        header[24] = (byte) (SAMPLE_RATE & 0xff);
        header[25] = (byte) ((SAMPLE_RATE >> 8) & 0xff);
        header[26] = (byte) ((SAMPLE_RATE >> 16) & 0xff);
        header[27] = (byte) ((SAMPLE_RATE >> 24) & 0xff);

        // 比特率：用来描述音频数据的特性
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);

        header[32] = (byte) (channels * bitsPerSample / 8); // block align
        header[33] = 0;

        header[34] = (byte) bitsPerSample;
        header[35] = 0;

        // 'data' 标识：表示接下来是音频数据
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';

        /**
         * 音频数据长度：用 4 个字节表示音频数据的长度
         */
        header[40] = (byte) (bufferSize & 0xff);
        header[41] = (byte) ((bufferSize >> 8) & 0xff);
        header[42] = (byte) ((bufferSize >> 16) & 0xff);
        header[43] = (byte) ((bufferSize >> 24) & 0xff);

        outputStream.write(header, 0, 44);
    }

    /**
     * 放子线程执行
     */
    private void updateWavHeader(String filePath) throws IOException {
        // 更新WAV文件头部的数据大小信息
        byte[] data = new byte[BUFFER_SIZE];
        FileInputStream inputStream = new FileInputStream(filePath);
        int totalBytes = 0;

        while (inputStream.read(data) != -1) {
            totalBytes += BUFFER_SIZE;
        }

        inputStream.close();

        RandomAccessFile wavFile = new RandomAccessFile(filePath, "rw");
        wavFile.seek(4);
        wavFile.write((totalBytes + 36) & 0xff);
        wavFile.write(((totalBytes + 36) >> 8) & 0xff);
        wavFile.write(((totalBytes + 36) >> 16) & 0xff);
        wavFile.write(((totalBytes + 36) >> 24) & 0xff);

        wavFile.seek(40);
        wavFile.write(totalBytes & 0xff);
        wavFile.write((totalBytes >> 8) & 0xff);
        wavFile.write((totalBytes >> 16) & 0xff);
        wavFile.write((totalBytes >> 24) & 0xff);

        wavFile.close();
    }
}
