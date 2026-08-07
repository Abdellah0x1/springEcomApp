package com.ecommerce.project.controller;

import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.services.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {
    private ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/public/products")
    public ResponseEntity<ProductResponse> getAllProducts(@RequestParam (name="page" , required = false, defaultValue = AppConstants.PAGE_NUMER) Integer pageNumber,
                                                            @RequestParam (name="size" , required = false, defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
                                                            @RequestParam (name="sortBy" , required = false, defaultValue = AppConstants.SORT_PRODUCTS_BY) String sortBy,
                                                            @RequestParam (name="sortOrder" , required = false, defaultValue = AppConstants.SORT_ORDER) String sortOrder){
        ProductResponse response = productService.getAllProducts(pageNumber,pageSize,sortBy,sortOrder);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/admin/products/{productId}")
    public ResponseEntity<ProductDTO> updateProduct(@Valid @RequestBody ProductDTO productDTO, @PathVariable Long productId){
        return new ResponseEntity<>(productService.updateProduct(productDTO,productId), HttpStatus.OK);
    }


    @PostMapping(value="/admin/categories/{categoryId}/product",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ProductDTO> addProduct  (
            @RequestPart("product")
            @Valid String productJson,
            @PathVariable Long categoryId,
            @RequestPart("images") List<MultipartFile> images) throws IOException{

        ProductDTO product =
                objectMapper.readValue(productJson, ProductDTO.class);

        ProductDTO newProductDTO = productService.createProduct(product,categoryId,images);
        return new ResponseEntity<>(newProductDTO, HttpStatus.CREATED);
    }

    @GetMapping("/public/products/{productId}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable Long productId){
        ProductDTO product = productService.getProductById(productId);
        return ResponseEntity.ok().body(product);

    }

    @GetMapping("/public/categories/{categoryId}/products")
    public ResponseEntity<ProductResponse> getProductsByCategory(@PathVariable Long categoryId,
                                                                 @RequestParam (name="page" , required = false, defaultValue = AppConstants.PAGE_NUMER) Integer pageNumber,
                                                                 @RequestParam (name="size" , required = false, defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
                                                                 @RequestParam (name="sortBy" , required = false, defaultValue = AppConstants.SORT_PRODUCTS_BY) String sortBy,
                                                                 @RequestParam (name="sortOrder" , required = false, defaultValue = AppConstants.SORT_ORDER) String sortOrder){
        ProductResponse productReponse  = productService.getProductsByCategory(categoryId,pageNumber,pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(productReponse,HttpStatus.OK);
    }

    @GetMapping("/public/products/keyword/{keyword}")
    public ResponseEntity<ProductResponse> getProductsByKeyWord(@PathVariable String keyword,
                                                                @RequestParam (name="page" , required = false, defaultValue = AppConstants.PAGE_NUMER) Integer pageNumber,
                                                                @RequestParam (name="size" , required = false, defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
                                                                @RequestParam (name="sortBy" , required = false, defaultValue = AppConstants.SORT_PRODUCTS_BY) String sortBy,
                                                                @RequestParam (name="sortOrder" , required = false, defaultValue = AppConstants.SORT_ORDER) String sortOrder){
        ProductResponse response = productService.getProductsByKeyword(keyword,pageNumber,pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/admin/products/{productId}")
    public ProductDTO deleteProduct(@PathVariable Long productId) throws IOException {
        ProductDTO productDTO = productService.deleteProduct(productId);
        return productDTO;
    }
    
}