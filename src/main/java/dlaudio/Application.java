package dlaudio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
	public static String OS_NAME = System.getProperty("os.name");
	
    public static void main(String[] args) {
    	copyLibariesToLocal();
        SpringApplication.run(Application.class, args);
    }
    
	// Copy lib folder when not present
    // TODO refactor this when linux support is added
	private static void copyLibariesToLocal() {
		File ffmpeg = new File("lib/ffmpeg");
		File win = new File("lib/win");
		File ffprobe = new File("lib/ffprobe");
		if (!win.exists() || !ffmpeg.exists() || !ffprobe.exists()) {
			win.mkdirs();
			ffmpeg.mkdirs();
			ffprobe.mkdirs();
			JarFile jarFile = null;
			try {
				String classFilePath = Application.class.getProtectionDomain().getCodeSource().getLocation().getPath().replaceAll("%20", " ");
				String jarFilePath = classFilePath.substring(0, classFilePath.indexOf("WEB-INF")).replace("file:/", "").replace("!", "");
				if(OS_NAME.equalsIgnoreCase("linux"))
					jarFilePath = "/" + jarFilePath;
				jarFile = new JarFile(jarFilePath);

				Enumeration<JarEntry> entries = jarFile.entries();
				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					copyDirectory(entry, jarFilePath, "lib/ffmpeg");
					copyDirectory(entry, jarFilePath, "lib/win");
					copyDirectory(entry, jarFilePath, "lib/ffprobe");
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
			String[] chopped = entry.getName().split("/");
			String fileName = chopped[chopped.length - 1];
			File libFile = new File(dirName, fileName);
			libFile.createNewFile();
			if(OS_NAME.equalsIgnoreCase("linux")) {
				Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
				perms.add(PosixFilePermission.OWNER_EXECUTE);
				Files.setPosixFilePermissions(Paths.get(libFile.getPath()), perms);
			}
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