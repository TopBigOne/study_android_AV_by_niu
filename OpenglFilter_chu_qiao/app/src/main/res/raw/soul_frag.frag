//纹理坐标系  1  世界坐标系  2
//   纹理   当前上色点的坐标  与顶点shader中的 ： "  aCoord"  对应起来
varying highp vec2 aCoord;
// 2d 采样器: 指的是一个2D 纹理数据的数组
uniform sampler2D vTexture;
// 放大系数 ：1：不断的变化    2：cpu 传进来的     3：增加 ，4：scalePercent  >1
uniform highp float scalePercent;

// 混合透明度 规律 ： 由大变小
uniform lowp float mixturePercent;
void main() {

    //中心点，所有的点，都与中心点center 作比较
    highp vec2 center = vec2(0.5, 0.5);
    //临时变量 textureCoordinateToUse 接收 aCoord   [0.6,0.6]  textureCoordinateToUse  ？
    highp vec2 textureCoordinateToUse = aCoord;
    //  假设：[0.6,0.6]  -  [0.5, 0.5]   =    [0.1, 0.1]
    textureCoordinateToUse -= center;
    //采样点 一定比  需要渲染 的坐标点要小     先看 y 轴

    // [0,-0.1]  /  1.1   =  [0, -0.09]
    // textureCoordinateToUse 的值在变小
    textureCoordinateToUse = textureCoordinateToUse / scalePercent;

    //[0, -0.09]   +  [0.5,0.5] =[0.5, 0.41]    ,0.41    实际是变大
    textureCoordinateToUse += center;
    //    [0.5,0.6]
    //    [0.5,0.6]
    //    gl_FragColor= texture2D(vTexture,aCoord);
    //    [0.5,0.59]   //原来绘制颜色
    lowp vec4 textureColor = texture2D(vTexture, aCoord);
    //      新采样颜色
    lowp vec4 textureColor2 = texture2D(vTexture, textureCoordinateToUse);

    // 通过mix() 函数，将 textureColor 和textureColor2 这两种颜色，进行混合；
    // 混合系数是：mixturePercent： 一个越来越小的值；
    gl_FragColor = mix(textureColor, textureColor2, mixturePercent);

}