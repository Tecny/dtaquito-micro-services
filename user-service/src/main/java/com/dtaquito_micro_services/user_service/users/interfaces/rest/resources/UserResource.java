package com.dtaquito_micro_services.user_service.users.interfaces.rest.resources;

import java.math.BigDecimal;

public record UserResource(Long id, String name, String email, String password, Long roleId, String roleType, String bankAccount, BigDecimal credits) { }
