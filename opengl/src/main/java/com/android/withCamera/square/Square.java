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

    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "void main() {" +
                    " gl_Position = vPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    " gl_FragColor = vColor;" +
                    "}";

}
