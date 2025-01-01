package com.android.opengl.watermark.bitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import com.android.opengl.base.egl.YGLSurfaceView;
import com.android.opengl.base.ShaderUtil;
import com.example.opengl.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * author : York
 * date   : 2020/12/20 0:55
 * desc   : 绘制图片纹理的 Render
 */
public class YBitmapRender implements YGLSurfaceView.YGLRender {

    private static final String TAG = "YBitmapRender";
    private final Context mContext;
    private final FloatBuffer vertexBuffer;
    private final FloatBuffer fragmentBuffer;
    private int program;
    private int vPosition;
    private int fPosition;
    private int []textureIds;
    private Bitmap bitmap;


    public YBitmapRender(Context context) {
        this.mContext = context;

        //顶点坐标
        float[] vertexData = {
                -1f, -1f,
                1f, -1f,
                -1f, 1f,
                1f, 1f
        };
        //读取顶点坐标
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertexData);
        vertexBuffer.position(0);

        //纹理坐标
        float[] fragmentData = {
                0f, 1f,
                1f, 1f,
                0f, 0f,
                1f, 0f
        };
        //读取纹理坐标
        fragmentBuffer = ByteBuffer.allocateDirect(fragmentData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(fragmentData);
        fragmentBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated() {
        //加载顶点着色器 shader
        String vertexSource = ShaderUtil.getRawResource(mContext, R.raw.screen_vert);
        //加载片元着色器 shader
        String fragmentSource = ShaderUtil.getRawResource(mContext, R.raw.screen_frag);
        //获取源程序
        program = ShaderUtil.createProgram(vertexSource, fragmentSource);
        //从渲染程序中得到着顶点色器中的属性
        vPosition = GLES20.glGetAttribLocation(program, "vPosition");
        //从渲染程序中得到片元着色器中的属性
        fPosition = GLES20.glGetAttribLocation(program, "fPosition");

        //创建 1个纹理,放入到 int [] textureIds, 一共有 30多个 纹理
        textureIds = new int[1];
        /**
         * 第一个参数：这个参数表示要生成的纹理对象的数量。也就是说，如果你希望生成多个纹理，var0 就是你希望生成的数量。
         */
        GLES20.glGenTextures(1, textureIds, 0);//第三个参数是指从哪儿开始取
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0]);

        //设置纹理的环绕方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        //设置纹理的过滤方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        //解绑纹理 指的是离开对 纹理的配置，进入下一个状态
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        //获取图片的 bitmap
        bitmap = BitmapFactory.decodeResource(mContext.getResources(), com.example.base.R.drawable.ic_launcher2);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        //设置窗口大小
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame() {
        Log.d(TAG, "onDrawFrame: ");
        //清除屏幕，此处用的是红色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1f,0f, 0f, 1f);

        //使用着色器源程序
        GLES20.glUseProgram(program);

        //使能顶点属性数组，使之有效
        GLES20.glEnableVertexAttribArray(vPosition);
        //使能之后，为顶点属性赋值，绑定顶点坐标
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer);

        //使能片元属性数组，使之有效
        GLES20.glEnableVertexAttribArray(fPosition);
        //使能之后，为片元属性赋值，绑定纹理坐标
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, fragmentBuffer);

        //要开始绘制纹理了，激活纹理 0号， 之所以激活 0号，是因为在没设置点的情况下默认是 0号
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //绑定 textureIds[0] 到已激活的 2D纹理 GL_TEXTURE0上（该函数用于将一个纹理对象绑定到当前的纹理单元。在这里，GLES20.GL_TEXTURE_2D表示我们正在处理一个2D纹理。）
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0]);
        //绑定 bitmap 到textureIds[0]纹理
        // GLUtils.texImage2D用于将位图（bitmap）的图像数据上传到当前绑定的纹理对象。在这里，GLES20.GL_TEXTURE_2D再次表示我们处理的是2D纹理。
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        //绘制图形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        //解绑 2D纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

    }

    @Override
    public void surfaceDestroyed() {
        Log.d(TAG, "surfaceDestroyed: ");
        // bitmap.recycle();//用完及时回收
    }
}
