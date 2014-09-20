/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.magnum.dataup;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.model.VideoStatus.VideoState;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;


@Controller
public class VideoController {

	/**
	 * You will need to create one or more Spring controllers to fulfill the
	 * requirements of the assignment. If you use this file, please rename it
	 * to something other than "AnEmptyController"
	 * 
	 * 
		 ________  ________  ________  ________          ___       ___  ___  ________  ___  __       
		|\   ____\|\   __  \|\   __  \|\   ___ \        |\  \     |\  \|\  \|\   ____\|\  \|\  \     
		\ \  \___|\ \  \|\  \ \  \|\  \ \  \_|\ \       \ \  \    \ \  \\\  \ \  \___|\ \  \/  /|_   
		 \ \  \  __\ \  \\\  \ \  \\\  \ \  \ \\ \       \ \  \    \ \  \\\  \ \  \    \ \   ___  \  
		  \ \  \|\  \ \  \\\  \ \  \\\  \ \  \_\\ \       \ \  \____\ \  \\\  \ \  \____\ \  \\ \  \ 
		   \ \_______\ \_______\ \_______\ \_______\       \ \_______\ \_______\ \_______\ \__\\ \__\
		    \|_______|\|_______|\|_______|\|_______|        \|_______|\|_______|\|_______|\|__| \|__|
                                                                                                                                                                                                                                                                        
	 * 
	 */
	
	private static final AtomicLong currentId = new AtomicLong(0L);
	private Map<Long,Video> videos = new HashMap<Long,Video>();
	private VideoFileManager videoDataManager;
	
	public VideoController() {
		try {
			videoDataManager = VideoFileManager.get();
		} catch (IOException e) {
			// Do nothing
		}
	}
	
	private void checkAndSetId(Video v)	{
		if (v.getId() == 0) {
			v.setId(currentId.incrementAndGet());
		}
	}
	
	private Video save(Video v)	{
		checkAndSetId(v);
		v.setDataUrl(getDataUrl(v.getId()));
		videos.put(v.getId(), v);
		return v;
	}
	
	private String getDataUrl(long videoId) {
		return getUrlBaseForLocalServer() + VideoSvcApi.VIDEO_SVC_PATH + "/" + videoId + "/" + VideoSvcApi.DATA_PARAMETER;
	}
	
	private String getUrlBaseForLocalServer() {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		return "http://" + request.getServerName() + ((request.getServerPort() != 80) ? ":" + request.getServerPort() : "");
	}
	
	private void saveSomeVideo(Video v, MultipartFile videoData) throws IOException {
		videoDataManager.saveVideoData(v, videoData.getInputStream());
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList() {
		return videos.values();
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v) {
		return save(v);
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_DATA_PATH, method=RequestMethod.POST)
	public @ResponseBody VideoStatus setVideoData(@PathVariable long id, MultipartFile data, HttpServletResponse response) {

		if (videos.containsKey(id)) {
			try {
				saveSomeVideo(videos.get(id), data);
			} catch (IOException e) {
				// Nothing to do
			}
		} else {
			response.setStatus(HttpStatus.NOT_FOUND.value());
		}
		
		return new VideoStatus(VideoState.READY);
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_DATA_PATH, method=RequestMethod.GET)
	public @ResponseBody void getData(@PathVariable long id, HttpServletResponse response) {
		try {
			if (!videos.containsKey(id) || !videoDataManager.hasVideoData(videos.get(id))) {
				response.sendError(HttpStatus.NOT_FOUND.value());
			} else {
					videoDataManager.copyVideoData(videos.get(id),  response.getOutputStream());
			}
		} catch (IOException e) {
			// Do nothing
		}
	}
}