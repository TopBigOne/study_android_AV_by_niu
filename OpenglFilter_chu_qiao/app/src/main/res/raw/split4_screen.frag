precision mediump float;
varying vec2 aCoord;
uniform sampler2D vTexture;
void main() {
    float y = aCoord.y;
    float divide = 1.0 / 4.0;

    // 每一个分屏，都采样 [0.5,0.75]范围的像素
    // case 1:
    if (y < divide) {
        if (y == 0.0) {
            y = 0.75;
        } else {
            // y = [0.0,0.25] + 0.75
            y += divide * 3.0;
        }

    } else if (y > divide && y < 2.0 * divide) {
        // y = [0.25,0.5] + 0.5
        y += divide * 2.0;
    }

    else if (y > 2.0 * divide && y < divide * 3.0) {
        // [0.5,0.75] + 0.25
        y += divide;
    }
    else if (y >= divide * 3.0) {
        // [0.75,1]
        // y -= divide;
    }

    // 采样的坐标
    gl_FragColor = texture2D(vTexture, vec2(aCoord.x, y));

}


