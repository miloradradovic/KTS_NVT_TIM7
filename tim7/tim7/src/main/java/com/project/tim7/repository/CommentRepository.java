package com.project.tim7.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.project.tim7.model.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer>  {

	@Query(value = "SELECT * from comments where comments.cultural_offer_id = ?1", nativeQuery = true)
    Page<Comment> findCommentsOfCulturalOffer(int id, Pageable pageable);
}
