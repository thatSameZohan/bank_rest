package com.bank.dto;

public record RegisterRequest(String username, String password, boolean isAdmin) {}
