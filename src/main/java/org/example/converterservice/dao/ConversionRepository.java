package org.example.converterservice.dao;

import org.example.converterservice.model.entity.Conversion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversionRepository extends JpaRepository<Conversion, Long> {
}