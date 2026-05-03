package com.aegira.loan.loanproduct.service;

import com.aegira.loan.common.exception.NotFoundException;
import com.aegira.loan.loanproduct.dto.LoanProductRequest;
import com.aegira.loan.loanproduct.dto.LoanProductResponse;
import com.aegira.loan.loanproduct.entity.LoanProduct;
import com.aegira.loan.loanproduct.repository.LoanProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanProductService {
    private final LoanProductRepository loanProductRepository;

    @Transactional
    public LoanProductResponse create(LoanProductRequest request) {
        LoanProduct product = new LoanProduct();
        fill(product, request);
        return toResponse(loanProductRepository.save(product));
    }

    public List<LoanProductResponse> findAll() {
        return loanProductRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public LoanProduct get(UUID id) {
        return loanProductRepository.findById(id).orElseThrow(() -> new NotFoundException("Loan product not found"));
    }

    public LoanProductResponse findById(UUID id) {
        return toResponse(get(id));
    }

    @Transactional
    public LoanProductResponse update(UUID id, LoanProductRequest request) {
        LoanProduct product = get(id);
        fill(product, request);
        return toResponse(product);
    }

    @Transactional
    public void delete(UUID id) {
        LoanProduct product = get(id);
        product.setActive(false);
    }

    private void fill(LoanProduct product, LoanProductRequest request) {
        product.setName(request.getName());
        product.setMinAmount(request.getMinAmount());
        product.setMaxAmount(request.getMaxAmount());
        product.setMinTenure(request.getMinTenure());
        product.setMaxTenure(request.getMaxTenure());
        product.setAnnualInterestRate(request.getAnnualInterestRate());
        product.setMinimumIncome(request.getMinimumIncome());
        product.setMaximumDsr(request.getMaximumDsr());
        product.setNeedCollateral(request.getNeedCollateral());
        product.setActive(request.getActive());
    }

    public LoanProductResponse toResponse(LoanProduct product) {
        return LoanProductResponse.builder()
                .id(product.getId()).name(product.getName()).minAmount(product.getMinAmount()).maxAmount(product.getMaxAmount())
                .minTenure(product.getMinTenure()).maxTenure(product.getMaxTenure())
                .annualInterestRate(product.getAnnualInterestRate()).minimumIncome(product.getMinimumIncome())
                .maximumDsr(product.getMaximumDsr()).needCollateral(product.getNeedCollateral()).active(product.getActive())
                .createdAt(product.getCreatedAt()).updatedAt(product.getUpdatedAt()).build();
    }
}
