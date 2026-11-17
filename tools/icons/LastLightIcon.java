import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 * Draws TA-01 · Last Light's menu icon as pixel art (Adam, 2026-11-17: "redo it in pixel style"), in the style of the
 * mod icon ({@code ModIcon.java}) and TA-02's icon ({@code QuasarJetsArt.java}): 64x32 pixels shown at 4x = 256x128, 2:1
 * like the menu's other icons. The last flash of an evaporating black hole on dark space: a blue-white point with a
 * cross glint, the burst's ring, a fainter dotted ring further out, a thin horizontal streak, and Hawking pairs at the
 * horizon (blue falls in, gold escapes), with a few stars. Our own art, made by code.
 *
 * <p>Run (JDK 21) from the repo root: {@code java tools/icons/LastLightIcon.java}. It writes both copies the mod uses:
 * {@code assets/ton618/textures/gui/sprites/skill/ton_618/last_light.png} (TON 618's menu) and
 * {@code assets/shooting_star_demo/textures/gui/sprites/skill/stellar_remote/last_light.png} (the Stellar Remote's).
 * Other paths can be given instead.
 */
public final class LastLightIcon {
    private static final String RES = "src/main/resources/assets/";
    static final int W = 64;
    static final int H = 32;

    public static void main(String[] args) throws Exception {
        BufferedImage icon = upscale(icon(), 4);
        String[] outs = args.length > 0 ? args : new String[] {
            RES + "ton618/textures/gui/sprites/skill/ton_618/last_light.png",
            RES + "shooting_star_demo/textures/gui/sprites/skill/stellar_remote/last_light.png"};
        for (String path : outs) {
            File file = new File(path);
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            ImageIO.write(icon, "png", file);
            System.out.println("wrote " + file);
        }
    }

    private static BufferedImage icon() {
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        int cx = 32;
        int cy = 16;
        // deep space, a little blue toward the middle, dithered like the mod icon's sky
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                double d = Math.hypot((x - cx) / 32.0, (y - cy) / 16.0);
                double dither = (x + y) % 2 == 0 ? 1.12 : 0.9;
                int b = (int) ((26 - 15 * Math.min(1, d)) * dither);
                int g = (int) ((10 - 5 * Math.min(1, d)) * dither);
                set(img, x, y, 4, Math.max(5, g), Math.max(10, b));
            }
        }
        // stars, kept off the burst
        Random random = new Random(618);
        for (int i = 0; i < 30; i++) {
            int x = random.nextInt(W);
            int y = random.nextInt(H);
            if (Math.hypot((x - cx) / 2.0, y - cy) < 13) {
                continue;
            }
            int v = 80 + random.nextInt(130);
            set(img, x, y, v, v, Math.min(255, v + 35));
        }
        // the faint outer ring, dotted (every other pixel)
        ring(img, cx, cy, 14.0, 7.0, 0.55, 70, 95, 150, true);
        // the burst's ring, bright and pale blue
        ring(img, cx, cy, 9.0, 9.0, 0.6, 150, 200, 255, false);
        // a thin horizontal streak through the point, fading out
        for (int x = 0; x < W; x++) {
            int dx = Math.abs(x - cx);
            if (dx < 3 || dx > 26) {
                continue;
            }
            double k = 1.0 - dx / 27.0;
            mix(img, x, cy, 190, 220, 255, 0.85 * k);
        }
        // the glow round the point
        for (int y = cy - 4; y <= cy + 4; y++) {
            for (int x = cx - 5; x <= cx + 5; x++) {
                double d = Math.hypot((x - cx) * 0.9, y - cy);
                if (d <= 4.2) {
                    mix(img, x, y, 120, 170, 255, 0.75 * (1.0 - d / 4.6));
                }
            }
        }
        // the point and its cross glint
        for (int i = -3; i <= 3; i++) {
            int v = 255 - Math.abs(i) * 30;
            set(img, cx + i, cy, v, v, 255);
            set(img, cx, cy + i, v, v, 255);
        }
        set(img, cx, cy, 255, 255, 255);
        set(img, cx - 1, cy - 1, 215, 230, 255);
        set(img, cx + 1, cy - 1, 215, 230, 255);
        set(img, cx - 1, cy + 1, 215, 230, 255);
        set(img, cx + 1, cy + 1, 215, 230, 255);
        // Hawking pairs at the horizon: blue falls in, gold escapes
        double[] angles = {0.35, 1.2, 2.05, 2.75, 3.55, 4.35, 5.05, 5.75};
        for (double a : angles) {
            double c = Math.cos(a);
            double s = Math.sin(a);
            set(img, (int) Math.round(cx + c * 5.5), (int) Math.round(cy + s * 4.0), 90, 150, 255);
            set(img, (int) Math.round(cx + c * 11.5), (int) Math.round(cy + s * 7.0), 255, 196, 80);
        }
        return img;
    }

    /** A ring of pixels (an ellipse rx by ry), drawn over what is there; {@code dotted}: every other pixel. */
    private static void ring(BufferedImage img, int cx, int cy, double rx, double ry, double width, int r, int g, int b,
                             boolean dotted) {
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                double d = Math.hypot((x - cx) / rx, (y - cy) / ry);
                double edge = Math.abs(d - 1.0) * Math.min(rx, ry);
                if (edge <= width && (!dotted || (x + y) % 2 == 0)) {
                    set(img, x, y, r, g, b);
                }
            }
        }
    }

    private static void set(BufferedImage img, int x, int y, int r, int g, int b) {
        if (x >= 0 && y >= 0 && x < W && y < H) {
            img.setRGB(x, y, 0xFF000000 | clamp(r) << 16 | clamp(g) << 8 | clamp(b));
        }
    }

    /** Blends a colour over the pixel by {@code a} (0 to 1). */
    private static void mix(BufferedImage img, int x, int y, int r, int g, int b, double a) {
        if (x < 0 || y < 0 || x >= W || y >= H || a <= 0) {
            return;
        }
        int p = img.getRGB(x, y);
        double k = Math.min(1.0, a);
        set(img, x, y, (int) (((p >> 16) & 255) * (1 - k) + r * k), (int) (((p >> 8) & 255) * (1 - k) + g * k),
            (int) ((p & 255) * (1 - k) + b * k));
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    /** Nearest neighbour, so the pixels stay sharp. */
    private static BufferedImage upscale(BufferedImage src, int k) {
        BufferedImage out = new BufferedImage(src.getWidth() * k, src.getHeight() * k, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < out.getHeight(); y++) {
            for (int x = 0; x < out.getWidth(); x++) {
                out.setRGB(x, y, src.getRGB(x / k, y / k));
            }
        }
        return out;
    }
}
