package com.covenantcode.crm.repository;

import com.covenantcode.crm.entity.Lead;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeadRepository extends JpaRepository<Lead, Long> {
}
