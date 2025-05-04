package com.ecommerce.payload;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class



CategoryDTO {

    private Long categoryId;

    @NotBlank
    @Size(min =5, message = "category name must have 5 characters as min")
    private String categoryName;
}


