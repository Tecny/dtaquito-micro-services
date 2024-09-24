package com.dtaquito_backend.dtaquito_backend.payments.domain.services;

import java.util.Optional;

import com.dtaquito_backend.dtaquito_backend.payments.domain.model.aggregates.Payments;
import com.dtaquito_backend.dtaquito_backend.payments.domain.model.queries.GetPaymentsByIdQuery;


public interface PaymentsQueryService {

    Optional<Payments> handle(GetPaymentsByIdQuery query);
}
