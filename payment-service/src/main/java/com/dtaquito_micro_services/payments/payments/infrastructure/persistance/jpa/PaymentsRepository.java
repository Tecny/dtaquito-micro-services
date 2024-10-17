package com.dtaquito_micro_services.payments.payments.infrastructure.persistance.jpa;

import com.dtaquito_micro_services.payments.payments.domain.model.aggregates.Payments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentsRepository extends JpaRepository<Payments, Long> {

    Optional<Payments> findByTransactionId(String transactionId);

    @Query(value = "SELECT p FROM Payments p WHERE p.userId = :userId ORDER BY p.id DESC")
    List<Payments> findLatestPaymentByUserId(@Param("userId") String userId);

}
