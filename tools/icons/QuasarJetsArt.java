import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 * Draws TA-02 · Quasar Jets' pixel art (our own, made by code, in the SA Series' pixel style): the skill's menu icon
 * (64x32 pixels shown at 4x = 256x128, 2:1 like the menu's other icons: a star pulled into a ribbon, TON618 blazing,
 * its two jets) and the four frozen-light crystal textures (16x16, one per star type).
 *
 * <p>Run (JDK 21) from the repo root: {@code java tools/icons/QuasarJetsArt.java}. It writes into
 * {@code src/main/resources/assets/...} directly.
 */
public final class QuasarJetsArt {
    private static final String RES = "src/main/resources/assets/";

    public static void main(String[] args) throws Exception {
        BufferedImage icon = upscale(icon(), 4);
        write(icon, RES + "ton618/textures/gui/sprites/skill/ton_618/quasar_jets.png");
        write(icon, RES + "shooting_star_demo/textures/gui/sprites/skill/stellar_remote/quasar_jets.png");
        String[] names = {"red_dwarf", "sun_like", "blue_giant", "red_supergiant"};
        int[] colors = {0xFF7A4D, 0xFFE2A0, 0xA8C8FF, 0xFF9450};
        for (int i = 0; i < names.length; i++) {
            write(crystal(colors[i], i), RES + "ton618_addition/textures/block/frozen_light_" + names[i] + ".png");
        }
    }

    private static BufferedImage icon() {
        int w = 64;
        int h = 32;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Random random = new Random(618);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                // deep space, a little blue toward the middle
                double d = Math.hypot((x - 32) / 32.0, (y - 16) / 16.0);
                int b = (int) (22 - 12 * Math.min(1, d));
                img.setRGB(x, y, argb(255, 4, 5, Math.max(8, b)));
            }
        }
        for (int i = 0; i < 26; i++) {
            int x = random.nextInt(w);
            int y = random.nextInt(h);
            int v = 90 + random.nextInt(120);
            img.setRGB(x, y, argb(255, v, v, Math.min(255, v + 30)));
        }
        int cx = 34;
        int cy = 16;
        // the two jets, up and down from the poles, with knots
        for (int y = 0; y < h; y++) {
            int dy = Math.abs(y - cy);
            if (dy < 3) {
                continue;
            }
            boolean knot = (dy % 5) == 0;
            int core = knot ? 255 : 210;
            img.setRGB(cx, y, argb(255, core, core, 255));
            if (dy < 12 || knot) {
                img.setRGB(cx - 1, y, argb(255, 120, 160, 255));
                img.setRGB(cx + 1, y, argb(255, 120, 160, 255));
            }
        }
        // the disc, blazing blue-white, seen almost edge-on
        for (int x = cx - 11; x <= cx + 11; x++) {
            int dx = Math.abs(x - cx);
            int glow = 255 - dx * 9;
            img.setRGB(x, cy, argb(255, Math.max(120, glow), Math.max(160, glow), 255));
            if (dx < 8) {
                img.setRGB(x, cy - 1, argb(255, 150, 190, 255));
                img.setRGB(x, cy + 1, argb(255, 90, 130, 230));
            }
        }
        // the hole's shadow and its bright rim
        for (int y = cy - 3; y <= cy + 3; y++) {
            for (int x = cx - 3; x <= cx + 3; x++) {
                double r = Math.hypot(x - cx, y - cy);
                if (r <= 2.2) {
                    img.setRGB(x, y, argb(255, 0, 0, 0));
                } else if (r <= 3.3) {
                    img.setRGB(x, y, argb(255, 230, 240, 255));
                }
            }
        }
        // the star, pulled into a ribbon that winds into the disc (gold, from the left)
        for (int i = 0; i < 26; i++) {
            double u = i / 25.0;
            int x = (int) Math.round(6 + u * (cx - 9 - 6));
            int y = (int) Math.round(9 + Math.sin(u * Math.PI) * -4 + u * 6);
            int v = (int) (255 - u * 60);
            img.setRGB(x, y, argb(255, 255, v, 120));
            if (u < 0.35) {
                img.setRGB(x, y + 1, argb(255, 255, 160, 70));
            }
        }
        // the star itself, stretched
        for (int y = 7; y <= 11; y++) {
            for (int x = 3; x <= 8; x++) {
                double r = Math.hypot((x - 5.5) / 2.6, (y - 9) / 1.8);
                if (r <= 1.0) {
                    img.setRGB(x, y, r < 0.5 ? argb(255, 255, 240, 200) : argb(255, 255, 150, 70));
                }
            }
        }
        return img;
    }

    /** A cluster of three shards in the star's colour, on a clear background (drawn as a cross model). */
    private static BufferedImage crystal(int color, int seed) {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        shard(img, 8, 2, 15, 4, color);
        shard(img, 4, 7 + (seed % 2), 15, 3, color);
        shard(img, 12, 5 - (seed % 2), 15, 3, color);
        return img;
    }

    private static void shard(BufferedImage img, int cx, int top, int bottom, int width, int color) {
        int r = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int b = color & 255;
        int dark = argb(255, r * 55 / 100, g * 55 / 100, b * 55 / 100);
        int mid = argb(255, r, g, b);
        int light = argb(255, r + (255 - r) * 45 / 100, g + (255 - g) * 45 / 100, b + (255 - b) * 45 / 100);
        int white = argb(255, 250, 252, 255);
        for (int y = top; y <= bottom; y++) {
            // a point at the top, then the full width
            int half = Math.max(0, Math.min(width / 2, (y - top) / 2));
            for (int x = cx - half; x <= cx + half; x++) {
                if (x < 0 || x > 15) {
                    continue;
                }
                int c = x == cx - half || x == cx + half ? dark : x < cx ? light : mid;
                if (x == cx - 1 && y > top + 1 && y < top + 4) {
                    c = white;
                }
                img.setRGB(x, y, c);
            }
            if (half == 0 && cx >= 0 && cx <= 15) {
                img.setRGB(cx, y, light);
            }
        }
    }

    private static BufferedImage upscale(BufferedImage src, int k) {
        BufferedImage out = new BufferedImage(src.getWidth() * k, src.getHeight() * k, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < out.getHeight(); y++) {
            for (int x = 0; x < out.getWidth(); x++) {
                out.setRGB(x, y, src.getRGB(x / k, y / k));
            }
        }
        return out;
    }

    private static int argb(int a, int r, int g, int b) {
        return (a << 24) | (Math.min(255, r) << 16) | (Math.min(255, g) << 8) | Math.min(255, b);
    }

    private static void write(BufferedImage img, String path) throws Exception {
        File file = new File(path);
        file.getParentFile().mkdirs();
        ImageIO.write(img, "png", file);
        System.out.println("wrote " + path);
    }
}
