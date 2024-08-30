//package beyond.ordersystem.ordering.service;
//
//import beyond.ordersystem.common.configs.RabbitMqConfig;
//import beyond.ordersystem.common.service.StockInventoryService;
//import beyond.ordersystem.ordering.dto.StockDecreaseEvent;
//import beyond.ordersystem.ordering.repository.OrderingRepository;
//import beyond.ordersystem.product.domain.Product;
//import beyond.ordersystem.product.repository.ProductRepository;
//import beyond.ordersystem.product.service.ProductService;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.amqp.core.Message;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import javax.persistence.EntityNotFoundException;
//
//// Component 에서 트렌젝셔널 처리를 할 수 있따.
//// (Service, Controller 드읃ㅇ)
//@Component
//public class StockDecreaseEventHandler {
//
//    @Autowired
//    private RabbitTemplate rabbitTemplate;
//    @Autowired
//    private ProductRepository productRepository;
//
//    public void publish(StockDecreaseEvent event){
//        rabbitTemplate.convertAndSend(RabbitMqConfig.STOCK_DECREASE_QUEUE, event);
//    }
//    //ORderingService 와 별도의 트렌잭션으로 돌아간다.
////    트랜잭션이 완료된 이후에 그 다음에 메시지 수신하므로, 동시성이슈발생 X
//    @Transactional
//    @RabbitListener(queues = RabbitMqConfig.STOCK_DECREASE_QUEUE)
//    public void listen(Message message) {
//        String messageBody = new String(message.getBody());
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        try {
////        json메시지를 parsing, - object Mapper로 파싱
//            StockDecreaseEvent event = objectMapper.readValue(messageBody, StockDecreaseEvent.class);
////        재고 업데이트
//            Product product = productRepository.findById(event.getProductId())
//                    .orElseThrow(() -> new EntityNotFoundException("not found"));
//            product.updateStockQuantity(event.getProductCount());
//        }catch (JsonProcessingException e){
//            throw new RuntimeException(e);
//        }
//    }
//}
