package com.android.withCamera.square;

public class Square {
    // 正方形的顶点坐标
    private final float squareCoords[] = {
            -0.5f, 0.5f, 0.0f, // top left
            -0.5f, -0.5f, 0.0f, // bottom left
            0.5f, -0.5f, 0.0f, // bottom right
            0.5f, 0.5f, 0.0f }; // top right

    // 顶点的绘制顺序
    private final short drawOrder[] = { 0, 1, 2, 0, 2, 3 };

    // 设置每个顶点对应的颜色，这里设置为白色
    private final float color[] = { 1.0f, 1.0f, 1.0f, 1.0f };
    //顶点
    private final String ver="attribute vec4 aPosition;" +
            "uniform mat4 uMatrix;" +
            "varying  vec4 vColor;" +
            "attribute vec4 aColor;" +
            "void main() {" +
            "  gl_Position = uMatrix*aPosition;" +
            "  vColor=aColor;" +
            "}";
    //片元
    private final String frag = "precision mediump float;" +//片元一定要指定精度
            "varying vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}";

}
