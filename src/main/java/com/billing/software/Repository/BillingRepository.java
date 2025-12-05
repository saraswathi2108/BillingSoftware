package com.billing.software.Repository;

import com.billing.software.Entity.BillingRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingRepository extends JpaRepository<BillingRequest , Long> {
}
