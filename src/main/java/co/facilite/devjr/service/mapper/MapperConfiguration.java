package co.facilite.devjr.service.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * Shared MapStruct configuration to control unmapped target reporting.
 */
@MapperConfig(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MapperConfiguration {}
