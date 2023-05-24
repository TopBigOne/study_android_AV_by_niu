// vertex shader
// 变量 float[4]  一个顶点  java传过来的
attribute vec4 vPosition;

// attritude：一般用于各个顶点各不相同的量。如顶点颜色、坐标等。
// 纹理坐标
attribute vec2 vCoord;

// varying：表示易变量，一般用于 顶点着色器传递到片元着色器的量。
// varying  给  fragment shader 传递的变量
varying vec2 aCoord;

void main() {
    // 内置变量：
    gl_Position = vPosition;
    // 目前坐标值  传递给
    aCoord = vCoord.xy;
}