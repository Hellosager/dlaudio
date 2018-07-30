package dlaudio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import AudioStripper.AudioStripper;



@Controller
public class FileUploadController {
	
	@RequestMapping(value="/download", method=RequestMethod.GET)
	public void getMP3FromVideo(@RequestParam("link") String link, HttpServletResponse response) throws IOException, InterruptedException {
		System.out.println("VIDEO DOWNLOAD NICE");
		AudioStripper as = new AudioStripper("Videos", "Musik");
		String pathMp3File = as.stripAudioFromVideoLink(link).replaceAll(".mp4", ".mp3");
		
		InputStream stream = new FileInputStream(new File(pathMp3File));
		IOUtils.copy(stream, response.getOutputStream());
	}

}
