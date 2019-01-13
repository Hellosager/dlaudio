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
	public static final String OS_NAME = System.getProperty("os.name");
	private static final boolean IS_WINDOWS_10 = !OS_NAME.equalsIgnoreCase("Linux");
	public static final String PATH_TO_FFMPEG = !IS_WINDOWS_10 ? "ffmpeg" : "lib/ffmpeg";
	public static final String PATH_TO_VIDEO = !IS_WINDOWS_10 ? "lib/video/arm" : "lib/video/win";
	
    public static void main(String[] args) {
    	copyLibariesToLocal();
        SpringApplication.run(Application.class, args);
    }
    
	// Copy lib folder when not present
    // TODO refactor this when linux support is added
	private static void copyLibariesToLocal() {
		File ffmpeg = new File(PATH_TO_FFMPEG);
		File videoLib = new File(PATH_TO_VIDEO);
		if (!videoLib.exists() || (IS_WINDOWS_10 && !ffmpeg.exists())) {
			videoLib.mkdirs();
			if(IS_WINDOWS_10)
				ffmpeg.mkdirs();
			
			JarFile jarFile = null;
			try {
				String classFilePath = Application.class.getProtectionDomain().getCodeSource().getLocation().getPath().replaceAll("%20", " ");
				String jarFilePath = classFilePath.substring(0, classFilePath.indexOf("WEB-INF")).replace("file:/", "").replace("!", "");
				if(!IS_WINDOWS_10)
					jarFilePath = "/" + jarFilePath;
				jarFile = new JarFile(jarFilePath);

				Enumeration<JarEntry> entries = jarFile.entries();
				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					if(IS_WINDOWS_10)
						copyDirectory(entry, PATH_TO_FFMPEG);
					copyDirectory(entry, PATH_TO_VIDEO);
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

	private static void copyDirectory(JarEntry entry, String dirName) throws IOException {
		if (entry.getName().startsWith(dirName) && !entry.getName().equals(dirName + "/")) {
			System.out.println("copy " + entry.getName());
			InputStream fileStream = Application.class.getClassLoader().getResourceAsStream(entry.getName());
			String[] chopped = entry.getName().split("/");
			String fileName = chopped[chopped.length - 1];
			File libFile = new File(dirName, fileName);
			libFile.createNewFile();
			if(!IS_WINDOWS_10) {
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