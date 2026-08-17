package com.ecommerce.project.services;

import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.payload.OrderDTO;

import java.util.List;


public interface AddressService {
    AddressDTO createAddress(AddressDTO addressDTO, User user);
    List<AddressDTO> getAllAddresses();

    AddressDTO getAddresssById(Long addressId);

    List<AddressDTO> getUserAddresses(User user);

    AddressDTO updateAddresss(Long addressId, AddressDTO addressDTO);

    String deleteAddress(Long addressId);

    List<AddressDTO> getMyAddresses();
}
