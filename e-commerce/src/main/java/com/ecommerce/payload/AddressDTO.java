package com.ecommerce.payload;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {

    private Integer Id;
    private String street;
    private String buildingName;
    private String city;
    private String state;
    private String country;

}
