package com.safeway.tech.repository;

import com.safeway.tech.domain.models.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EnderecoRepository extends JpaRepository<Address, UUID> {
}
