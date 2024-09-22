uniform mat4 textureTransform;
//NDK坐标点（attribute:定义一个输入变量，以便在顶点着色器中接收顶点位置数据，并进行进一步处理）
attribute vec4 position;
attribute vec2 inputTextureCoordinate;
//传给片段着色器的纹理坐标（纹理坐标点变换后输出）
varying   vec2 textureCoordinate;

 void main() {
     gl_Position = position;
     textureCoordinate = inputTextureCoordinate;
 }