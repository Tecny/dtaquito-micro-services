package com.dtaquito_micro_services.payments.payments.domain.services;

import com.dtaquito_micro_services.payments.payments.domain.model.aggregates.Payments;
import com.dtaquito_micro_services.payments.payments.domain.model.queries.GetPaymentsByIdQuery;

import java.util.Optional;


public interface PaymentsQueryService {

    Optional<Payments> handle(GetPaymentsByIdQuery query);
}
