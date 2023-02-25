package ru.trae.backend.dto.manager;

/**
 * ManagerDto is a data transfer object (DTO) class used to encapsulate data related to a manager.
 *
 * @author Vladimir Olennikov
 */
public record ManagerDto(
        long id,
        String firstName,
        String middleName,
        String lastName,
        Long phone,
        String email,
        String role,
        String dateOfRegister
) {
}
