package com.dtaquito_backend.dtaquito_backend.iam.interfaces.rest.resources;

import java.util.List;

public record SignUpResource(String name, String email, String password, List<String> roles, String bankAccount) {
}