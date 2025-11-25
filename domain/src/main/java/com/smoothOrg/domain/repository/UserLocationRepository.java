package com.smoothOrg.domain.repository;

import com.smoothOrg.domain.entity.UserLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserLocationRepository extends JpaRepository<UserLocation, Long> {
    List<UserLocation> findByUserId(Long userId);
    Optional<UserLocation> findByUserIdAndIsDefaultTrue(Long userId);
}
