package com.aegira.loan.customer.service;

import com.aegira.loan.common.exception.BadRequestException;
import com.aegira.loan.common.exception.NotFoundException;
import com.aegira.loan.customer.dto.CustomerRequest;
import com.aegira.loan.customer.dto.CustomerResponse;
import com.aegira.loan.customer.entity.Customer;
import com.aegira.loan.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        if (customerRepository.existsByNik(request.getNik())) {
            throw new BadRequestException("Customer NIK already exists");
        }
        Customer customer = new Customer();
        fill(customer, request);
        return toResponse(customerRepository.save(customer));
    }

    public List<CustomerResponse> findAll() {
        return customerRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public CustomerResponse findById(UUID id) {
        return toResponse(get(id));
    }

    @Transactional
    public CustomerResponse update(UUID id, CustomerRequest request) {
        Customer customer = get(id);
        fill(customer, request);
        return toResponse(customer);
    }

    public boolean duplicateNik(String nik) {
        return customerRepository.existsByNik(nik);
    }

    public Customer get(UUID id) {
        return customerRepository.findById(id).orElseThrow(() -> new NotFoundException("Customer not found"));
    }

    private void fill(Customer customer, CustomerRequest request) {
        customer.setNik(request.getNik());
        customer.setName(request.getName());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setAddress(request.getAddress());
        customer.setMaritalStatus(request.getMaritalStatus());
        customer.setJobType(request.getJobType());
        customer.setMonthlyIncome(request.getMonthlyIncome());
        customer.setMonthlyExpense(request.getMonthlyExpense());
        customer.setExistingInstallment(request.getExistingInstallment());
    }

    public CustomerResponse toResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId()).nik(customer.getNik()).name(customer.getName())
                .phoneNumber(customer.getPhoneNumber()).dateOfBirth(customer.getDateOfBirth())
                .address(customer.getAddress()).maritalStatus(customer.getMaritalStatus()).jobType(customer.getJobType())
                .monthlyIncome(customer.getMonthlyIncome()).monthlyExpense(customer.getMonthlyExpense())
                .existingInstallment(customer.getExistingInstallment())
                .createdAt(customer.getCreatedAt()).updatedAt(customer.getUpdatedAt()).build();
    }
}
