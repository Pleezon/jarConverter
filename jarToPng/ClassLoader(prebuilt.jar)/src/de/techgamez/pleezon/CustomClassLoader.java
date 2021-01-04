package de.techgamez.pleezon;



import java.awt.Color;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore.Entry;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;


public class CustomClassLoader extends ClassLoader {
	private static ZipFile zf;
	
    private static CustomClassLoader dis = new CustomClassLoader();
    
   
    public static void main(String[] args) throws URISyntaxException, IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    	loadMainPNG(args);
	}
    
    @Override
    public Class findClass(String name) throws ClassNotFoundException {
        byte[] b = getClass(name);
        return defineClass(name, b, 0, b.length);
    }
    
    private static byte[] getClass(String name) {
    	name = name.replaceAll("\\.", "/") + ".png";
    	ZipEntry entry = zf.getEntry(name);
    	try {
			InputStream is = zf.getInputStream(entry);
			return dec(ImageIO.read(is));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
    }
    
    
    
    
    
    
    private static void loadMainPNG(String[] args) throws URISyntaxException, IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    	File namex = new File(CustomClassLoader.class.getProtectionDomain().getCodeSource().getLocation()
                .toURI());
    	zf = new ZipFile(namex);
    
    	ZipEntry entry = zf.getEntry("META-INF/main_class");
    	if(entry != null) {
    		String name = StreamToString(zf.getInputStream(entry));
            ZipEntry mainpng = zf.getEntry(name.replaceAll("\\.", "/")+".png");
            InputStream is = zf.getInputStream(mainpng);
    		BufferedImage bi = ImageIO.read(is);
    		byte[] cl = dec(bi);
    		Class<?> cla = dis.loadClassN(name, cl);
    		Method m = cla.getDeclaredMethod("main", String[].class);
    		m.invoke(null, new Object[] {args});
    	}else {
    		return;
    	}
    }
    
    
    
    private static String StreamToString(InputStream is) throws IOException {
    	BufferedInputStream bis = new BufferedInputStream(is);
    	ByteArrayOutputStream buf = new ByteArrayOutputStream();
    	int result = bis.read();
    	while(result != -1) {
    	    buf.write((byte) result);
    	    result = bis.read();
    	}
    	return buf.toString("UTF-8");
    }
    
    
    private Class<?> loadClassN(String name, byte[] bytes) {
        Class<?> cl=null;
        try{cl = defineClass(name, bytes, 0, bytes.length);}catch (NoClassDefFoundError e){e.printStackTrace();}
        return cl;
    }

    private static Class<?> loadClass(String name, byte[] bytes) {
        return dis.loadClassN(name, bytes);
    }

    
    private static byte[] dec(BufferedImage bi){
        StringBuilder ret = new StringBuilder();
        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                Color c = new Color(bi.getRGB(x, y));
                int r = c.getRed();
                int g = c.getGreen();
                int b = c.getBlue();
                int a = c.getAlpha();
                if(a==0)return decode(ret.toString());
                ret.append((char)r).append((char)g).append((char)b);
            }
        }
        return decode(ret.toString());
    }

    private static byte[] decode(String s){
        return Base64.getDecoder().decode(s.trim());
    }
    
}