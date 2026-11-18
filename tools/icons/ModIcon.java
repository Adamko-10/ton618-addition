import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * The project icon (CurseForge avatar), as 64x64 pixel art written out at 4x, nearest neighbour: in the same style as
 * the SA Series' icon (its {@code art/make_icon.js}): a dithered night sky, stars, Earth's limb along the bottom, the
 * "Addon" tag and a badge, here "TA". In the sky: TON618, its shadow, photon ring and accretion disk (the far side bent
 * over the top), Hawking pairs at its horizon (blue falls in, gold escapes, as on Last Light's menu icon) in Last
 * Light's pale blue glow; on the limb the Sun rising: the world comes back on a morning. Our own art, made by code.
 *
 * <p>Run (JDK 21): {@code java tools/icons/ModIcon.java [--scale=N] [out.png ...]} (default scale 4, 256x256; default
 * out {@code dev/publish/project-icon.png}).
 */
public final class ModIcon {
    static final int S = 64;
    static final double[][] IMG = new double[S * S][3];
    static int seed = 618;

    /** mulberry32, so the stars land in the same places every run. */
    static double rand() {
        seed += 0x6D2B79F5;
        int t = seed;
        t = (t ^ (t >>> 15)) * (1 | t);
        t = (t + ((t ^ (t >>> 7)) * (61 | t))) ^ t;
        return ((t ^ (t >>> 14)) & 0xFFFFFFFFL) / 4294967296.0;
    }

    static double[] rgb(int h) {
        return new double[] {(h >> 16) & 255, (h >> 8) & 255, h & 255};
    }

    static void blend(double fx, double fy, int hex, double a) {
        int x = (int) Math.round(fx);
        int y = (int) Math.round(fy);
        if (x < 0 || y < 0 || x >= S || y >= S || a <= 0) {
            return;
        }
        a = Math.min(1, a);
        double[] p = IMG[y * S + x];
        double[] c = rgb(hex);
        for (int i = 0; i < 3; i++) {
            p[i] = p[i] * (1 - a) + c[i] * a;
        }
    }

    static void put(double x, double y, int hex) {
        blend(x, y, hex, 1);
    }

    interface Pixel {
        void at(int x, int y);
    }

    static void each(Pixel f) {
        for (int y = 0; y < S; y++) {
            for (int x = 0; x < S; x++) {
                f.at(x, y);
            }
        }
    }

    /** A little pixel map: each character is a colour from the key, anything else is left alone. */
    static void stamp(int x0, int y0, String[] rows, char[] keys, int[] colours) {
        for (int dy = 0; dy < rows.length; dy++) {
            for (int dx = 0; dx < rows[dy].length(); dx++) {
                char ch = rows[dy].charAt(dx);
                for (int k = 0; k < keys.length; k++) {
                    if (keys[k] == ch) {
                        put(x0 + dx, y0 + dy, colours[k]);
                    }
                }
            }
        }
    }

    // Earth: a big circle whose top shows along the bottom (as the SA icon)
    static final double EX = 32, EY = 150, ER = 104;
    // TON618, upper left of centre
    static final double HX = 27, HY = 21, R = 7.6;
    // the Sun, rising on the limb at the left
    static final double SX = 13, SY = 48;

    static double earthDist(double x, double y) {
        return Math.hypot(x - EX, y - EY);
    }

    static double holeDist(double x, double y) {
        return Math.hypot(x - HX, y - HY);
    }

    public static void main(String[] args) throws Exception {
        int scale = 4;
        java.util.List<String> outs = new java.util.ArrayList<>();
        for (String arg : args) {
            if (arg.startsWith("--scale=")) {
                scale = Integer.parseInt(arg.substring(8));
            } else {
                outs.add(arg);
            }
        }
        if (outs.isEmpty()) {
            outs.add("dev/publish/project-icon.png");
        }
        draw();
        int w = S * scale;
        BufferedImage out = new BufferedImage(w, w, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < w; y++) {
            for (int x = 0; x < w; x++) {
                double[] p = IMG[(y / scale) * S + x / scale];
                int r = (int) Math.max(0, Math.min(255, Math.round(p[0])));
                int g = (int) Math.max(0, Math.min(255, Math.round(p[1])));
                int b = (int) Math.max(0, Math.min(255, Math.round(p[2])));
                out.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        for (String path : outs) {
            File file = new File(path);
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            ImageIO.write(out, "png", file);
            System.out.println("icon written to " + file.getPath() + " (" + w + "x" + w + ")");
        }
    }

    static void draw() {
        // the night sky, dithered (the SA icon's colours)
        double[] top = rgb(0x060A22);
        double[] bottom = rgb(0x1E2A66);
        each((x, y) -> {
            double k = y / (double) (S - 1);
            double d = (x + y) % 2 == 0 ? 1.1 : 0.92;
            for (int i = 0; i < 3; i++) {
                IMG[y * S + x][i] = (top[i] * (1 - k) + bottom[i] * k) * d;
            }
        });

        // stars, kept off TON618, the Sun and Earth
        for (int n = 0; n < 34; n++) {
            int x = (int) Math.floor(rand() * S);
            int y = (int) Math.floor(rand() * 46);
            boolean white = rand() < 0.3;
            if (earthDist(x, y) < ER + 3 || holeDist(x, y) < R + 9 || Math.abs(y - HY) < 2 && Math.abs(x - HX) < 26
                || Math.hypot(x - SX, y - SY) < 9) {
                continue;
            }
            put(x, y, white ? 0xFFFFFF : 0x9DB0E6);
        }
        for (int[] s : new int[][] {{55, 6}, {6, 9}, {58, 30}}) {   // a few twinkling ones
            put(s[0], s[1], 0xDCE6FF);
            for (int[] o : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                put(s[0] + o[0], s[1] + o[1], 0x7F93D0);
            }
        }

        // Last Light's glow: Hawking radiation, a pale blue haze round the hole
        each((x, y) -> {
            double d = holeDist(x, y);
            if (d >= R && d < R + 13) {
                blend(x, y, 0x5F8FD8, 0.42 * Math.pow(1 - (d - R) / 13, 2));
            }
        });

        // the Sun's warmth on the sky before Earth goes over it
        each((x, y) -> {
            double d = Math.hypot(x - SX, y - SY);
            if (d < 14) {
                blend(x, y, 0xFF8A3C, 0.55 * Math.pow(1 - d / 14, 2));
            }
        });

        // Earth: ocean, a lighter rim, the thin atmosphere above it (as the SA icon)
        each((x, y) -> {
            double d = earthDist(x, y);
            if (d < ER) {
                put(x, y, d > ER - 3 ? 0x2F8EE0 : (x + y) % 2 == 0 && d < ER - 4 ? 0x174A92 : 0x1D58A8);
            } else if (d < ER + 1.4) {
                blend(x, y, 0x96DCFF, 0.65);
            }
        });
        char[] landKeys = {'g', 'G', 'b'};
        int[] land = {0x3A9248, 0x2B7139, 0x9A7A4C};
        stamp(22, 53, new String[] {"...gggg....", ".ggGGgbbgg.", "gggGgg..gg.", "..g........"}, landKeys, land);
        stamp(3, 57, new String[] {".gggg..", "ggGbggg", "..ggg.."}, landKeys, land);

        // sunrise: the Sun's top above the limb, dawn spread along it
        each((x, y) -> {
            double g = Math.exp(-Math.pow((x - SX) / 13, 2) - Math.pow((y - SY + 1) / 2.6, 2));
            if (g > 0.08) {
                blend(x, y, 0xFFB45A, 0.85 * g);
            }
        });
        each((x, y) -> {
            double d = Math.hypot(x - SX, y - SY);
            if (d < 3.6 && earthDist(x, y) >= ER - 0.6) {
                put(x, y, d < 2.2 ? 0xFFF8DC : 0xFFD26A);
            }
        });
        for (int d = 4; d <= 7; d++) {   // a few rays, upward and along the limb
            double a = 0.9 * (1 - (d - 4) / 4.0);
            blend(SX, SY - d, 0xFFE6A0, a);
            blend(SX - d - 1, SY - 0.4, 0xFFC878, a * 0.8);
            blend(SX + d + 1, SY - 0.4, 0xFFC878, a * 0.8);
        }
        put(SX, SY - 1, 0xFFFFFF);

        // TON618's far side, bent up over the shadow (thick on top) and under it (thin): stepped colours, no fades
        each((x, y) -> {
            double d = holeDist(x, y);
            double up = (HY - y) / Math.max(d, 0.001);   // 1 straight above, -1 straight below
            double width = up > 0 ? 0.8 + 3.0 * Math.pow(up, 1.4) : 0.6 + 1.0 * Math.pow(-up, 2);
            double r = d - R - 0.4;
            if (r < 0 || r >= width) {
                return;
            }
            double heat = (up > 0 ? 0.35 + 0.65 * up : 0.25 + 0.35 * -up) * (1 - 0.45 * r / width);
            int c = heat > 0.78 ? 0xFFE6A8 : heat > 0.55 ? 0xFFB850 : heat > 0.35 ? 0xE47826 : 0x9C3E1A;
            put(x, y, c);
        });
        // the shadow, then the photon ring hugging it
        each((x, y) -> {
            double d = holeDist(x, y);
            if (d < R) {
                put(x, y, 0x020306);
            } else if (d < R + 0.85) {
                put(x, y, 0xFFF2D6);
            }
        });
        // the disk's near side, nearly edge on, across the shadow; the side coming at us (left) brighter
        for (int x = 0; x < S; x++) {
            double dx = x - HX;
            if (Math.abs(dx) > 25) {
                continue;
            }
            double b = (1 - Math.abs(dx) / 25) * (dx < 0 ? 1.12 : 0.82);
            for (int row = -1; row <= 1; row++) {
                double h = row == 0 ? b : b - 0.3;
                if (row != 0 && Math.abs(dx) > 17) {
                    continue;
                }
                int c = h > 0.72 ? 0xFFF4D4 : h > 0.48 ? 0xFFC864 : h > 0.24 ? 0xF0802A : h > 0.06 ? 0xA8441C : -1;
                if (c >= 0) {
                    put(x, HY + row, c);
                }
            }
        }

        // Hawking pairs at the horizon: the escaping one a gold spark with a short streak behind it, at different
        // distances (no spokes); its partner a blue dot falling in, close to the ring
        double[][] sparks = {{-140, 15}, {-100, 18.5}, {-58, 15.5}, {-18, 18}, {148, 14.5}, {38, 14.5}};
        for (double[] s : sparks) {
            double a = Math.toRadians(s[0]);
            double ux = Math.cos(a);
            double uy = Math.sin(a);
            double hx = HX + ux * s[1];
            double hy = HY + uy * s[1];
            blend(hx - ux * 1.6, hy - uy * 1.6, 0xE8A84A, 0.7);
            blend(hx - ux * 3.0, hy - uy * 3.0, 0xB0782E, 0.45);
            for (int[] o : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                blend(hx + o[0], hy + o[1], 0xFFD27A, 0.5);
            }
            put(hx, hy, 0xFFF6DC);
            double b = Math.toRadians(s[0] + 14);
            double r = R + (s[1] > 16 ? 2.4 : 1.9);
            put(HX + Math.cos(b) * r, HY + Math.sin(b) * r, 0x9AD6FF);
        }

        // the "Addon" tag along the bottom right, drawn just as the SA icon draws it (the series' tag)
        String[][] tag = {
            {"0110", "1001", "1001", "1111", "1001", "1001"},   // A
            {"0001", "0001", "0111", "1001", "1001", "0111"},   // d
            {"0001", "0001", "0111", "1001", "1001", "0111"},   // d
            {"0000", "0000", "0110", "1001", "1001", "0110"},   // o
            {"0000", "0000", "1110", "1001", "1001", "1001"},   // n
        };
        int tx0 = 36;
        int ty0 = 54;
        int tw = 24;
        for (int y = ty0 - 2; y <= ty0 + 8; y++) {
            for (int x = tx0 - 3; x <= tx0 + tw + 2; x++) {
                boolean border = y == ty0 - 2 || y == ty0 + 8 || x == tx0 - 3 || x == tx0 + tw + 2;
                put(x, y, border ? 0x0E5B86 : y == ty0 - 1 ? 0x7FE0FF : 0x1FA8DC);
            }
        }
        for (int x : new int[] {tx0 - 3, tx0 + tw + 2}) {
            for (int y : new int[] {ty0 - 2, ty0 + 8}) {
                put(x, y, 0x15205A);
            }
        }
        double[] shade = rgb(0x0E6A9A);
        for (int i = 0; i < tag.length; i++) {
            String[] ch = tag[i];
            for (int ry = 0; ry < 6; ry++) {
                for (int rx = 0; rx < 4; rx++) {
                    if (ch[ry].charAt(rx) != '1') {
                        continue;
                    }
                    int x = tx0 + i * 5 + rx;
                    int y = ty0 + 1 + ry;
                    put(x, y, 0xFFFFFF);
                    if (ch[Math.min(ry + 1, 5)].charAt(Math.min(rx + 1, 3)) != '1') {   // its dark drop shadow
                        double[] p = IMG[(y + 1) * S + x + 1];
                        for (int k = 0; k < 3; k++) {
                            p[k] = Math.min(p[k], shade[k]);
                        }
                    }
                }
            }
        }

        // the TA badge on top of the tag, right-aligned: a rounded gold plate (the disk's colour) with "TA" in chunky
        // dark letters, as the SA icon's blue "SA" plate
        String[] t = {"######", "######", "..##..", "..##..", "..##..", "..##..", "..##..", "..##.."};
        String[] aa = {".####.", "######", "##..##", "##..##", "######", "######", "##..##", "##..##"};
        int bw = 20;
        int bh = 14;
        int bx = 43;
        int by = 39;   // letters start on even pixels, so a 32x32 downscale keeps them sharp
        for (int y = by + 1; y < ty0 - 2; y++) {
            for (int x = bx + 1; x <= bx + bw; x++) {
                blend(x, y, 0x000000, 0.35);
            }
        }
        for (int y = by; y < by + bh; y++) {
            for (int x = bx; x < bx + bw; x++) {
                boolean edgeX = x == bx || x == bx + bw - 1;
                boolean edgeY = y == by || y == by + bh - 1;
                if (edgeX && edgeY) {
                    continue;   // rounded corners
                }
                boolean nearCorner = (x <= bx + 1 || x >= bx + bw - 2) && (y <= by + 1 || y >= by + bh - 2);
                put(x, y, edgeX || edgeY || nearCorner ? 0x3A1C06 : y == by + 1 ? 0xFFF0C4 : y == by + bh - 2 ? 0xE09A38 : 0xFFCC6A);
            }
        }
        char[] ink = {'#'};
        int[] inkColour = {0x3A1A04};
        stamp(bx + 3, by + 3, t, ink, inkColour);
        stamp(bx + 11, by + 3, aa, ink, inkColour);
    }
}
