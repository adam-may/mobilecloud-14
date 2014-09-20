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

package org.magnum.mobilecloud.video;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
	
	// Guide: https://class.coursera.org/mobilecloud-001/forum/thread?thread_id=924
	
	@Autowired
	private VideoRepository _videoRepository;
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList() {
		return (Collection<Video>) _videoRepository.findAll();
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v) {
		v.setLikes(0);
		return _videoRepository.save(v);
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}", method=RequestMethod.GET)
	public @ResponseBody Video getVideoById(@PathVariable long id, HttpServletResponse response) {
		Video v = _videoRepository.findOne(id);
		if (v == null)
			response.setStatus(HttpStatus.SC_NOT_FOUND);
		return v;
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}/like", method=RequestMethod.POST)  
	public void likeVideo(@PathVariable long id, Principal principal, HttpServletResponse response) {
		Video v = _videoRepository.findOne(id);
		if (v == null) {
			response.setStatus(HttpStatus.SC_NOT_FOUND);
			return;
		}
		String username = principal.getName();
		if (!v.like(username))
			response.setStatus(HttpStatus.SC_BAD_REQUEST);
		_videoRepository.save(v);
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}/unlike", method=RequestMethod.POST)
	public void unlikeVideo(@PathVariable long id, Principal principal, HttpServletResponse response) {
		Video v = _videoRepository.findOne(id);
		if (v == null) {
			response.setStatus(HttpStatus.SC_NOT_FOUND);
			return;
		}
		String username = principal.getName();
		v.unlike(username);
		_videoRepository.save(v);
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}/likedby", method=RequestMethod.GET)
	public @ResponseBody Collection<String> getUsersWhoLikedVideo(@PathVariable long id, HttpServletResponse response) {
		Video v = _videoRepository.findOne(id);
		if (v == null) {
			response.setStatus(HttpStatus.SC_NOT_FOUND);
			return new ArrayList<String>();
		}
		return v.getUsersWhoLiked();
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_TITLE_SEARCH_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> findByTitle(@RequestParam String title) {
		return _videoRepository.findByName(title);
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_DURATION_SEARCH_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> findByDurationLessThan(@RequestParam long duration) {
		return _videoRepository.findByDurationLessThan(duration);
	}
}
