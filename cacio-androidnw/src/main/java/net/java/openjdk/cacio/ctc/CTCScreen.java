package net.java.openjdk.cacio.ctc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.List;
import javax.imageio.ImageIO;

import sun.awt.peer.cacio.WindowClippedGraphics;
import sun.awt.peer.cacio.managed.*;
import sun.awt.peer.cacio.*;
import java.io.*;

public class CTCScreen implements PlatformScreen {

    private BufferedImage screenBuffer;
    private static CTCScreen instance;

    static {
        instance = new CTCScreen();
    }

    private CTCScreen() {
        Dimension screenDimension = FullScreenWindowFactory.getScreenDimension();
        screenBuffer = new BufferedImage(screenDimension.width, screenDimension.height, BufferedImage.TYPE_INT_ARGB);
    }

    public static CTCScreen getInstance() {
        return instance;
    }

    @Override
    public ColorModel getColorModel() {
        return screenBuffer.getColorModel();
    }

    @Override
    public GraphicsConfiguration getGraphicsConfiguration() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    }

    @Override
    public Rectangle getBounds() {
        Dimension screenDimension = FullScreenWindowFactory.getScreenDimension();
        return new Rectangle(0, 0, screenDimension.width, screenDimension.height);
    }

    @Override
    public Graphics2D getClippedGraphics(Color fg, Color bg, Font f, List<Rectangle> clipRects) {
        Graphics2D g2d = (Graphics2D) screenBuffer.getGraphics();
        if (clipRects != null && !clipRects.isEmpty()) {
            Area a = new Area(getBounds());
            for (Rectangle clip : clipRects) {
                a.subtract(new Area(clip));
            }
            g2d = new WindowClippedGraphics(g2d, a);
        }
        return g2d;
    }

    int[] getRGBPixels(Rectangle bounds) {
        return screenBuffer.getRGB(bounds.x, bounds.y, bounds.width, bounds.height, null, 0, bounds.width);
    }

    int getRGBPixel(int x, int y) {
        return screenBuffer.getRGB(x, y);
    }

    private static int[] dataBufAux;

    public static int[] getCurrentScreenRGB() {
        if (instance.screenBuffer == null) {
            return null;
        } else {
            if (dataBufAux == null) {
                dataBufAux = new int[((int) FullScreenWindowFactory.getScreenDimension().getWidth()) * (int) FullScreenWindowFactory.getScreenDimension().getHeight()];
            }
            instance.screenBuffer.getRaster().getDataElements(0, 0,
                    (int) FullScreenWindowFactory.getScreenDimension().getWidth(),
                    (int) FullScreenWindowFactory.getScreenDimension().getHeight(),
                    dataBufAux);

            return dataBufAux;
        }
    }

    static {
        try {
            File currLibFile;
            for (String ldLib : System.getenv("LD_LIBRARY_PATH").split(":")) {
                if (ldLib.isEmpty()) continue;
                currLibFile = new File(ldLib, "libpojavexec_awt.so");
                if (currLibFile.exists()) {
                    System.load(currLibFile.getAbsolutePath());
                    break;
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }
}