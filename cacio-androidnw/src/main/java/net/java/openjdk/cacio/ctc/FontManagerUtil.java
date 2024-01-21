package net.java.openjdk.cacio.ctc;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

import sun.font.Font2D;
import sun.font.FontManager;
import sun.font.FontManagerFactory;
import sun.font.FontScaler;

class FontManagerUtil {

    // A way to force set a FontManager
    static void setFontManager(final String fmClassName) {
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            try {
                System.setProperty("sun.font.fontmanager", fmClassName);

                Class<?> fmClass = Class.forName(fmClassName);
                FontManager instance = (FontManager) fmClass.getDeclaredConstructor().newInstance();

                Field fmInstanceField = FontManagerFactory.class.getDeclaredField("instance");
                fmInstanceField.setAccessible(true);
                fmInstanceField.set(null, instance);

                String currName = FontManagerFactory.getInstance().getClass().getName();
                if (!currName.equals(fmClassName)) {
                    System.err.println("Could not change font manager to " + fmClassName + ", current was " + currName);
                }
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException("Failed to set font manager", ex);
            }
            return null;
        });
    }

    static void setFontScaler(final String fsClassName) {
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            try {
                Field fmInstanceField = FontScaler.class.getDeclaredField("scalerConstructor");
                fmInstanceField.setAccessible(true);
                fmInstanceField.set(null, Class.forName(fsClassName).getConstructor(Font2D.class, int.class, boolean.class, int.class));
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException("Failed to set font scaler", ex);
            }
            return null;
        });
    }
}