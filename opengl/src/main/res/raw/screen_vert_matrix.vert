attribute vec4 vPosition;
attribute vec2 fPosition;
varying vec2 ft_Position;
uniform mat4 u_Matrix;
void main(){
    // 将顶点的原始位置通过一个变换矩阵（u_Matrix）进行变换，得到顶点在裁剪空间中的位置，
    // 并将这个位置存储在gl_Position中。这是实现3D图形渲染中顶点变换的常见做法
    gl_Position = vPosition * u_Matrix;
    ft_Position = fPosition;
}