import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 * Draws TA-01 · Last Light's menu icon (our own art, made by code): the last flash of an evaporating black hole on dark
 * space: a blue-white point, its glow, the burst's ring, a few Hawking pairs (blue falls in, gold escapes) and stars.
 * 2:1 like the menu's other icons.
 *
 * <p>Run (JDK 21): {@code java tools/icons/LastLightIcon.java <out.png> [more out.png ...]}. The mod's copies:
 * {@code assets/ton618/textures/gui/sprites/skill/ton_618/last_light.png} (TON 618's menu) and
 * {@code assets/shooting_star_demo/textures/gui/sprites/skill/stellar_remote/last_light.png} (the Stellar Remote's).
 */
public final class LastLightIcon {
    static final int W = 256;
    static final int H = 128;

    public static void main(String[] args) throws Exception {
        float[][] r = new float[H][W];
        float[][] g = new float[H][W];
        float[][] b = new float[H][W];
        double cx = W * 0.5;
        double cy = H * 0.5;

        // dark space, a little blue towards the middle
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                double d = Math.hypot((x - cx) / W, (y - cy) / H);
                float v = (float) Math.max(0.0, 0.10 - d * 0.16);
                r[y][x] = 0.010F + v * 0.25F;
                g[y][x] = 0.012F + v * 0.45F;
                b[y][x] = 0.030F + v * 0.95F;
            }
        }
        // stars
        Random random = new Random(618L);
        for (int i = 0; i < 70; i++) {
            int x = random.nextInt(W);
            int y = random.nextInt(H);
            float s = 0.25F + random.nextFloat() * random.nextFloat() * 0.75F;
            float warm = random.nextFloat();
            add(r, g, b, x, y, s * (0.8F + warm * 0.2F), s * 0.9F, s * (1.0F - warm * 0.25F));
            if (s > 0.75F) {
                for (int[] o : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                    add(r, g, b, x + o[0], y + o[1], s * 0.3F, s * 0.3F, s * 0.32F);
                }
            }
        }
        // the glow, the burst's ring and a faint outer ring, a thin horizontal streak
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                double dx = x + 0.5 - cx;
                double dy = (y + 0.5 - cy) * 1.0;
                double d = Math.hypot(dx, dy);
                double glow = Math.exp(-d * d / (2 * 13.0 * 13.0)) * 1.1 + Math.exp(-d * d / (2 * 34.0 * 34.0)) * 0.30;
                double ring = Math.exp(-Math.pow(d - 33.0, 2) / (2 * 1.6 * 1.6)) * 0.85
                    + Math.exp(-Math.pow(d - 33.0, 2) / (2 * 6.0 * 6.0)) * 0.22
                    + Math.exp(-Math.pow(d - 52.0, 2) / (2 * 2.2 * 2.2)) * 0.18;
                double streak = Math.exp(-dy * dy / (2 * 0.9 * 0.9)) * Math.exp(-Math.abs(dx) / 55.0) * 0.55;
                double core = Math.exp(-d * d / (2 * 2.6 * 2.6)) * 3.0;
                r[y][x] += (float) (glow * 0.62 + ring * 0.78 + streak * 0.70 + core);
                g[y][x] += (float) (glow * 0.82 + ring * 0.90 + streak * 0.85 + core);
                b[y][x] += (float) (glow * 1.00 + ring * 1.00 + streak * 1.00 + core);
            }
        }
        // Hawking pairs near the point: blue falls in, gold escapes
        Random pairs = new Random(100L);
        for (int i = 0; i < 9; i++) {
            double a = pairs.nextDouble() * Math.PI * 2.0;
            double at = 12.0 + pairs.nextDouble() * 7.0;
            double out = at + 7.0 + pairs.nextDouble() * 9.0;
            dot(r, g, b, cx + Math.cos(a) * (at - 4.0), cy + Math.sin(a) * (at - 4.0), 1.3, 0.35F, 0.6F, 1.0F);
            dot(r, g, b, cx + Math.cos(a) * out, cy + Math.sin(a) * out, 1.5, 1.0F, 0.72F, 0.28F);
        }

        BufferedImage image = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                image.setRGB(x, y, tone(r[y][x]) << 16 | tone(g[y][x]) << 8 | tone(b[y][x]));
            }
        }
        for (String path : args) {
            File file = new File(path);
            file.getParentFile().mkdirs();
            ImageIO.write(image, "png", file);
            System.out.println("wrote " + file);
        }
    }

    private static void add(float[][] r, float[][] g, float[][] b, int x, int y, float vr, float vg, float vb) {
        if (x >= 0 && y >= 0 && x < W && y < H) {
            r[y][x] += vr;
            g[y][x] += vg;
            b[y][x] += vb;
        }
    }

    private static void dot(float[][] r, float[][] g, float[][] b, double px, double py, double radius, float vr, float vg, float vb) {
        for (int y = (int) (py - 4); y <= (int) (py + 4); y++) {
            for (int x = (int) (px - 4); x <= (int) (px + 4); x++) {
                double d = Math.hypot(x + 0.5 - px, y + 0.5 - py);
                float k = (float) Math.exp(-d * d / (2 * radius * radius));
                add(r, g, b, x, y, vr * k, vg * k, vb * k);
            }
        }
    }

    /** Soft tone curve: bright parts roll off to white instead of clipping hard. */
    private static int tone(float v) {
        double t = 1.0 - Math.exp(-Math.max(0.0F, v) * 1.6);
        return (int) Math.round(Math.min(1.0, t * 1.04) * 255.0);
    }
}
