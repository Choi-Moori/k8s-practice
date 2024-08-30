package beyond.ordersystem.ordering.service;

import beyond.ordersystem.common.service.StockInventoryService;
import beyond.ordersystem.member.domain.Member;
import beyond.ordersystem.member.repository.MemberRepository;
import beyond.ordersystem.ordering.controller.SseController;
import beyond.ordersystem.ordering.domain.OrderDetail;
import beyond.ordersystem.ordering.domain.OrderStatus;
import beyond.ordersystem.ordering.domain.Ordering;
import beyond.ordersystem.ordering.dto.OrderSaveReqDto;
import beyond.ordersystem.ordering.dto.OrderListResDto;
import beyond.ordersystem.ordering.dto.StockDecreaseEvent;
import beyond.ordersystem.ordering.repository.OrderDetailRepository;
import beyond.ordersystem.ordering.repository.OrderingRepository;
import beyond.ordersystem.product.domain.Product;
import beyond.ordersystem.product.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@Slf4j
public class OrderingService {

    private final OrderingRepository orderingRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final StockInventoryService stockInventoryService;
//    private final StockDecreaseEventHandler stockDecreaseEventHandler;
    private final SseController sseController;

    @Autowired
    public OrderingService(OrderingRepository orderingRepository, MemberRepository memberRepository, ProductRepository productRepository, OrderDetailRepository orderDetailRepository, StockInventoryService stockInventoryService, SseController sseController) {
        this.orderingRepository = orderingRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.stockInventoryService = stockInventoryService;
        this.sseController = sseController;
    }

//    public Long orderCreate(OrderCreateDto dto){
//        Member member = memberRepository.findById(dto.getMemberId()).orElseThrow(()->new EntityNotFoundException("해당 멤버 ID가 존재하지 않습니다."));
//        Ordering ordering = Ordering.builder()
//                .member(member)
//                .build();
//
//        List<OrderDetail> orderDetailList = new ArrayList<>();
//        for(OrderCreateDto.OrderInfoDto dtos : dto.getOrderInfoDto()){
//            Product product = productRepository.findById(dtos.getProductId()).orElseThrow(()->new EntityNotFoundException("해당 상품 ID가 존재하지 않습니다."));
//            OrderDetail orderDetail = OrderDetail.builder()
//                    .product(product)
//                    .quantity(dtos.getProductCount())
//                    .ordering(ordering)
//                    .build();
//            ordering.getOrderDetails().add(orderDetail);
//        }
//        orderingRepository.save(ordering);
//        return ordering.getId();
//    }

    public Ordering orderCreate(List<OrderSaveReqDto> dtos){
//        방법2. JPA에 최적화된 방식
//        Member member = memberRepository.findById(dto.getMemberId()).orElseThrow(()->new EntityNotFoundException("해당 ID가 존재하지 않습니다."));

//        아래꺼 그냥 외우면 됨. -> 이해 하구^^
//        토큰 사용할때 간결하게 사용할 수 있는
        String memberEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(memberEmail).orElseThrow(
                () -> new EntityNotFoundException("없음")
        );

        // orderStatus는 초기화했고, orderDetail은 없다고 가정 (아래서 add하는 방식 사용하기 위해)
        // 즉, member만 builder에 넣어주면 됨 => 이렇게 ordering 객체 생성
        Ordering ordering = Ordering.builder()
                .member(member)
                //.orderDetails()
                .build();

        for(OrderSaveReqDto orderCreateReqDto : dtos){
            Product product = productRepository.findById(orderCreateReqDto.getProductId()).orElse(null);
            System.out.println(orderCreateReqDto);
            int quantity = orderCreateReqDto.getProductCnt();

            if(quantity < 1){
                throw new IllegalArgumentException("구매 수량은 1개 이상만 가능합니다");
            }
            // 구매 가능한지 재고 비교
            if (product.getName().contains("sale")) { // sale인 상품일 때만 redis를 통해 재고관리
                // 동시성 해결 => redis를 통한 재고관리 및 재고 잔량 확인
                int newQuantity = stockInventoryService.decreaseStock(orderCreateReqDto.getProductId(), orderCreateReqDto.getProductCnt()).intValue();
                // 여기서 분기처리 ㄱㄱ
                if (newQuantity < 0) { // 재고가 더 부족할 때 -1L 반환한거
                    throw new IllegalArgumentException("재고 부족");
                }
                // rdb에 재고 업데이트 (product 테이블에 업데이트) => 이전까진 100개수량에서 마이너스가 안되고 있었음
                // rabbitmq를 통해 비동기적으로 이벤트 처리
//                stockDecreaseEventHandler.publish(new StockDecreaseEvent(product.getId(), orderCreateReqDto.getProductCnt()));

            } else {
                if (product.getStockQuantity() < quantity) {
                    throw new IllegalArgumentException("재고 부족");
                }
                log.info("재고 확인 (전) : " + product.getStockQuantity());
                product.updateStockQuantity(quantity);
                log.info("재고 확인 (후) : " + product.getStockQuantity());

            }
            // 구매 가능하면 진행
            OrderDetail orderDetail =  OrderDetail.builder()
                    .product(product)
                    .quantity(quantity)
                    // 아직 save가 안됐는데 어떻게 이 위의 ordering이 들어가나? => jpa가 알 아 서 해줌⭐
                    .ordering(ordering)
                    .build();
            ordering.getOrderDetails().add(orderDetail);
        }
        Ordering savedOrdering = orderingRepository.save(ordering);

//        보내려 하는 정보와, 보내고자 하는 사람의 이메일을 보낸다.
        sseController.publishMessage(savedOrdering.toEntity(), "admin@test.com");
        return savedOrdering;
    }

    public List<OrderListResDto> orderList(){
        List<Ordering> orderings = orderingRepository.findAll();
        List<OrderListResDto> orderListResDtos = new ArrayList<>();
        for(Ordering ordering : orderings){
            orderListResDtos.add(ordering.toEntity());
        }
        return orderListResDtos;
    }

    public List<OrderListResDto> myOrder(){
        String memberEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new EntityNotFoundException("해당 email의 멤버가 존재하지 않습니다."));

        List<Ordering> orderings = orderingRepository.findAllByMember(member);
        List<OrderListResDto> orderListResDtos = new ArrayList<>();
        for(Ordering ordering : orderings){
            orderListResDtos.add(ordering.toEntity());
        }
        return orderListResDtos;
    }
    /**
     * 주문 취소
     */
    @Transactional
    public Ordering orderCancel(Long id) {
        Ordering ordering = orderingRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("not found")
        );
        for(OrderDetail orderDetail: ordering.getOrderDetails()){
            orderDetail.getProduct().cancelOrdering(orderDetail.getQuantity());
        }

        ordering.cancelOrdering(OrderStatus.CANCELED); // update로 ordered > canceld로 변경
        return ordering;
    }
}


//      OrderCreate
//        Member member = memberRepository.findById(dto.getMemberId()).orElseThrow(()->new EntityNotFoundException("해당 ID가 존재하지 않습니다."));
//        Ordering ordering = dto.toEntity(member);
//
//        for(OrderCreateDto.OrderInfoDto dtos : dto.getOrderInfoDto()){
//            Product product = productRepository.findById(dtos.getProductId()).orElseThrow(() -> new EntityNotFoundException("해당 상품 ID가 존재하지 않습니다."));
//            OrderDetail orderDetail = OrderDetail.builder()
//                    .product(product)
//                    .ordering(ordering)
//                    .quantity(dtos.getProductCount())
//                    .build();
//            ordering.getOrderDetails().add(orderDetail);
//        }
//        return orderingRepository.save(ordering);
