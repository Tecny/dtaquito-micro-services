package com.dtaquito_micro_services.payments.payments.application.internal.queryservices;

import com.dtaquito_micro_services.payments.payments.domain.model.aggregates.Payments;
import com.dtaquito_micro_services.payments.payments.domain.model.queries.GetPaymentsByIdQuery;
import com.dtaquito_micro_services.payments.payments.domain.services.PaymentsQueryService;
import com.dtaquito_micro_services.payments.payments.infrastructure.persistance.jpa.PaymentsRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PaymentsQueryServiceImpl implements PaymentsQueryService {

    private final PaymentsRepository paymentsRepository;

    public PaymentsQueryServiceImpl(PaymentsRepository paymentsRepository) {
        this.paymentsRepository = paymentsRepository;
    }

    @Override
    public Optional<Payments> handle(GetPaymentsByIdQuery query) {
        return paymentsRepository.findById(query.id());
    }
}
