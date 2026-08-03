package com.ecommerce.project.services;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductImageDTO;
import com.ecommerce.project.repositories.CartItemRepository;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.utils.AuthUtils;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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


    @Transactional
    @Override
    public CartDTO addProductToCart(Long ProductId, Integer quantity) {
        //find existing cart or create one
        Cart cart = createCart();
        //retrieve Product Details
        Product product = productRepository.findById(ProductId).orElseThrow(()-> new ResourceNotFoundException("Product", "ProductId", ProductId));
        if(quantity <= 0){
            throw new APIException("Quantity must be greater than 0");
        }
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
        cart.getCartItems().add(newCartItem);

        cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice() * quantity));

        cartRepository.save(cart);

        //return updated cart info
        return mapCartToDTO(cart);
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

    @Transactional
    @Override
    public List<CartDTO> getAllCarts(){
        List<Cart> carts = cartRepository.findAll();
        if(carts.isEmpty()){
            throw new APIException("No Carts were found");
        }

        return carts.stream().map(this::mapCartToDTO).toList();
    }

    @Transactional
    @Override
    public CartDTO getCart(String emailId, Long cartId){
        Cart cart = cartRepository.findCartByEmailAndCartId(emailId, cartId);
        if(cart == null){
            throw new ResourceNotFoundException("Cart", "CartId", cartId);
        }

        return mapCartToDTO(cart);
    }

    @Transactional
    @Override
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity){
        Cart userCart = cartRepository.findCartByEmail(authUtils.loggedInEmail());
        Long cartId = userCart.getId();
        Cart cart = cartRepository.findById(cartId).orElseThrow(()-> new ResourceNotFoundException("Cart", "CartId", cartId));

        Product product = productRepository.findById(productId).orElseThrow(()-> new ResourceNotFoundException("Product", "ProductId", productId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);
        if(cartItem == null){
            throw new APIException("Product "+ product.getProductName() + " does not exist in the cart");
        }

        int newQuantity = cartItem.getQuantity() + quantity;
        if(newQuantity < 0){
            throw new APIException("The resulting quantity cannot be negative");
        }
        if(quantity > 0 && product.getQuantity() < newQuantity){
            throw new APIException("Please make an order of the  " + product.getProductName() + " less than or  equal to the quantity " + product.getQuantity());
        }

        cartItem.setQuantity(cartItem.getQuantity() + quantity);
        cartItem.setProductPrice(product.getSpecialPrice());
        cartItem.setDiscount(product.getDiscount());
        cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice() * quantity));
        cartRepository.save(cart);
        CartItem updatedCartItem = cartItemRepository.save(cartItem);
        if(updatedCartItem.getQuantity() == 0){
            cart.getCartItems().removeIf(item -> item.getId().equals(updatedCartItem.getId()));
            cartItemRepository.deleteById(updatedCartItem.getId());
        }
        return mapCartToDTO(cart);
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

    @Transactional
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
        cart.setTotalPrice(cartPrice + (product.getSpecialPrice() * cartItem.getQuantity()));

        cartRepository.save(cart);
        cartItemRepository.save(cartItem);
    }

    private CartDTO mapCartToDTO(Cart cart) {
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<ProductDTO> productDTOS = cart.getCartItems().stream()
                .map(this::mapCartItemToProductDTO)
                .toList();
        cartDTO.setProducts(productDTOS);
        return cartDTO;
    }

    private ProductDTO mapCartItemToProductDTO(CartItem item) {
        ProductDTO productDTO = modelMapper.map(item.getProduct(), ProductDTO.class);
        productDTO.setQuantity(item.getQuantity());
        productDTO.setDiscount(item.getDiscount());
        productDTO.setSpecialPrice(item.getProductPrice());
        List<ProductImageDTO> productImages = item.getProduct().getProductImages().stream()
                .map(image -> modelMapper.map(image, ProductImageDTO.class))
                .toList();
        productDTO.setImages(productImages);
        return productDTO;
    }
}
