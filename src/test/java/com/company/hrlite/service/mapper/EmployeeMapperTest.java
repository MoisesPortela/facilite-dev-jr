package com.company.hrlite.service.mapper;

import static com.company.hrlite.domain.EmployeeAsserts.*;
import static com.company.hrlite.domain.EmployeeTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EmployeeMapperTest {

    private EmployeeMapper employeeMapper;

    @BeforeEach
    void setUp() {
        employeeMapper = new EmployeeMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getEmployeeSample1();
        var actual = employeeMapper.toEntity(employeeMapper.toDto(expected));
        assertEmployeeAllPropertiesEquals(expected, actual);
    }
}
