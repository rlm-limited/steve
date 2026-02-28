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
package de.rwth.idsg.steve.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Schema(description = "Represents a single meter value reading during a transaction.")
@Getter
@Builder
public class MeterValueDto {

    @Schema(description = "The timestamp of the meter value reading.", requiredMode = Schema.RequiredMode.REQUIRED, example = "2023-10-27T10:00:00Z")
    private final Instant timestamp;

    @Schema(description = "The value of the reading.", requiredMode = Schema.RequiredMode.REQUIRED, example = "1500.0")
    private final String value;

    @Schema(description = "The unit of the reading.", example = "Wh")
    private final String unit;

    @Schema(description = "The type of measurement.", example = "Energy.Active.Import.Register")
    private final String measurand;

    @Schema(description = "The location of the measurement.", example = "Outlet")
    private final String location;
}