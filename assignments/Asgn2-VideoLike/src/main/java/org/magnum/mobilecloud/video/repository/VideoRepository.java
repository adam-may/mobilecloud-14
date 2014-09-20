package org.magnum.mobilecloud.video.repository;

import java.util.Collection;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepository extends CrudRepository<Video, Long>{

	// Find all videos with a matching title (e.g., Video.name)
	public Collection<Video> findByName(String title);
	
	// Find all videos that are shorter than a specified duration
	public Collection<Video> findByDurationLessThan(long maxduration);
}
