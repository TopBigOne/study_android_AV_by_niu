// 传递过来的纹理坐标
varying highp vec2 textureCoordinate;
// 纹理采样器 （获取对应的纹理ID）
uniform sampler2D inputImageTexture;
//片元      纹理坐标系  2
void main(){
    //将纹理颜色添加到对应的像素点上
    gl_FragColor = texture2D(inputImageTexture, textureCoordinate);
}