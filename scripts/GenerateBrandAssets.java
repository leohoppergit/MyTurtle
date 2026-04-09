package scripts;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

public final class GenerateBrandAssets {

    private static final Color OLIVE_DARK = new Color(0x2D, 0x39, 0x17);
    private static final Color OLIVE = new Color(0x55, 0x6B, 0x2F);
    private static final Color OLIVE_LIGHT = new Color(0x7E, 0xB4, 0x2A);
    private static final Color SAND = new Color(0xF4, 0xE6, 0xCC);
    private static final Color SAND_SOFT = new Color(0xE8, 0xD8, 0xB1);
    private static final Color BROWN = new Color(0x6B, 0x4F, 0x2A);
    private static final Color BROWN_DARK = new Color(0x49, 0x35, 0x18);
    private static final Color MINT = new Color(0xD7, 0xF1, 0xB5);

    private GenerateBrandAssets() {
    }

    public static void main(String[] args) throws IOException {
        Path outputDir = Path.of("docs", "assets");
        Files.createDirectories(outputDir);

        writeImage(outputDir.resolve("myturtle-icon.png"), buildIcon(1024, 1024));
        writeImage(outputDir.resolve("myturtle-feature-graphic.png"), buildFeatureGraphic(1600, 900));
    }

    private static void writeImage(Path outputPath, BufferedImage image) throws IOException {
        ImageIO.write(image, "png", outputPath.toFile());
    }

    private static BufferedImage buildIcon(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createGraphics(image);

        float[] dist = {0f, 0.6f, 1f};
        Color[] colors = {new Color(0x8A, 0xC9, 0x33), OLIVE, OLIVE_DARK};
        g.setPaint(new LinearGradientPaint(0, 0, width, height, dist, colors));
        g.fill(new RoundRectangle2D.Double(0, 0, width, height, width * 0.26, width * 0.26));

        g.setPaint(new RadialGradientPaint(
            width * 0.28f,
            height * 0.22f,
            width * 0.5f,
            new float[]{0f, 1f},
            new Color[]{new Color(255, 255, 255, 60), new Color(255, 255, 255, 0)}
        ));
        g.fillRect(0, 0, width, height);

        drawShell(g, width * 0.5, height * 0.47, width * 0.46, height * 0.42);
        drawHeadAndLegs(g, width * 0.5, height * 0.47, width * 0.46, height * 0.42);
        drawChartLine(g, width, height);
        drawShield(g, width * 0.74, height * 0.70, width * 0.16);

        g.dispose();
        return image;
    }

    private static BufferedImage buildFeatureGraphic(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createGraphics(image);

        float[] dist = {0f, 0.45f, 1f};
        Color[] colors = {new Color(0x1E, 0x27, 0x0E), new Color(0x3D, 0x4E, 0x21), new Color(0x70, 0x93, 0x2B)};
        g.setPaint(new LinearGradientPaint(0, 0, width, height, dist, colors));
        g.fillRect(0, 0, width, height);

        g.setColor(new Color(255, 255, 255, 18));
        g.fill(new RoundRectangle2D.Double(56, 64, width - 112, height - 128, 56, 56));
        g.fill(new Ellipse2D.Double(-120, -180, 720, 520));
        g.fill(new Ellipse2D.Double(width - 560, height - 430, 700, 500));

        BufferedImage icon = buildIcon(700, 700);
        g.drawImage(icon, 80, 120, 640, 640, null);

        drawFeatureText(g, width, height);
        drawFeatureChips(g, width, height);
        drawMiniCards(g, width, height);

        g.dispose();
        return image;
    }

    private static void drawShell(Graphics2D g, double centerX, double centerY, double shellWidth, double shellHeight) {
        g.setColor(SAND);
        g.fill(new Ellipse2D.Double(centerX - shellWidth / 2, centerY - shellHeight / 2, shellWidth, shellHeight));

        g.setColor(OLIVE);
        g.fill(new Ellipse2D.Double(centerX - shellWidth * 0.44, centerY - shellHeight * 0.43, shellWidth * 0.88, shellHeight * 0.86));

        g.setStroke(new BasicStroke((float) (shellWidth * 0.018), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(255, 255, 255, 70));
        g.draw(new Ellipse2D.Double(centerX - shellWidth * 0.40, centerY - shellHeight * 0.39, shellWidth * 0.80, shellHeight * 0.78));

        double cellW = shellWidth * 0.20;
        double cellH = shellHeight * 0.22;
        double[][] centers = {
            {centerX, centerY - shellHeight * 0.18},
            {centerX - shellWidth * 0.16, centerY},
            {centerX + shellWidth * 0.16, centerY},
            {centerX - shellWidth * 0.08, centerY + shellHeight * 0.18},
            {centerX + shellWidth * 0.08, centerY + shellHeight * 0.18},
        };
        for (double[] cell : centers) {
            Shape scute = new RoundRectangle2D.Double(
                cell[0] - cellW / 2,
                cell[1] - cellH / 2,
                cellW,
                cellH,
                cellW * 0.38,
                cellW * 0.38
            );
            g.setColor(BROWN);
            g.fill(scute);
            g.setColor(new Color(255, 255, 255, 44));
            g.draw(scute);
        }
    }

    private static void drawHeadAndLegs(Graphics2D g, double centerX, double centerY, double shellWidth, double shellHeight) {
        g.setColor(SAND_SOFT);
        g.fill(new Ellipse2D.Double(centerX - shellWidth * 0.10, centerY - shellHeight * 0.63, shellWidth * 0.20, shellHeight * 0.18));

        double[][] legs = {
            {centerX - shellWidth * 0.42, centerY - shellHeight * 0.10},
            {centerX + shellWidth * 0.28, centerY - shellHeight * 0.10},
            {centerX - shellWidth * 0.28, centerY + shellHeight * 0.36},
            {centerX + shellWidth * 0.14, centerY + shellHeight * 0.36},
        };
        for (double[] leg : legs) {
            g.fill(new Ellipse2D.Double(leg[0], leg[1], shellWidth * 0.14, shellHeight * 0.18));
        }
    }

    private static void drawChartLine(Graphics2D g, int width, int height) {
        Path2D.Double line = new Path2D.Double();
        line.moveTo(width * 0.20, height * 0.66);
        line.lineTo(width * 0.34, height * 0.60);
        line.lineTo(width * 0.45, height * 0.63);
        line.lineTo(width * 0.57, height * 0.50);
        line.lineTo(width * 0.72, height * 0.42);

        g.setStroke(new BasicStroke(width * 0.022f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(MINT);
        g.draw(line);

        double[][] points = {
            {width * 0.20, height * 0.66},
            {width * 0.34, height * 0.60},
            {width * 0.45, height * 0.63},
            {width * 0.57, height * 0.50},
            {width * 0.72, height * 0.42},
        };
        for (double[] point : points) {
            g.setColor(SAND);
            g.fill(new Ellipse2D.Double(point[0] - width * 0.020, point[1] - width * 0.020, width * 0.040, width * 0.040));
            g.setColor(BROWN_DARK);
            g.setStroke(new BasicStroke(width * 0.006f));
            g.draw(new Ellipse2D.Double(point[0] - width * 0.020, point[1] - width * 0.020, width * 0.040, width * 0.040));
        }
    }

    private static void drawShield(Graphics2D g, double x, double y, double size) {
        Path2D.Double shield = new Path2D.Double();
        shield.moveTo(x, y);
        shield.curveTo(x + size * 0.46, y - size * 0.14, x + size * 0.88, y + size * 0.12, x + size * 0.82, y + size * 0.48);
        shield.curveTo(x + size * 0.76, y + size * 0.86, x + size * 0.42, y + size * 1.08, x + size * 0.30, y + size * 1.18);
        shield.curveTo(x + size * 0.18, y + size * 1.08, x - size * 0.16, y + size * 0.86, x - size * 0.22, y + size * 0.48);
        shield.curveTo(x - size * 0.28, y + size * 0.12, x + size * 0.14, y - size * 0.14, x + size * 0.60, y);
        shield.closePath();

        g.setColor(new Color(33, 41, 19, 210));
        g.fill(shield);
        g.setStroke(new BasicStroke((float) (size * 0.06), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(MINT);
        g.draw(shield);

        Path2D.Double leaf = new Path2D.Double();
        leaf.moveTo(x + size * 0.18, y + size * 0.60);
        leaf.curveTo(x + size * 0.16, y + size * 0.28, x + size * 0.46, y + size * 0.18, x + size * 0.58, y + size * 0.42);
        leaf.curveTo(x + size * 0.68, y + size * 0.64, x + size * 0.50, y + size * 0.90, x + size * 0.24, y + size * 0.92);
        leaf.closePath();
        g.fill(leaf);

        g.setColor(new Color(33, 41, 19, 200));
        g.setStroke(new BasicStroke((float) (size * 0.04), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine((int) (x + size * 0.25), (int) (y + size * 0.88), (int) (x + size * 0.54), (int) (y + size * 0.36));
    }

    private static void drawFeatureText(Graphics2D g, int width, int height) {
        g.setColor(SAND);
        g.setFont(new Font("SansSerif", Font.BOLD, 96));
        g.drawString("MyTurtle", 760, 250);

        g.setFont(new Font("SansSerif", Font.PLAIN, 34));
        g.setColor(new Color(241, 234, 214));
        g.drawString("Datenschutzfreundliche Schildkröten-Dokumentation für Android", 760, 310);

        g.setFont(new Font("SansSerif", Font.PLAIN, 28));
        g.drawString("Gewicht, Panzerlänge, Lebensereignisse und Fotos lokal festhalten.", 760, 365);
        g.drawString("Ohne Cloud-Zwang, ohne Konto, ohne EXIF-Metadaten im Import.", 760, 405);

        g.setFont(new Font("SansSerif", Font.BOLD, 28));
        g.setColor(MINT);
        g.drawString("v0.1.0-alpha", 760, 470);
    }

    private static void drawFeatureChips(Graphics2D g, int width, int height) {
        String[] labels = {"Lokal", "Ohne EXIF", "Zeitverlauf", "Kein Cloud-Zwang"};
        int x = 760;
        int y = 520;
        int gap = 18;
        g.setFont(new Font("SansSerif", Font.BOLD, 24));
        for (String label : labels) {
            int chipWidth = g.getFontMetrics().stringWidth(label) + 44;
            g.setColor(new Color(244, 230, 204, 34));
            g.fillRoundRect(x, y, chipWidth, 52, 26, 26);
            g.setColor(new Color(215, 241, 181, 180));
            g.drawRoundRect(x, y, chipWidth, 52, 26, 26);
            g.setColor(SAND);
            g.drawString(label, x + 22, y + 34);
            x += chipWidth + gap;
        }
    }

    private static void drawMiniCards(Graphics2D g, int width, int height) {
        int cardY = 610;
        drawCard(g, 760, cardY, 230, 170, "Messungen", "37 g", "85 mm");
        drawCard(g, 1020, cardY, 230, 170, "Ereignisse", "Winterruhe", "Tierarzt");
        drawCard(g, 1280, cardY, 230, 170, "Fotos", "Jahresbild", "Messungsfoto");
    }

    private static void drawCard(Graphics2D g, int x, int y, int width, int height, String title, String line1, String line2) {
        g.setColor(new Color(0x1C, 0x24, 0x0D, 180));
        g.fillRoundRect(x, y, width, height, 32, 32);
        g.setColor(new Color(244, 230, 204, 90));
        g.drawRoundRect(x, y, width, height, 32, 32);

        g.setColor(SAND);
        g.setFont(new Font("SansSerif", Font.BOLD, 26));
        g.drawString(title, x + 22, y + 42);

        g.setColor(MINT);
        g.setFont(new Font("SansSerif", Font.BOLD, 34));
        g.drawString(line1, x + 22, y + 92);

        g.setColor(new Color(244, 230, 204));
        g.setFont(new Font("SansSerif", Font.PLAIN, 24));
        g.drawString(line2, x + 22, y + 132);
    }

    private static Graphics2D createGraphics(BufferedImage image) {
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g;
    }
}
