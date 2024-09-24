package com.dtaquito_backend.dtaquito_backend.payments.infrastructure.persistance.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dtaquito_backend.dtaquito_backend.payments.domain.model.aggregates.Payments;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentsRepository extends JpaRepository<Payments, Long> {

    Optional<Payments> findByTransactionId(String transactionId);

    @Query("SELECT p FROM Payments p WHERE p.userId = :userId AND p.paymentStatus = :paymentStatus")
    List<Payments> findByUserIdAndPaymentStatus(@Param("userId") Long userId, @Param("paymentStatus") String paymentStatus);
}
