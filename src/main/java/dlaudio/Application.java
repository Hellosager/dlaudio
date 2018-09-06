package dlaudio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
public class Application {
	
    public static void main(String[] args) {
    	copyLibariesToLocal();
        SpringApplication.run(Application.class, args);
    }
    
	// Copy lib folder when not present
    // TODO refactor this when linux support is added
	private static void copyLibariesToLocal() {
		File ffmpeg = new File("lib\\ffmpeg");
		File win = new File("lib\\win");
		if (!win.exists() || !ffmpeg.exists()) {
			win.mkdirs();
			ffmpeg.mkdirs();
			JarFile jarFile = null;
			try {
				String classFilePath = Application.class.getProtectionDomain().getCodeSource().getLocation().getPath().replaceAll("%20", " ");
				String jarFilePath = classFilePath.substring(0, classFilePath.indexOf("WEB-INF")).replace("file:/", "").replace("!", "");
				jarFile = new JarFile(jarFilePath);

				Enumeration<JarEntry> entries = jarFile.entries();
				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					copyDirectory(entry, jarFilePath, "lib/ffmpeg");
					copyDirectory(entry, jarFilePath, "lib/win");
				}
			} catch (IOException ioex) {
				ioex.printStackTrace();
			} finally {
				try {
					jarFile.close();
				} catch (Exception e) {
				}
			}
		}
	}

	private static void copyDirectory(JarEntry entry, String jarFilePath, String dirName) throws IOException {
		if (entry.getName().startsWith(dirName) && !entry.getName().equals(dirName + "/")) {
			System.out.println("copy " + entry.getName());
			InputStream fileStream = Application.class.getClassLoader().getResourceAsStream(entry.getName());
			String[] chopped = entry.getName().split("\\/");
			String fileName = chopped[chopped.length - 1];
			File libFile = new File(dirName, fileName);
			libFile.createNewFile();
			OutputStream out = new FileOutputStream(libFile);
			byte[] buffer = new byte[1024];
			int len = fileStream.read(buffer);
			while (len != -1) {
				out.write(buffer, 0, len);
				len = fileStream.read(buffer);
			}
			fileStream.close();
			out.close();
		}
	}
}