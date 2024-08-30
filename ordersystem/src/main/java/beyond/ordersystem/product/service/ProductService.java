package beyond.ordersystem.product.service;

import beyond.ordersystem.common.service.StockInventoryService;
import beyond.ordersystem.ordering.dto.StockDecreaseEvent;
import beyond.ordersystem.product.domain.Product;
import beyond.ordersystem.product.dto.ProductCreateReqDto;
import beyond.ordersystem.product.dto.ProductResDto;
import beyond.ordersystem.product.dto.ProductSearchDto;
import beyond.ordersystem.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final S3Client s3Client;
    private final StockInventoryService stockInventoryService;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;


    @Autowired
    public ProductService(ProductRepository productRepository, S3Client s3Client, StockInventoryService stockInventoryService) {
        this.productRepository = productRepository;
        this.s3Client = s3Client;
        this.stockInventoryService = stockInventoryService;
    }

    public Product productCreate(ProductCreateReqDto dto){
        MultipartFile image = dto.getImagePath();
        Product product = null;
        try {
            product = productRepository.save(dto.toEntity());
            byte[] bytes = image.getBytes();
            Path path = Paths.get("C:/Users/anflw/Desktop/BeyondCamp/temp/",
                    product.getId() + "_" + image.getOriginalFilename());
            Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

            product.updateImagePath(path.toString());
            if(dto.getName().contains("sale")){
                stockInventoryService.increaseStock(product.getId(), dto.getStockQuantity());
            }
        }catch (IOException e){
//            트렌젝션 처리를 하기 위해 예외를 던져준다.
            throw new RuntimeException("image save failed");
        }

        return product;
    }

    public Product productAwsCreate(ProductCreateReqDto dto){
        MultipartFile image = dto.getImagePath();
        Product product = null;
        try {
            product = productRepository.save(dto.toEntity());
            byte[] bytes = image.getBytes();
            String fileName = product.getId() + "_" + image.getOriginalFilename();
            Path path = Paths.get("C:/Users/anflw/Desktop/BeyondCamp/temp/",fileName);
//            local pc에 임시 저장
            Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

//            aws 에 pc에 저장된 파일을 업로드.
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .build();

            PutObjectResponse putObjectResponse =
                    s3Client.putObject(putObjectRequest , RequestBody.fromFile(path));
            String s3Path = s3Client.utilities().getUrl(a -> a.bucket(bucket).key(fileName)).toExternalForm();
            product.updateImagePath(s3Path);
        }catch (IOException e){
//            트렌젝션 처리를 하기 위해 예외를 던져준다.
            throw new RuntimeException("image save failed");
        }
        return product;
    }

    public Page<ProductResDto> productList(ProductSearchDto searchDto, Pageable pageable){
//        검색을 위해 Specification 객체 사용.
//        Specification 객체는 복잡한 쿼리를 명세를 이용하여 정의하는 방식으로, 쿼리를 쉽게 생성.
        Specification<Product> specification = new Specification<Product>() {
            @Override
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if(searchDto.getSearchName() != null){
//                    root: Entity의 속성을 접근하기 위한 객체, CriteriaBuilder는 쿼리를 생성하기 위한 객체.
                    predicates.add(criteriaBuilder.like(root.get("name"), "%"+searchDto.getSearchName()+"%"));
                }
                if(searchDto.getCategory() != null){
                    predicates.add(criteriaBuilder.like(root.get("category"), "%"+searchDto.getCategory()+"%"));
                }
                Predicate[] predicatesArr = new Predicate[predicates.size()];
                for(int i = 0 ; i < predicatesArr.length; i++){
                    predicatesArr[i] = predicates.get(i);
                }
//                위 2개의 쿼리 조건문을 and 조건으로 연결
                Predicate predicate = criteriaBuilder.and(predicatesArr);
                return predicate;
            }
        };
        Page<Product> products = productRepository.findAll(specification, pageable);
        return products.map(Product::toEntity);
    }
}
