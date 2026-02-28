/*
 * SteVe - SteckdosenVerwaltung - https://github.com/steve-community/steve
 * Copyright (C) 2013-2026 SteVe Community Team
 * All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package de.rwth.idsg.steve.service;

import de.rwth.idsg.steve.SteveException;
import de.rwth.idsg.steve.repository.TransactionRepository;
import de.rwth.idsg.steve.repository.dto.TransactionDetails;
import de.rwth.idsg.steve.web.dto.MeterValueDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class MeterValueService {

    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public List<MeterValueDto> getMeterValuesForTransaction(int transactionId, Integer size, String sortDirection) {
        TransactionDetails transactionDetails = transactionRepository.getDetails(transactionId);
        if (transactionDetails == null) {
            throw new SteveException.NotFound("Transaction with id " + transactionId + " not found");
        }

        List<TransactionDetails.MeterValues> meterValues = transactionDetails.getValues();
        if (meterValues == null) {
            return Collections.emptyList();
        }

        // Sorting
        Comparator<TransactionDetails.MeterValues> comparator = Comparator.comparing(TransactionDetails.MeterValues::getValueTimestamp);
        if ("desc".equalsIgnoreCase(sortDirection)) {
            comparator = comparator.reversed();
        }

        Stream<TransactionDetails.MeterValues> stream = meterValues.stream().sorted(comparator);

        // Limiting
        if (size != null) {
            stream = stream.limit(size);
        }

        return stream.map(this::toDto)
                          .collect(Collectors.toList());
    }

    private MeterValueDto toDto(TransactionDetails.MeterValues meterValue) {
        return MeterValueDto.builder()
                            .timestamp(Instant.ofEpochMilli(meterValue.getValueTimestamp().getMillis()))
                            .value(meterValue.getValue())
                            .unit(meterValue.getUnit())
                            .measurand(meterValue.getMeasurand())
                            .location(meterValue.getLocation())
                            .build();
    }
}