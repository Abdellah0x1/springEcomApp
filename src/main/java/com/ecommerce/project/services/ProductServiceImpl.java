package com.ecommerce.project.services;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.model.ProductImage;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductImageDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.ProductRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ModelMapper mapper;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private FileService fileService;

    @Value("${project.image}")
    private String path;
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartService cartService;



    @Autowired
    private CloudinaryService cloudinaryService;

    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("asc")? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sort);
        Page<Product> productPage = productRepository.findAll(pageDetails);


        List<Product> products = productPage.getContent();
        List<ProductDTO> productDTOS = products.stream().map(product ->{
            ProductDTO productDTO =  mapper.map(product, ProductDTO.class);
            List<ProductImageDTO> productDTOImages = product.getProductImages().stream().map(productImage -> mapper.map(productImage, ProductImageDTO.class)).toList();
            productDTO.setImages(productDTOImages);
            return productDTO;
        }).toList();

        if(products.isEmpty()){
            throw new APIException("No products found");
        }

        ProductResponse response = new ProductResponse();

        response.setContent(productDTOS);
        response.setPageNumber(pageDetails.getPageNumber());
        response.setPageSize(pageDetails.getPageSize());
        response.setTotalPages(productPage.getTotalPages());
        response.setTotalElements(productPage.getContent().size());
        response.setLastPage(productPage.isLast());
        return response;
    }

    public ProductDTO updateProduct(ProductDTO productDTO , Long productId){
        Product product = productRepository.findById(productId).orElseThrow(()-> new ResourceNotFoundException("Product", "id", productId));
        product.setProductName(productDTO.getProductName());
        product.setPrice(productDTO.getPrice());
        product.setQuantity(productDTO.getQuantity());
        product.setDescription(productDTO.getDescription());
        product.setSpecialPrice(productDTO.getSpecialPrice());

        List<Cart> carts = cartRepository.findCartsByProductId(productId);
        List<CartDTO> cartDTOS = carts.stream().map(cart ->{
            CartDTO cartDTO =  mapper.map(cart,CartDTO.class);
            List<ProductDTO> products = cart.getCartItems().stream().map(item -> {
                ProductDTO productMappedDTO = mapper.map(item.getProduct(),ProductDTO.class);
                List<ProductImageDTO> productImagesDTOs = item.getProduct().getProductImages().stream().map(productImage -> mapper.map(productImage, ProductImageDTO.class)).toList();
                productMappedDTO.setImages(productImagesDTOs);
                return productMappedDTO;
            }).toList();
            cartDTO.setProducts(products);
            return  cartDTO;
        }).toList();

        cartDTOS.forEach(cartDTO -> cartService.updateProductsInCart(cartDTO.getCartId(), productId));

        Product savedProduct =  productRepository.save(product);
        return mapper.map(savedProduct, ProductDTO.class);
    }


    public ProductDTO createProduct(ProductDTO productDTO,Long categoryId,List<MultipartFile> images) throws IOException {
        Category category = categoryRepository.findById(categoryId).orElseThrow(()-> new ResourceNotFoundException("Category", "id", categoryId));

        List<Product> products =  category.getProducts();

        for(Product product: products){
            if(product.getProductName().equalsIgnoreCase(productDTO.getProductName())) throw new APIException("Product name already exists");
        }

        Product product = mapper.map(productDTO, Product.class);
        product.setProductImages(new ArrayList<>());

        product.setCategory(category);
        double specialPrice = (product.getPrice()) - product.getPrice() * (product.getDiscount()*0.01);

        product.setSpecialPrice(specialPrice);

        int displayOrder = 1;

        for(MultipartFile image : images){
            Map<?, ?> result = cloudinaryService.uploadFile(image);
            ProductImage productImage = new ProductImage();
            productImage.setPublicId(result.get("public_id").toString());
            productImage.setUrl(result.get("secure_url").toString());
            productImage.setDisplayOrder(displayOrder++);
            productImage.setProduct(product);
            product.getProductImages().add(productImage);
        }

        Product newProduct = productRepository.save(product);
        ProductDTO newProductDTO = mapper.map(newProduct, ProductDTO.class);
        newProductDTO.setImages(product.getProductImages().stream().map(image -> mapper.map(image, ProductImageDTO.class)).toList());
        return  newProductDTO;
    }

    @Transactional
    public ProductDTO deleteProduct(Long productId) throws IOException {
        Product product = productRepository.findById(productId).orElseThrow(()-> new ResourceNotFoundException("Product", "id", productId));
        List<Cart> carts = cartRepository.findCartsByProductId(productId);

        carts.forEach(cart -> cartService.deleteProductFromCart(cart.getId(), productId));


        for(ProductImage productImage: product.getProductImages()){
            cloudinaryService.deleteFile(productImage.getPublicId());
        }

        productRepository.delete(product);
        return mapper.map(product, ProductDTO.class);
    }

    public ProductResponse getProductsByCategory(Long categoryId,Integer pageNumber, Integer pageSize, String sortBy, String sortOrder){
        Category category = categoryRepository.findById(categoryId).orElseThrow(()-> new ResourceNotFoundException("Category", "id", categoryId));

        Sort sort = sortOrder.equalsIgnoreCase("asc")? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sort);
        Page<Product> productPage = productRepository.findByCategoryOrderByPriceAsc(category,pageDetails);


        List<Product> products = productPage.getContent();
        List<ProductDTO> productDTOS = products.stream().map(product -> mapper.map(product, ProductDTO.class)).toList();


        if(products.isEmpty()){
            throw new APIException(category.getCategoryName() + " does not have any products");
        }

        ProductResponse response = new ProductResponse();

        response.setContent(productDTOS);

        response.setPageNumber(pageDetails.getPageNumber());
        response.setPageSize(pageDetails.getPageSize());
        response.setTotalPages(productPage.getTotalPages());
        response.setTotalElements(productPage.getContent().size());
        response.setLastPage(productPage.isLast());
        return  response;
    }


    @Override
    public ProductResponse getProductsByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder){

        Sort sort = sortOrder.equalsIgnoreCase("asc")? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sort);
        Page<Product> productPage = productRepository.findByProductNameContainingIgnoreCase(keyword,pageDetails);


        List<Product> products = productPage.getContent();
        List<ProductDTO> productDTOS = products.stream().map(product ->{
            ProductDTO productDTO =  mapper.map(product, ProductDTO.class);
            List<ProductImageDTO> productDTOImages = product.getProductImages().stream().map(productImage -> mapper.map(productImage, ProductImageDTO.class)).toList();
            productDTO.setImages(productDTOImages);
            return productDTO;
        }).toList();


        if(products.isEmpty()){
            throw new APIException("No products found");
        }

        ProductResponse response = new ProductResponse();

        response.setContent(productDTOS);

        response.setPageNumber(pageDetails.getPageNumber());
        response.setPageSize(pageDetails.getPageSize());
        response.setTotalPages(productPage.getTotalPages());
        response.setTotalElements(productPage.getContent().size());
        response.setLastPage(productPage.isLast());
        return  response;
    }


//    @Override
//    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
//        //get product from db
//        Product product = productRepository.findById(productId).orElseThrow(()-> new ResourceNotFoundException("Product", "id", productId));
//        String fileName = fileService.uploadImage(path,image);
//        product.setImage(fileName);
//        productRepository.save(product);
//        return mapper.map(product, ProductDTO.class);
//    }
}
