package com.ecommerce.service;


import com.ecommerce.exceptions.ApiException;
import com.ecommerce.exceptions.ResourceNotFoundException;
import com.ecommerce.model.Cart;
import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.payload.CartDTO;
import com.ecommerce.payload.CartItemDTO;
import com.ecommerce.payload.ProductDTO;
import com.ecommerce.payload.ProductResponse;
import com.ecommerce.repositories.CartRepository;
import com.ecommerce.repositories.CategoryRepository;
import com.ecommerce.repositories.ProductRepository;
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
import java.util.stream.Collectors;

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

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartService cartService;

    @Value("${project.image}")
    private String path;

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize,String sortBy,String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber,pageSize,sortByAndOrder);

        Page<Product> pageProducts = productRepository.findAll(pageDetails);
        List<Product> products = pageProducts.getContent();

       if(products.isEmpty()){
           throw new ApiException("No Product Created Till Now !!!");
       }
       List<ProductDTO> productDTOs = products.stream().map(p -> modelMapper.map(p, ProductDTO.class)).toList();

       ProductResponse productResponse = new ProductResponse();
       productResponse.setContent(productDTOs);
       productResponse.setPageNumber(pageProducts.getNumber());
       productResponse.setPageSize(pageProducts.getSize());
       productResponse.setTotalElements(pageProducts.getTotalElements());
       productResponse.setTotalPages(pageProducts.getTotalPages());
       productResponse.setLastPage(pageProducts.isLast());

       return productResponse;
    }

    @Override
    public ProductResponse getProductsByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Category category = categoryRepository.findById(categoryId).
                orElseThrow(() -> new ResourceNotFoundException("Category","category",categoryId));

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber,pageSize,sortByAndOrder);

        Page<Product> pageProducts = productRepository.findByCategory_CategoryId(categoryId, pageDetails);
        List<Product> products = pageProducts.getContent();

        if(products.isEmpty()){
            throw new ApiException("No Products Created Under CategoryId " + categoryId +" Till Now !!!");
        }
        ProductResponse productResponse = new ProductResponse();
        List<ProductDTO> productDTOS = products.stream().map(p -> modelMapper.map(p,ProductDTO.class)).toList();
        productResponse.setContent(productDTOS);
        return productResponse;
    }

    @Override
    public ProductResponse getProductsByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber,pageSize,sortByAndOrder);

        Page<Product> pageProducts = productRepository.findByProductNameContainingIgnoreCase(keyword,pageDetails);
        List<Product> products = pageProducts.getContent();

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

        Product existProduct = productRepository.findByProductName(product.getProductName());

        if(existProduct != null){
            throw new ApiException("Product Name Already Exist !!!");
        }

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

        List<Cart> carts = cartRepository.findCartsByProductId(productId);

        List<CartDTO> cartDTOS = carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

            List<ProductDTO> productDTOS = cart.getCartItems().stream()
                    .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class))
                    .collect(Collectors.toList());

            cartDTO.setProduct(productDTOS);

            return cartDTO;
        }).collect(Collectors.toList());

        cartDTOS.forEach(cart -> cartService.updateProductInCarts(cart.getCartId(),productId));

        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product product = productRepository.findById(productId).
                orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        List<Cart> carts = cartRepository.findCartsByProductId(productId);
        carts.forEach(cart -> cartService.deleteProductFromCart(cart.getCartId(), Math.toIntExact(productId)));

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
