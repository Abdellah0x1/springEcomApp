package com.ecommerce.project.services;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.ProductRepository;
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
import java.util.List;

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

    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("asc")? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sort);
        Page<Product> productPage = productRepository.findAll(pageDetails);


        List<Product> products = productPage.getContent();
        List<ProductDTO> productDTOS = products.stream().map(product -> mapper.map(product, ProductDTO.class)).toList();

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

        Product savedProduct =  productRepository.save(product);
        return mapper.map(savedProduct, ProductDTO.class);
    }


    public ProductDTO createProduct(ProductDTO productDTO,Long categoryId){
        Category category = categoryRepository.findById(categoryId).orElseThrow(()-> new ResourceNotFoundException("Category", "id", categoryId));

        List<Product> products =  category.getProducts();

        for(Product product: products){
            if(product.getProductName().equalsIgnoreCase(productDTO.getProductName())) throw new APIException("Product name already exists");
        }

        Product product = mapper.map(productDTO, Product.class);
        product.setCategory(category);
        double specialPrice = (product.getPrice()) - product.getPrice() * (product.getDiscount()*0.01);
        product.setImage("default.png");
        product.setSpecialPrice(specialPrice);
        Product newProduct = productRepository.save(product);
        return  mapper.map(newProduct, ProductDTO.class);
    }

    public ProductDTO deleteProduct(Long productId){
        Product product = productRepository.findById(productId).orElseThrow(()-> new ResourceNotFoundException("Product", "id", productId));
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
        List<ProductDTO> productDTOS = products.stream().map(product -> mapper.map(product, ProductDTO.class)).toList();


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


    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        //get product from db
        Product product = productRepository.findById(productId).orElseThrow(()-> new ResourceNotFoundException("Product", "id", productId));
        String fileName = fileService.uploadImage(path,image);
        product.setImage(fileName);
        productRepository.save(product);
        return mapper.map(product, ProductDTO.class);
    }
}
