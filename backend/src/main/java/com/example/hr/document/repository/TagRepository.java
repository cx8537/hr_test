package com.example.hr.document.repository;

import com.example.hr.document.entity.Tag;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {

	Optional<Tag> findByName(String name);
}
