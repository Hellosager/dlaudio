package dlaudio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import AudioStripper.AudioStripper;



@Controller
public class DownloadController {
	
	@RequestMapping(value="/convert", method=RequestMethod.GET)
	public RedirectView getMP3FromVideo(@RequestParam("link") String link, RedirectAttributes attributes) throws IOException, InterruptedException {
		AudioStripper as = new AudioStripper("Videos", "Musik");
		String pathMp3File = as.stripAudioFromVideoLink(link).replaceAll(".mp4", ".mp3").replaceFirst("Musik\\\\", "");
		System.out.println("resName: " + pathMp3File);
		
		attributes.addFlashAttribute("flashAttribute", "redirectWithRedirectView");
		attributes.addAttribute("attribute", "redirectWithRedirectView");
		return new RedirectView("download/" + pathMp3File);
	}
	
	@RequestMapping(value="/download/{resourceName}", method=RequestMethod.GET)
	public void downloadResource(@PathVariable(value="resourceName") String resourceName, HttpServletResponse response) {
		response.setHeader("Content-Disposition", "attachment;filename=\"" + resourceName + "\"");	// TODO HARDCODED
		InputStream stream;
		try {
			stream = new FileInputStream(new File("Musik\\" + resourceName));	// TODO HARDCODED
			IOUtils.copy(stream, response.getOutputStream());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@RequestMapping(value="/convert", method=RequestMethod.POST)
	public RedirectView getMP3sFromVideo(@ModelAttribute("textFile")MultipartFile textFile, RedirectAttributes attributes) throws IOException, InterruptedException {
		System.out.println("inmethod");
		attributes.addFlashAttribute("flashAttribute", "redirectWithRedirectView");
		attributes.addAttribute("attribute", "redirectWithRedirectView");
		String fileContent = new String(textFile.getBytes());
		String[] links = fileContent.split("\n");
		AudioStripper as = new AudioStripper("Videos", "Musik");
		String[] downloadedMP3Paths = as.stripAudioFromVideoLinks(links);

		// Zip it
		String zipName = zipFiles(downloadedMP3Paths);
		
		return new RedirectView("download/" + zipName);
	}
	
	
	private String zipFiles(String[] filePaths) throws IOException {
		String uniqueZipName = "converted-mp3s-" + System.currentTimeMillis() + ".zip";
		System.out.println("Storing downloaded files in " + "Musik\\" + uniqueZipName);
		FileOutputStream fos = new FileOutputStream("Musik\\" + uniqueZipName);
		ZipOutputStream zipOutputStream = new ZipOutputStream(fos);
		for(String path : filePaths) {
			File fileToZip = new File(path);
			FileInputStream fis = new FileInputStream(fileToZip);
			ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
			zipOutputStream.putNextEntry(zipEntry);
			
			byte[] bytes = new byte[1024];
			int length;
			while((length = fis.read(bytes)) >= 0) {
				zipOutputStream.write(bytes, 0, length);
			}
			fis.close();
		}
		zipOutputStream.close();
		fos.close();
		
		return uniqueZipName;
	}
}
