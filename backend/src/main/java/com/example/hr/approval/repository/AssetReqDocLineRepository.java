package com.example.hr.approval.repository;

import com.example.hr.approval.entity.AssetReqDocLine;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetReqDocLineRepository extends JpaRepository<AssetReqDocLine, Long> {

	List<AssetReqDocLine> findByDocumentId(Long documentId);
}
