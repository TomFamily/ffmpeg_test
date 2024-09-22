#extension GL_OES_EGL_image_external : require
precision mediump float;
//外部纹理采样器
uniform samplerExternalOES videoTex;
varying vec2 textureCoordinate;

void main() {
    //获取此纹理（预览图像）对应坐标的颜色值
    vec4 tc = texture2D(videoTex, textureCoordinate);
    //求此颜色的灰度值
    float color = tc.r * 0.3 + tc.g * 0.59 + tc.b * 0.11;
    gl_FragColor = vec4(color,color,color,1.0);
}
