package com.ecommerce.project.services;

import com.ecommerce.project.enums.OrderStatus;
import com.ecommerce.project.enums.PaymentStatus;
import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.*;
import com.ecommerce.project.payload.OrderDTO;
import com.ecommerce.project.repositories.*;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Service
public class OrderServiceImpl implements OrderService{
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;


    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CartService cartService;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    public OrderDTO createOrder(String email, Long addressId) {
        Cart userCart = cartRepository.findCartByEmail(email);
        if(userCart == null){
            throw new ResourceNotFoundException("Cart", "email", email);
        }
        Address address = addressRepository.findById(addressId).orElseThrow(()-> new ResourceNotFoundException("Address", "id", addressId));

        //check if the cart is empty
        if(userCart.getCartItems().isEmpty()){
            throw new APIException("Cart is empty");
        }

        // ceck if demanded quantity demanded is available
        for(CartItem cartItem: userCart.getCartItems()){
            Product product = cartItem.getProduct();
            if(product.getQuantity() < cartItem.getQuantity()){
                throw new APIException("Only " + product.getQuantity() + "of " + product.getProductName() + "remaining");
            }
        }

        Order order = new Order();
        order.setEmail(email);
        order.setAddress(address);
        order.setOrderDate(LocalDate.now());
        order.setOrderStatus(OrderStatus.PENDING_PAYMENT);

        List<OrderItem> orderItems = new ArrayList<>();
        double total = 0;

        // convert cart items to orderItems
        for(CartItem cartItem: userCart.getCartItems()){
            Product product = cartItem.getProduct();
            OrderItem orderItem = new OrderItem();

            orderItem.setProduct(product);
            orderItem.setOrder(order);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setOrderedProductPrice(product.getSpecialPrice());
            orderItem.setDiscount(product.getDiscount());

            orderItems.add(orderItem);
            total += product.getSpecialPrice() * cartItem.getQuantity();
        }
        order.setOrderItems(orderItems);
        order.setTotalAmount(total);

        Payment payment = new Payment();
        payment.setProvider("STRIPE");
        payment.setStatus(PaymentStatus.PENDING);
        payment.setAmount(total);
        payment.setCurrency("usd");

        payment.setOrder(order);
        order.setPayment(payment);


        orderRepository.save(order);

        return modelMapper.map(order, OrderDTO.class);
    }

//    @Override
//    @Transactional
//    public OrderDTO placeOrder(String email, Long addressId, String paymentMethod, String pgName, String pgPaymentId, String pgStatus, String pgResponseMessage) {
//        Cart userCart = cartRepository.findCartByEmail(email);
//        if(userCart == null){
//            throw new ResourceNotFoundException("Cart", "email" , email);
//        }
//        Address address = addressRepository.findById(addressId).orElseThrow(() -> new ResourceNotFoundException("Address", "id" , addressId));
//
//
//        Order order = new Order();
//        order.setEmail(email);
//        order.setAddress(address);
//        order.setOrderDate(LocalDate.now());
//        order.setOrderStatus("Order accepted !");
//
//        Payment payment = new Payment(paymentMethod,pgPaymentId, pgStatus, pgResponseMessage,pgName);
//        payment = paymentRepository.save(payment);
//        order.setPayment(payment);
//
//
//        List<CartItem> cartItems = userCart.getCartItems();
//        if(cartItems.isEmpty()){
//            throw new APIException("Cart is empty");
//        }
//
//        List<OrderItem> orderItems = new ArrayList<>();
//
//        for(CartItem cartItem: cartItems){
//            OrderItem orderItem = new OrderItem();
//            orderItem.setProduct(cartItem.getProduct());
//            orderItem.setQuantity(cartItem.getQuantity());
//            orderItem.setDiscount(cartItem.getDiscount());
//            orderItem.setOrderedProductPrice(cartItem.getProductPrice());
//            orderItem.setOrder(order);
//
//            orderItems.add(orderItem);
//        }
//
//        orderItemRepository.saveAll(orderItems);
//        order.setOrderItems(orderItems);
//
//        //SAVE ORDER
//        orderRepository.save(order);
//
//        //update the stock
//        userCart.getCartItems().forEach(item-> {
//            int quantity = item.getQuantity();
//            Product product = item.getProduct();
//            product.setQuantity(product.getQuantity() - quantity);
//            productRepository.save(product);
//
//            //clear the cart
//            cartService.deleteProductFromCart(userCart.getId(), product.getProductId());
//        });
//
//        OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);
//        orderItems.forEach(orderItem -> {
//            orderDTO.getOrderItems().add(modelMapper.map(orderItem, OrderItemDTO.class));
//        });
//        orderDTO.setAddressId(addressId);
//
//        return orderDTO;
//    }

}
