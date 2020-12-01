package com.project.tim7.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import com.project.tim7.model.Newsletter;

@Repository
public interface NewsletterRepository extends JpaRepository<Newsletter, Integer> {
	
	@Query("SELECT n FROM Newsletter n JOIN n.culturalOffer co JOIN co.subscribed s WHERE s.id = ?1")
	public List<Newsletter> findNewsletterForUser(int idRegisteredUser);

	@Query("SELECT n FROM Newsletter n JOIN n.culturalOffer co JOIN co.subscribed s WHERE s.id = ?1")
	public Page<Newsletter> findNewsletterForUser(int idRegisteredUser, Pageable pageable);
}
