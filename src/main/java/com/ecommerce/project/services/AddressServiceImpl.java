package com.ecommerce.project.services;

import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.repositories.AddressRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.utils.AuthUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {
    @Autowired
    AddressRepository addressRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthUtils authUtils;

    @Autowired
    ModelMapper modelMapper;

    public AddressDTO createAddress(AddressDTO addressDTO, User user) {
        Address address = modelMapper.map(addressDTO , Address.class);

        List<Address> addressList = user.getAddresses();
        addressList.add(address);
        user.setAddresses(addressList);

        address.setUser(user);

        Address newAddress = addressRepository.save(address);
        return modelMapper.map(newAddress, AddressDTO.class);
    }

    public List<AddressDTO> getAllAddresses(){
        List<Address> addresses = addressRepository.findAll();
        return addresses.stream().map(address -> modelMapper.map(address, AddressDTO.class)).toList();
    }

    @Override
    public AddressDTO getAddresssById(Long addressId) {
        Address address = addressRepository.findById(addressId).orElseThrow(()-> new ResourceNotFoundException("Address","id", addressId ));
        return modelMapper.map(address, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getUserAddresses(User user) {
        List<Address> addresses = user.getAddresses();
        return addresses.stream().map(address -> modelMapper.map(address, AddressDTO.class)).toList();
    }

    @Override
    public AddressDTO updateAddresss(Long addressId, AddressDTO addressDTO) {
        Address addressFromDB = addressRepository.findById(addressId).orElseThrow(()-> new ResourceNotFoundException("Address","id", addressId));
        addressFromDB.setCity(addressDTO.getCity());
        addressFromDB.setCountry(addressDTO.getCountry());
        addressFromDB.setStreet(addressDTO.getStreet());
        addressFromDB.setState(addressDTO.getState());
        addressFromDB.setZipcode(addressDTO.getZipcode());
        addressDTO.setBuilding(addressDTO.getBuilding());

        Address updatedAddress = addressRepository.save(addressFromDB);

        User user = addressFromDB.getUser();

        user.getAddresses().removeIf(address -> address.getAddressId().equals(addressFromDB.getAddressId()));

        user.getAddresses().add(updatedAddress);

        userRepository.save(user);

        return modelMapper.map(updatedAddress, AddressDTO.class);
    }

    @Override
    public String deleteAddress(Long addressId) {
         Address address = addressRepository.findById(addressId).orElseThrow(()-> new ResourceNotFoundException("Address","id", addressId));

         User user = address.getUser();
         user.getAddresses().removeIf(userAddress -> userAddress.getAddressId().equals(addressId));
         userRepository.save(user);

         addressRepository.delete(address);
         return "Address deleted successfully with id: " + addressId;
    }
}
