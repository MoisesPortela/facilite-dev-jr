package co.facilite.devjr.service.mapper;

import co.facilite.devjr.domain.Address;
import co.facilite.devjr.domain.Department;
import co.facilite.devjr.domain.Employee;
import co.facilite.devjr.service.dto.AddressDTO;
import co.facilite.devjr.service.dto.DepartmentDTO;
import co.facilite.devjr.service.dto.EmployeeDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Employee} and its DTO {@link EmployeeDTO}.
 */
@Mapper(componentModel = "spring", config = MapperConfiguration.class)
public interface EmployeeMapper extends EntityMapper<EmployeeDTO, Employee> {
    @Mapping(target = "address", source = "address", qualifiedByName = "addressId")
    @Mapping(target = "department", source = "department", qualifiedByName = "departmentName")
    EmployeeDTO toDto(Employee s);

    @Named("addressId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    AddressDTO toDtoAddressId(Address address);

    @Named("departmentName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    DepartmentDTO toDtoDepartmentName(Department department);
}
