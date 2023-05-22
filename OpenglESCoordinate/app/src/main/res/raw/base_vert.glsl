attribute vec4 position;
attribute vec4 inputTextureCoordinate;
varying vec2 textureCoordinate;

void main() {
    gl_Position = position;
    // 将外部输入的纹理坐标`inputTextureCoordinate.xy`赋值给`textureCoordinate`, 即传递给Fragment Shader
    textureCoordinate = inputTextureCoordinate.xy;
}
