package dlaudio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import AudioStripper.AudioStripper;



@Controller
public class DownloadController {
	
	@RequestMapping(value="/convert", method=RequestMethod.GET)
	public void getMP3FromVideo(@RequestParam("link") String link, HttpServletResponse response) throws IOException, InterruptedException {
		System.out.println("VIDEO DOWNLOAD NICE");
		AudioStripper as = new AudioStripper("Videos", "Musik");
		String pathMp3File = as.stripAudioFromVideoLink(link).replaceAll(".mp4", ".mp3");
		System.out.println("resName: " + pathMp3File);
//		response.setHeader("Content-Disposition", "attachment;filename=\"" + pathMp3File + "\"");
//		InputStream stream = new FileInputStream(new File(pathMp3File));
//		IOUtils.copy(stream, response.getOutputStream());
		
		System.out.println("----------------WIEDER FREI----------------");
	}
	
	@RequestMapping(value="/download/{resourceName}", method=RequestMethod.GET)
	public void downloadResource(@PathVariable(value="resourceName") String resourceName, HttpServletResponse response) {
		System.out.println("drin");
		response.setHeader("Content-Disposition", "attachment;filename=\"" + "Musik\\" + resourceName + "\"");	// TODO HARDCODED
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

}
