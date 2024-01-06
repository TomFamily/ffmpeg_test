package com.example.android_media_lib

// AudioTrack：https://www.cnblogs.com/renhui/p/7463287.html
/**
 * 播放：
 * MediaPlayer 更加适合在后台长时间播放本地音乐文件或者在线的流式资源;
 * SoundPool   则适合播放比较短的音频片段，比如游戏声音、按键声、铃声片段等等，它可以同时播放多个音频;
 * AudioTrack  则更接近底层，提供了非常强大的控制能力，支持低延迟播放，适合流媒体和VoIP语音电话等场景。
 *
 * AudioTrack 只能播放已经解码的PCM流，如果对比支持的文件格式的话则是AudioTrack只支持wav格式的音频文件，
 * 因为wav格式的音频文件大部分都是PCM流。AudioTrack不创建解码器，所以只能播放不需要解码的wav文件。
 * 每一个音频流对应着一个AudioTrack类的一个实例，每个AudioTrack会在创建时注册到 AudioFlinger中，
 * 由AudioFlinger把所有的AudioTrack进行混合（Mixer），然后输送到AudioHardware中进行播放，目前Android
 * 同时最多可以创建32个音频流，也就是说，Mixer最多会同时处理32个AudioTrack的数据流。
 */

/**
 * 录制：
 *
 * MediaRecorder：更易于使用，特别是对于简单的音视频录制任务。你只需要设置一些参数，然后调用相应的方法即可。可以直
 *                接将录制的音频保存为常见的多媒体格式，如AAC、3GP等。适用于直接生成可以在多种设备上播放的音视频文件。
 *
 * AudioRecord： 更为底层，需要你处理更多的细节，如音频缓冲区、采样率、位深度等。适用于需要更多控制权和高度定制的场景。
 *               提供的是原始的 PCM 数据，你需要自己处理这些数据并选择保存或者进行进一步的编码。
 */

/**
 * Android 原生支持的音频录制格式只有 WAV；若需要将音频数据编码成MP3，你需要使用第三方库或库函数，
 * 因为Android SDK本身并没有原生支持MP3编码。
 *
 * WAV（Waveform Audio File Format）是一种音频文件格式，通常包含未压缩的音频数据。
 * WAV 文件可以存储不同格式的音频，包括 PCM（脉冲编码调制）格式，这是一种未经压缩的原始音频格式。
 * 在绝大多数情况下，WAV 文件中的音频数据是未经压缩的，即每个音频样本都以原始的 PCM 格式存储。这种未经压缩
 * 的存储方式保留了音频的原始质量，但相应地占用更多的存储空间。虽然 WAV 文件通常以未经压缩的形式存储音频数
 * 据，但有时也可以使用某些压缩算法，如PCM 压缩，但这并不常见。通常，如果你需要压缩音频文件以减小文件大小，
 * 你可能会选择使用其他压缩格式，如MP3、AAC、或者其他高效的音频压缩算法。总的来说，WAV 文件通常用于存储无
 * 损的、未经压缩的音频数据，以保留最高质量的音频。如果你对文件大小有要求，而又不愿意损失音频质量，你可能需要
 * 考虑使用其他压缩格式。
 */

/**
 * PCM（Pulse Code Modulation）是一种原始音频数据格式。PCM 是一种数字音频表示方法，它直接记录声音波形的
 * 振幅值，并将其转换为数字信号，用于数字化音频。
 *
 * AAC（Advanced Audio Coding 高级音频编码）是一种音频编解码器，通常用于对音频进行压缩。AAC 是一种有损压缩格式，
 * 它旨在提供高质量的音频压缩，同时保持相对较小的文件大小。
 */