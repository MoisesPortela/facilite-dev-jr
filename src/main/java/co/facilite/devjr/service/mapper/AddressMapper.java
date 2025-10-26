package co.facilite.devjr.service.mapper;

import co.facilite.devjr.domain.Address;
import co.facilite.devjr.service.dto.AddressDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Address} and its DTO {@link AddressDTO}.
 */
@Mapper(componentModel = "spring", config = MapperConfiguration.class)
public interface AddressMapper extends EntityMapper<AddressDTO, Address> {}
