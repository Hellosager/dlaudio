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

import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
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
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import AudioStripper.AudioStripper;
import AudioStripper.ConvertingProcess;
import AudioStripper.MultiLinkConvertingProcess;
import AudioStripper.SingleLinkConvertingProcess;

@Controller
public class DownloadController {
	private HashMap<Integer, ConvertingProcess> processes = new HashMap<>();

	@RequestMapping(value = "/convert", method = RequestMethod.GET)
	public String getMP3FromVideo(@RequestParam("link") String link, Model model)
			throws IOException, InterruptedException {
		model.addAttribute("type", "single");
		AudioStripper as = new AudioStripper("Videos", "Musik");
		startConvertingProcess(new SingleLinkConvertingProcess(link, as), model);

		return "progress";
	}
	

	@RequestMapping(value = "/download/{pid}", method = RequestMethod.GET)
	public void downloadResource(@PathVariable(value = "pid") int pid,
			HttpServletResponse response) {
		String resourceName = null;
		ConvertingProcess process = processes.get(pid);
		if(process instanceof SingleLinkConvertingProcess) {
			SingleLinkConvertingProcess slcp = (SingleLinkConvertingProcess) process;
			resourceName = slcp.getMP3Path();
		}else if(process instanceof MultiLinkConvertingProcess) {
			MultiLinkConvertingProcess mlcp = (MultiLinkConvertingProcess) process;
			try {
				resourceName = zipFiles(mlcp.getMP3Paths());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		response.setHeader("Content-Disposition", "attachment;filename=\"" + resourceName + "\""); // TODO HARDCODED
		InputStream stream;
		try {
			stream = new FileInputStream(new File(resourceName)); // TODO HARDCODED
			IOUtils.copy(stream, response.getOutputStream());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(value = "/convert", method = RequestMethod.POST)
	public String getMP3sFromVideo(@ModelAttribute("textFile") MultipartFile textFile, Model model) throws IOException, InterruptedException {
		System.out.println("inmethod");
		model.addAttribute("type", "multi");
		String fileContent = new String(textFile.getBytes());
		String[] links = fileContent.split("\n");
		AudioStripper as = new AudioStripper("Videos", "Musik");
		startConvertingProcess(new MultiLinkConvertingProcess(links, as), model);
		return "progress";
	}

	// TODO find way to get duration from video with ffmpeg output
	// TODO remove hardcode and replace it with logic that calculates processed video
	// from video duration

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

	private String zipFiles(String[] filePaths) throws IOException {
		String uniqueZipName = "converted-mp3s-" + System.currentTimeMillis() + ".zip";
		System.out.println("Storing downloaded files in " + "Musik\\" + uniqueZipName);
		FileOutputStream fos = new FileOutputStream("Musik\\" + uniqueZipName);
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
	
	private void startConvertingProcess(ConvertingProcess process, Model model) {
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
