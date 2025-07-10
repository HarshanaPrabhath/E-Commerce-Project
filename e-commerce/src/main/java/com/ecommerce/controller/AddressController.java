package com.ecommerce.controller;

import com.ecommerce.exceptions.ApiException;
import com.ecommerce.exceptions.ResourceNotFoundException;
import com.ecommerce.model.User;
import com.ecommerce.payload.AddressDTO;
import com.ecommerce.repositories.UserRepository;
import com.ecommerce.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AddressController {

    @Autowired
    AddressService addressService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping(value ="/addresses")
    public ResponseEntity<AddressDTO> addAddress(@Valid  @RequestBody AddressDTO addressDTO){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email).orElseThrow(()-> new ApiException("User Not Found"));

         AddressDTO address = addressService.createAddress(addressDTO,user);

        return new ResponseEntity<AddressDTO>( address, HttpStatus.CREATED);

    }

    @GetMapping(value = "/addresses")
    public ResponseEntity<List<AddressDTO>> getAllAddresses(){

        List<AddressDTO> addressDTOS  = addressService.getAllAddresses();
        return new ResponseEntity<>(addressDTOS,HttpStatus.OK);
    }


    @GetMapping(value = "/address/{addressId}")
    public ResponseEntity<AddressDTO> getAllAddresses(@PathVariable Long addressId){

        AddressDTO addressDTOS  = addressService.getAddress(addressId);
        return new ResponseEntity<>(addressDTOS,HttpStatus.OK);
    }

    @GetMapping(value="users/addresses")
    public ResponseEntity<List<AddressDTO>> getUserAddresses(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user =  userRepository.findByEmail(email).orElseThrow(()->
                new ResourceNotFoundException("User","user_email", email));

        List<AddressDTO> addressDTOS = addressService.getUserAddresses(user);

        return new ResponseEntity<>(addressDTOS,HttpStatus.OK);
    }

    @PutMapping(value = "/address/{addressId}")
    public ResponseEntity<AddressDTO> updateAddress(@PathVariable Long addressId, @RequestBody AddressDTO addressDTO){

        AddressDTO addressDTOS  = addressService.updateAddress(addressId,addressDTO);
        return new ResponseEntity<>(addressDTOS,HttpStatus.OK);
    }

    @DeleteMapping(value = "/address/{addressId}")
    public ResponseEntity<String> deleteAddress(@PathVariable Long addressId){

        String massage = addressService.deleteAddress(addressId);

        return new ResponseEntity<>(massage,HttpStatus.OK);
    }

}
