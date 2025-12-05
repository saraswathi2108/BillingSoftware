package com.billing.software.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.billing.software.entity.PasswordResetOTP;

@Repository
public interface PasswordRepository extends JpaRepository<PasswordResetOTP,Long> {
	
    @Modifying
    @Query("DELETE FROM PasswordResetOTP WHERE username = :username")
    void deleteByUsername(String username);

    Optional<PasswordResetOTP> findByUsername(String username);
}