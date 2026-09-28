package com.gharnata.repository;

import com.gharnata.entity.FactureSequence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepFacSequ extends JpaRepository<FactureSequence, Long> {
}
