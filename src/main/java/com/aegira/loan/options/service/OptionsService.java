package com.aegira.loan.options.service;

import com.aegira.loan.customer.entity.Customer;
import com.aegira.loan.customer.repository.CustomerRepository;
import com.aegira.loan.loanproduct.entity.LoanProduct;
import com.aegira.loan.loanproduct.repository.LoanProductRepository;
import com.aegira.loan.options.dto.CustomerOptionResponse;
import com.aegira.loan.options.dto.LoanProductOptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OptionsService {
    private final LoanProductRepository loanProductRepository;
    private final CustomerRepository customerRepository;

    public List<LoanProductOptionResponse> loanProducts() {
        return loanProductRepository.findAll().stream()
                .filter(product -> Boolean.TRUE.equals(product.getActive()))
                .map(this::toProductOption)
                .collect(Collectors.toList());
    }

    public List<CustomerOptionResponse> customers(String keyword) {
        return customerRepository.findAll().stream()
                .filter(customer -> matches(customer, keyword))
                .map(this::toCustomerOption)
                .collect(Collectors.toList());
    }

    private LoanProductOptionResponse toProductOption(LoanProduct product) {
        return LoanProductOptionResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .minAmount(product.getMinAmount())
                .maxAmount(product.getMaxAmount())
                .minTenure(product.getMinTenure())
                .maxTenure(product.getMaxTenure())
                .build();
    }

    private CustomerOptionResponse toCustomerOption(Customer customer) {
        return CustomerOptionResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .nik(customer.getNik())
                .phoneNumber(customer.getPhoneNumber())
                .build();
    }

    private boolean matches(Customer customer, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return true;
        }
        String value = keyword.toLowerCase(Locale.ENGLISH);
        return contains(customer.getName(), value)
                || contains(customer.getNik(), value)
                || contains(customer.getPhoneNumber(), value);
    }

    private boolean contains(String source, String lowerKeyword) {
        return source != null && source.toLowerCase(Locale.ENGLISH).contains(lowerKeyword);
    }
}
