/*
 * SteVe - SteckdosenVerwaltung - https://github.com/steve-community/steve
 * Copyright (C) 2013-2026 SteVe Community Team
 * All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package de.rwth.idsg.steve.web.api;

import de.rwth.idsg.steve.SteveException;
import de.rwth.idsg.steve.repository.dto.Transaction;
import de.rwth.idsg.steve.service.MeterValueService;
import de.rwth.idsg.steve.service.TransactionService;
import de.rwth.idsg.steve.web.api.ApiControllerAdvice.ApiErrorResponse;
import de.rwth.idsg.steve.web.dto.MeterValueDto;
import de.rwth.idsg.steve.web.dto.TransactionQueryForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Sevket Goekay <sevketgokay@gmail.com>
 * @since 13.09.2022
 */
@Tag(name = "transaction-controller",
    description = """
        Operations related to querying transactions.
        A transaction represents a charging session at a charge box (i.e. charging station. The notions 'charge box' and 'charging station' are being used interchangeably).
        """
)
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class TransactionsRestController {

    private final TransactionService transactionService;
    private final MeterValueService meterValueService;

    @Operation(description = """
        Returns a list of transactions based on the query parameters.
        The query parameters can be used to filter the transactions.
        """)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))}),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))})}
    )
    @GetMapping(value = "")
    public List<Transaction> get(@Valid @ParameterObject TransactionQueryForm.TransactionQueryFormForApi params) {
        log.debug("Read request for query: {}", params);

        if (params.isReturnCSV()) {
            throw new SteveException.BadRequest("returnCSV=true is not supported for API calls");
        }

        var response = transactionService.getTransactions(params);
        log.debug("Read response for query: {}", response);
        return response;
    }

    @Operation(description = """
        Returns the meter values for a single transaction.
        Allows specifying the number of values to return and the sort order (by timestamp).
        """)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved meter values"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))}),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Transaction not found", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))})}
    )
    @GetMapping(value = "/{transactionId}/metervalues")
    public List<MeterValueDto> getMeterValues(
        @Parameter(description = "ID of the transaction to retrieve meter values for", required = true)
        @PathVariable int transactionId,

        @Parameter(description = "The number of meter values to return. If not specified, all values are returned.")
        @RequestParam(value = "size", required = false) Integer size,

        @Parameter(description = "The sort order for the meter values by timestamp. 'asc' for oldest first, 'desc' for most recent first.",
                   schema = @Schema(type = "string", allowableValues = {"asc", "desc"}, defaultValue = "desc"))
        @RequestParam(value = "sort", defaultValue = "desc") String sort
    ) {
        log.debug("Read request for meter values for transactionId: {}, size: {}, sort: {}", transactionId, size, sort);

        if (!"asc".equalsIgnoreCase(sort) && !"desc".equalsIgnoreCase(sort)) {
            throw new SteveException.BadRequest("Invalid 'sort' parameter. Allowed values are 'asc' or 'desc'.");
        }

        return meterValueService.getMeterValuesForTransaction(transactionId, size, sort);
    }
}
