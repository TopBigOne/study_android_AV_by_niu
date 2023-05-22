varying highp vec2 textureCoordinate;
uniform sampler2D inputImageTexture;
void main() {
    // 内置变量
    gl_FragColor = texture2D(inputImageTexture, textureCoordinate);
}
