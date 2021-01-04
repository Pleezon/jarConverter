package de.techgamez.pleezon.encode;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class JarReader {


    public static HashMap<String,byte[]> unzip(String path) throws Exception {
        byte[] fileContent = Files.readAllBytes(new File(path).toPath());
        return unzip(fileContent);
    }

    static HashMap<String,byte[]> unzip(byte[] zippedFile) throws IOException {
        HashMap<String,byte[]> ret = new HashMap<>();
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zippedFile));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int len;
            while ((len = zis.read(buffer)) > 0) {
                bos.write(buffer, 0, len);
            }
            ret.put(zipEntry.getName(),bos.toByteArray());
            bos.close();
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
        return ret;
    }
    public static void pack(String sourceDirPath, String zipFilePath) throws IOException {

        Path p = Files.createFile(Paths.get(zipFilePath));
        OutputStream os;
        try (JarOutputStream zs = new JarOutputStream(Files.newOutputStream(p))) {
            Path pp = Paths.get(sourceDirPath);
            Files.walk(pp)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
                        try {
                            zs.putNextEntry(zipEntry);
                            Files.copy(path, zs);
                            zs.closeEntry();
                        } catch (IOException e) {
                            System.err.println(e);
                        }
                    });

        }
    }

    private static void createJar(File source, JarOutputStream target) {
        createJar(source, source, target);
    }

    public static void createJar(File source, File baseDir, JarOutputStream target) {
        BufferedInputStream in = null;

        try {
            if (!source.exists()) {
                throw new IOException("Source directory is empty");
            }
            if (source.isDirectory()) {
                // For Jar entries, all path separates should be '/'(OS independent)
                String name = source.getPath().replace("\\", "/");
                if (!name.isEmpty()) {
                    if (!name.endsWith("/")) {
                        name += "/";
                    }
                    JarEntry entry = new JarEntry(name);
                    entry.setTime(source.lastModified());
                    target.putNextEntry(entry);
                    target.closeEntry();
                }
                for (File nestedFile: source.listFiles()) {
                    createJar(nestedFile, baseDir, target);
                }
                return;
            }

            String entryName = baseDir.toPath().relativize(source.toPath()).toFile().getPath().replace("\\", "/");
            JarEntry entry = new JarEntry(entryName);
            entry.setTime(source.lastModified());
            target.putNextEntry(entry); in = new BufferedInputStream(new FileInputStream(source));

            byte[] buffer = new byte[1024];
            while (true) {
                int count = in .read(buffer);
                if (count == -1)
                    break;
                target.write(buffer, 0, count);
            }
            target.closeEntry();
        } catch (Exception ignored) {

        } finally {
            if ( in != null) {
                try { in .close();
                } catch (Exception ignored) {
                    throw new RuntimeException(ignored);
                }
            }
        }
    }
}
