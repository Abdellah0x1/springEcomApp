package com.ecommerce.project.services;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.repositories.CartItemRepository;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.utils.AuthUtils;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    AuthUtils authUtils;
    @Autowired
    private CartItemRepository cartItemRepository;


    @Override
    public CartDTO addProductToCart(Long ProductId, Integer quantity) {
        //find existing cart or create one
        Cart cart = createCart();
        //retrieve Product Details
        Product product = productRepository.findById(ProductId).orElseThrow(()-> new ResourceNotFoundException("Product", "ProductId", ProductId));
        // Check if product exists on user cart
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cart.getId(),ProductId);

        if(cartItem != null){
            throw new APIException("Product "+ product.getProductName() + " already exists in the cart");
        }
        //check product quantity
        if(product.getQuantity() == 0){
            throw new APIException("Product "+ product.getProductName() + " is not available");
        }

        if(product.getQuantity() < quantity){
            throw new APIException("Please make an order of the " + product.getProductName() + " less than or  equal to " + product.getQuantity());
        }

        CartItem newCartItem = new CartItem();
        newCartItem.setProduct(product);
        newCartItem.setCart(cart);
        newCartItem.setQuantity(quantity);
        newCartItem.setDiscount(product.getDiscount());
        newCartItem.setProductPrice(product.getSpecialPrice());

        //save cart item
        cartItemRepository.save(newCartItem);


        product.setQuantity(product.getQuantity());

        cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice() * quantity));

        cartRepository.save(cart);

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<CartItem> cartItems = cart.getCartItems();

        Stream<ProductDTO> productDTOStream = cartItems.stream().map(item -> {
            ProductDTO map = modelMapper.map(item, ProductDTO.class);
            map.setQuantity(item.getQuantity());
            return map;
        });
        cartDTO.setProducts(productDTOStream.toList());

        //return updated cart info
        return cartDTO;
    }


    private Cart createCart(){
        Cart userCart = cartRepository.findCartByEmail(authUtils.loggedInEmail());
        if(userCart != null){
            return userCart;
        }
        //if cart does not exist create one
        Cart newCart = new Cart();
        newCart.setTotalPrice(0.00);
        newCart.setUser(authUtils.loggedInUser());
        return cartRepository.save(newCart);
    }

    @Override
    public List<CartDTO> getAllCarts(){
        List<Cart> carts = cartRepository.findAll();
        if(carts.isEmpty()){
            throw new APIException("No Carts were found");
        }

        List<CartDTO> cartDTOS = carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
            List<ProductDTO> productDTOS = cart.getCartItems().stream().map(item -> modelMapper.map(item.getProduct(), ProductDTO.class)).toList();
            cartDTO.setProducts(productDTOS);
            return cartDTO;
        }).toList();
        return cartDTOS;
    }
    @Override
    public CartDTO getCart(String emailId, Long cartId){
        Cart cart = cartRepository.findCartByEmailAndCartId(emailId, cartId);
        if(cart == null){
            throw new ResourceNotFoundException("Cart", "CartId", cartId);
        }
        CartDTO cartDTO =modelMapper.map(cart, CartDTO.class);
        List<ProductDTO> productDTOS = cart.getCartItems().stream().map(item-> modelMapper.map(item.getProduct(), ProductDTO.class)).toList();
        cartDTO.setProducts(productDTOS);
        return cartDTO;
    }

    @Transactional
    @Override
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity){
        Cart userCart = cartRepository.findCartByEmail(authUtils.loggedInEmail());
        Long cartId = userCart.getId();
        Cart cart = cartRepository.findById(cartId).orElseThrow(()-> new ResourceNotFoundException("Cart", "CartId", cartId));

        Product product = productRepository.findById(productId).orElseThrow(()-> new ResourceNotFoundException("Product", "ProductId", productId));
        if(product.getQuantity() == 0){
            throw new APIException("Product "+ product.getProductName() + " is not available");
        }
        if(product.getQuantity() < quantity){
            throw new APIException("Please make an order of the  " + product.getProductName() + " less than or  equal to the quantity " + product.getQuantity());
        }



        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);
        if(cartItem == null){
            throw new APIException("Product "+ product.getProductName() + " does not exist in the cart");
        }

        int newQuantity = cartItem.getQuantity() + quantity;
        if(newQuantity < 0){
            throw new APIException("The resulting quantity cannot be negative");
        }

        cartItem.setQuantity(cartItem.getQuantity() + quantity);
        cartItem.setProductPrice(product.getSpecialPrice());
        cartItem.setDiscount(product.getDiscount());
        cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice() * quantity));
        cartRepository.save(cart);
        CartItem updatedCartItem = cartItemRepository.save(cartItem);
        if(updatedCartItem.getQuantity() == 0){
            cartItemRepository.deleteById(updatedCartItem.getId());
        }
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<CartItem> cartItems = cart.getCartItems();

        Stream<ProductDTO> productStream = cartItems.stream().map(item -> {
            ProductDTO map = modelMapper.map(item.getProduct(), ProductDTO.class);
            map.setQuantity(item.getQuantity());
            map.setDiscount(item.getDiscount());
            map.setQuantity(item.getQuantity());
            return map;
        });

        cartDTO.setProducts(productStream.toList());
        return cartDTO;
    }

    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(()-> new ResourceNotFoundException("Cart", "CartId", cartId));
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

        if(cartItem == null){
            throw new ResourceNotFoundException("Product", "ProductId", productId);
        }
        cart.setTotalPrice(cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity()));
        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId, productId);

        return "Product " + cartItem.getProduct().getProductName() + " has been deleted";
    }

    @Override
    public void updateProductsInCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(()-> new ResourceNotFoundException("Cart", "CartId", cartId));

        Product product = productRepository.findById(productId).orElseThrow(()-> new ResourceNotFoundException("Product", "ProductId", productId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

        if(cartItem == null) {
            throw new APIException("Product "+ product.getProductName() + " does not exist in the cart");
        }

        double cartPrice = cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity());

        cartItem.setProductPrice(product.getSpecialPrice());
        cart.setTotalPrice(cartItem.getProductPrice() + (product.getSpecialPrice() * cartItem.getQuantity()));

        cartItem =  cartItemRepository.save(cartItem);
    }
}
