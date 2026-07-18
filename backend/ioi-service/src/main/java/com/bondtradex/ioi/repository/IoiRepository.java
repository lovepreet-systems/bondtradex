package com.bondtradex.ioi.repository;

import com.bondtradex.ioi.entity.Ioi;
import com.bondtradex.ioi.entity.IoiStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IoiRepository extends JpaRepository<Ioi, UUID> {

    Optional<Ioi> findByIoiNumber(String ioiNumber);

    Page<Ioi> findByClientId(UUID clientId, Pageable pageable);

    Page<Ioi> findByStatus(IoiStatus status, Pageable pageable);

    boolean existsByIoiNumber(String ioiNumber);
}