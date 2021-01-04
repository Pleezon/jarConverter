package de.techgamez.pleezon.encode;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class JarToPng {


    public static void main(String[] args) throws Exception {
        File target_jar = new File(args[0]); //the main jar to be "obfuscated" with PNG
        File output_jar = new File(args[1]);
        //copy the jar inside the jar to the output
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(output_jar));
        File namex = new File(JarToPng.class.getProtectionDomain().getCodeSource().getLocation()
                .toURI());
        ZipInputStream thisJarInputStream = new ZipInputStream(new FileInputStream(namex));
        ZipEntry entry1;
        while ((entry1 = thisJarInputStream.getNextEntry()) != null) {
            if (entry1.getName().endsWith(".jar")) {
                ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
                byte[] buffer1 = new byte[1024];
                int len1;
                while ((len1 = thisJarInputStream.read(buffer1)) > 0) {
                    bos1.write(buffer1,0,len1);
                }
                ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(bos1.toByteArray()));
                ZipEntry entry2;
                while((entry2 = zipInputStream.getNextEntry())!=null){
                    zos.putNextEntry(entry2);

                    byte[] buffer4 = new byte[1024];
                    int len4;
                    while ((len4 = zipInputStream.read(buffer4)) > 0) {
                        zos.write(buffer4, 0, len4);
                    }
                }
                zipInputStream.close();
                bos1.close();
                break;
            }
        }
        thisJarInputStream.close();
        ZipInputStream zs = new ZipInputStream(new FileInputStream(target_jar));
        ZipEntry entry;
        byte[] buffer = new byte[1024];
        while ((entry = zs.getNextEntry()) != null) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int len;
            while ((len = zs.read(buffer)) > 0) {
                bos.write(buffer, 0, len);
            }
            String entryName = entry.getName();
            System.out.println("entryname before: " + entryName);
            if (entry.getName().endsWith(".class")) {
                System.out.println("detected class.");
                BufferedImage img = enc(bos.toByteArray());
                bos.reset();
                ImageIO.write(img, "png", bos);
                entryName = entryName.replace(".class", ".png");
            } else if (entry.getName().endsWith("MANIFEST.MF")) {
                System.out.println("detected MANIFEST! replacing...");
                BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bos.toByteArray()), StandardCharsets.UTF_8));
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("Main-Class: ")) {
                        String prev_main_class = line.replace("Main-Class: ", "").trim();
                        bos.reset();
                        BufferedWriter bf = new BufferedWriter(new OutputStreamWriter(bos));
                        bf.write(prev_main_class);
                        bf.close();
                        entryName = entryName.replace("MANIFEST.MF", "main_class");
                    }

                }
            }
            System.out.println("entryname after: " + entryName);
            zos.putNextEntry(new ZipEntry(entryName));
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            byte[] buffer3 = new byte[1024];
            int len3;
            while ((len3 = bis.read(buffer3)) > 0) {
                zos.write(buffer3, 0, len3);
            }
            bis.close();
            bos.close();
            zos.closeEntry();
        }
        zos.close();
    }








    private static BufferedImage enc(byte[] bytes){

        String s;
        s = Base64.getEncoder().encodeToString(bytes);
        while(s.length()%3!=0)s+=" ";
        int fLength = s.length();
        while(fLength % 3 != 0||!isStringSquare(fLength/3)) fLength++;
        int sqrt = (int) Math.sqrt(fLength/3);
        BufferedImage bi = new BufferedImage(sqrt,sqrt,BufferedImage.TYPE_4BYTE_ABGR);
        for(int i=0;i<s.length();i+=3) {
            char[] chars = s.substring(i,i+3).toCharArray();
            Color c = new Color(chars[0], chars[1], chars[2], 255);
            bi.setRGB((i/3)%sqrt, (i/3)/sqrt, c.getRGB());
        }
        for(int i=s.length();i<fLength;i++)
            bi.setRGB((i/3)%sqrt, (i/3)/sqrt, new Color(0,0,0,0).getRGB());
        return bi;
    }
    private static boolean isStringSquare(int s){
        return Math.sqrt(s)%1==0;
    }

}
