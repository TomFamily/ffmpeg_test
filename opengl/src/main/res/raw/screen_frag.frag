precision mediump float;
// varying关键字用于从顶点着色器传递插值数据到片段着色器
varying vec2 ft_Position;
uniform sampler2D vTexture;

void main(){
    // texture2D函数用于从vTexture纹理中采样颜色，使用ft_Position作为纹理坐标。采样结果（即纹理中对应坐标的颜色）
    // 被赋值给gl_FragColor，这是GLSL中预定义的输出变量，用于存储片段的最终颜色。
    gl_FragColor = texture2D(vTexture, ft_Position);
}