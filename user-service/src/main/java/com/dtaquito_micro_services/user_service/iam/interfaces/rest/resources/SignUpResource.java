package com.dtaquito_micro_services.user_service.iam.interfaces.rest.resources;

import java.util.List;

public record SignUpResource(String name, String email, String password, List<String> roles, String bankAccount) {
}