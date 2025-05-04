package com.ecommerce.service;


import com.ecommerce.exceptions.ApiException;
import com.ecommerce.exceptions.ResourceNotFoundException;
import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.payload.ProductDTO;
import com.ecommerce.payload.ProductResponse;
import com.ecommerce.repositories.CategoryRepository;
import com.ecommerce.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;

    @Value("${project.image}")
    private String path;

    @Override
    public ProductResponse getAllProducts() {
       ProductResponse productResponse = new ProductResponse();
       List<Product> products = productRepository.findAll();
       if(products.isEmpty()){
           throw new ApiException("No Product Created Till Now !!!");
       }
       List<ProductDTO> productDTOs = products.stream().map(p -> modelMapper.map(p, ProductDTO.class)).toList();
       productResponse.setContent(productDTOs);

       return productResponse;
    }

    @Override
    public ProductResponse getProductsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId).
                orElseThrow(() -> new ResourceNotFoundException("Category","category",categoryId));

        List<Product> products = productRepository.findByCategory_CategoryIdOrderByPriceAsc(categoryId);
        if(products.isEmpty()){
            throw new ApiException("No Products Created Under CategoryId " + categoryId +" Till Now !!!");
        }
        ProductResponse productResponse = new ProductResponse();
        List<ProductDTO> productDTOS = products.stream().map(p -> modelMapper.map(p,ProductDTO.class)).toList();
        productResponse.setContent(productDTOS);
        return productResponse;
    }

    @Override
    public ProductResponse getProductsByKeyword(String keyword) {
        List<Product> products = productRepository.findByProductNameContainingIgnoreCase(keyword);

        if(products.isEmpty()){
            throw new ApiException("No Products Available Related To This Keyword !!!");
        }
        List<ProductDTO> productDTOS = products.stream().map(p -> modelMapper.map(p,ProductDTO.class)).toList();
        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        return productResponse;
    }

    @Override
    public ProductDTO addProduct(Long categoryId, ProductDTO productDTO) {
        Category category = categoryRepository.findById(categoryId).
                orElseThrow(() -> new ResourceNotFoundException("Category","category",categoryId));

        Product product = modelMapper.map(productDTO, Product.class);

        product.setImage("default.png");
        product.setCategory(category);

        //calculate special price using discount percentage
        double specialPrice = product.getPrice() - ((product.getDiscount() * 0.01) * product.getPrice());
        product.setSpecialPrice(specialPrice);

        Product savedProduct = productRepository.save(product);

        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
        Product productToUpdate = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productDTO.getProductId()));


        productToUpdate.setProductName(productDTO.getProductName());
        productToUpdate.setDescription(productDTO.getDescription());
        productToUpdate.setPrice(productDTO.getPrice());
        productToUpdate.setDiscount(productDTO.getDiscount());
        productToUpdate.setQuantity(productDTO.getQuantity());
        productToUpdate.setImage(productDTO.getImage());

        double specialPrice = productDTO.getPrice() - ((productDTO.getDiscount() * 0.01) * productDTO.getPrice());
        productToUpdate.setSpecialPrice(specialPrice);


        Product savedProduct = productRepository.save(productToUpdate);
        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product product = productRepository.findById(productId).
                orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        productRepository.delete(product);

        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile productImage) throws IOException {
        Product productFromDb = productRepository.findById(productId).
                orElseThrow(()-> new ResourceNotFoundException("Product", "productId", productId));

        String fileName = fileService.uploadImage(path,productImage);

        productFromDb.setImage(fileName);

        productRepository.save(productFromDb);

        return modelMapper.map(productFromDb, ProductDTO.class);
    }



}
