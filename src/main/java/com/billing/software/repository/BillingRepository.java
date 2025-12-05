package com.billing.software.repository;

import com.billing.software.entity.BillingRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingRepository extends JpaRepository<BillingRequest , Long> {
}
