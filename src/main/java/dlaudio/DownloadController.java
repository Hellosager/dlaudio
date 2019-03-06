package dlaudio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import AudioStripper.AudioStripper;
import AudioStripperProcesses.AudioStripperProcess;
import AudioStripperProcesses.AudioConvertingProcesses.MultiLinkAudioConvertingProcess;
import AudioStripperProcesses.AudioConvertingProcesses.SingleLinkAudioConvertingProcess;
import AudioStripperProcesses.VideoDownloadProcesses.SingleLinkVideoDownloadProcess;


@Controller
public class DownloadController {

	@Autowired
	ServletContext servletContext;

	private HashMap<Integer, AudioStripperProcess> processes = new HashMap<>();


	// TODO to hardcoded, no support for other formats, refactor if needed
	@RequestMapping(value = "/background.mp4", method = RequestMethod.GET)
	public void getBackgroundVideo(HttpServletResponse response) throws IOException {
		InputStream stream = servletContext.getResourceAsStream("background.mp4");
		response.setContentType("video/mp4");
		IOUtils.copy(stream, response.getOutputStream());
	}
	
	@RequestMapping(value = "/download/{pid}", method = RequestMethod.GET)
	public void downloadResource(@PathVariable(value = "pid") int pid,
			HttpServletResponse response) {
		String resourcePath = null;
		String resourceName = null;
		AudioStripperProcess process = processes.get(pid);
		if(process instanceof SingleLinkAudioConvertingProcess) {
			SingleLinkAudioConvertingProcess slcp = (SingleLinkAudioConvertingProcess) process;
			resourcePath = slcp.getResourcePath();
			resourceName = slcp.getResourceName();
		}else if(process instanceof MultiLinkAudioConvertingProcess) {
			MultiLinkAudioConvertingProcess mlcp = (MultiLinkAudioConvertingProcess) process;
			try {
				resourcePath = zipFiles(mlcp.getMP3Paths());
				resourceName = new File(resourcePath).getName();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else if(process instanceof SingleLinkVideoDownloadProcess) {
			SingleLinkVideoDownloadProcess slvdp = (SingleLinkVideoDownloadProcess) process;
			resourcePath = slvdp.getDownloadedVideoPath();
			resourceName = slvdp.getDownloadedResourceName();
		}
		
		response.setHeader("Content-Disposition", "attachment;filename=\"" + resourceName + "\""); // TODO HARDCODED
		InputStream stream;
		try {
			stream = new FileInputStream(new File(resourcePath));
			IOUtils.copy(stream, response.getOutputStream());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@RequestMapping(value = "/convert", method = RequestMethod.GET)
	public String getMP3FromVideo(@RequestParam("link") String link, Model model)
			throws IOException, InterruptedException {
		model.addAttribute("type", "single");
		AudioStripper as = new AudioStripper("Videos", "Musik");
		startAudioStripperProcess(new SingleLinkAudioConvertingProcess(link, as), model);
		
		return "progress";
	}

	@RequestMapping(value = "/convert", method = RequestMethod.POST)
	public String getMP3sFromVideo(@ModelAttribute("textFile") MultipartFile textFile, Model model)
			throws IOException, InterruptedException {
		System.out.println("inmethod");
		model.addAttribute("type", "multi");
		String fileContent = new String(textFile.getBytes());
		String[] links = fileContent.split("\n");
		AudioStripper as = new AudioStripper("Videos", "Musik");
		startAudioStripperProcess(new MultiLinkAudioConvertingProcess(links, as), model);
		return "progress";
	}
	
	@RequestMapping(value = "/convertVideo", method = RequestMethod.GET)
	public String downloadVideo(@RequestParam("link") String link, Model model)
			throws IOException, InterruptedException {
		model.addAttribute("type", "singleVideo");
		AudioStripper as = new AudioStripper("Videos", "Musik");
		startAudioStripperProcess(new SingleLinkVideoDownloadProcess(link, as) , model);
		
		return "progress";
	}

	@GetMapping(value = "/retrieveProgress/{pid}", produces = "application/json")
	@ResponseBody
	public Map<String, Integer> retrieveProgress(@PathVariable(value = "pid") int pid) {
		HashMap<String, Integer> map = new HashMap<>();
		map.put("progress", processes.get(pid).getCurrentProgress());
		return map;
	}

	@RequestMapping("/progress")
	public String progress() {
		return "progress";
	}

	// TODO implement process kill for converting processes
	// ATTENTION!!! This could be called by anyone, find better way to cleanup after process termination by user
	// AND DONT CALL IT BY PATH VAR
	@RequestMapping(value = "/killProcess/{pid}", method = RequestMethod.GET)
	public void leave(HttpServletResponse response, @PathVariable(value = "pid") int pid) {
		System.out.println("Leaving site...");
		processes.get(pid).kill();
	}
	
	private String zipFiles(String[] filePaths) throws IOException {
		String uniqueZipName = "converted-mp3s-" + System.currentTimeMillis() + ".zip";
		System.out.println("Storing downloaded files in " + "Musik/" + uniqueZipName);
		FileOutputStream fos = new FileOutputStream("Musik/" + uniqueZipName);
		ZipOutputStream zipOutputStream = new ZipOutputStream(fos);
		for (String path : filePaths) {
			File fileToZip = new File(path);
			FileInputStream fis = new FileInputStream(fileToZip);
			ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
			zipOutputStream.putNextEntry(zipEntry);

			byte[] bytes = new byte[1024];
			int length;
			while ((length = fis.read(bytes)) >= 0) {
				zipOutputStream.write(bytes, 0, length);
			}
			fis.close();
		}
		zipOutputStream.close();
		fos.close();

		return "Musik\\" + uniqueZipName;
	}

	private void startAudioStripperProcess(AudioStripperProcess process, Model model) {
		int pid = processes.size();
		model.addAttribute("pid", pid);
		processes.put(pid, process);
		model.addAttribute("maxProgress", process.getMaxProgress());
		new Thread() {
			@Override
			public void run() {
				process.start();
			}
		}.start();
	}

}
