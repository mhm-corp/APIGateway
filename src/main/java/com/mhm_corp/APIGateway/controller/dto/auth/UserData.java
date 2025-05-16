package com.mhm_corp.APIGateway.controller.dto.auth;

import lombok.*;

import java.time.LocalDate;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserData {
    private String idCard;
    private String username;
    private String firstName;
    private String lastName;
    private String address;
    private String email;
    private LocalDate birthdate;
    private String phoneNumber;

}
