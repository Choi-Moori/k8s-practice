package beyond.ordersystem.ordering.controller;

import beyond.ordersystem.ordering.dto.OrderListResDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class SseController implements MessageListener {
//    SseEmmiter는 연결된 사용자정보(ip, 위치정보 등) 를 의미 => 사용자와 서버와 연결되어야 함.
//    ConcurrentHashMap은 Thread-safe한 map(동시성 이슈발생 X)
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

//    여러번 구독을 방지하기 위한 ConcurrentHashSet
    private Set<String> subscribeList = ConcurrentHashMap.newKeySet();

    @Qualifier("4")
    private final RedisTemplate<String, Object> sseRedisTemplate;
    private final RedisMessageListenerContainer redisMessageListenerContainer;

    public SseController(@Qualifier("4") RedisTemplate<String,Object> sseRedisTemplate, RedisMessageListenerContainer redisMessageListenerContainer) {
        this.sseRedisTemplate = sseRedisTemplate;
        this.redisMessageListenerContainer = redisMessageListenerContainer;
    }

//    email 에 해당되는 메시지를 listen 하는 listener 를 추가한 메서드.
    public void subscribeChannel(String email){
//        이미 구독한 email 일 경우에는 더이상 구독하지 않기 위한 분기처리.
        if(!subscribeList.contains(email)) {
            MessageListenerAdapter listenerAdapter = createListenerAdapter(this);
            redisMessageListenerContainer.addMessageListener(listenerAdapter, new PatternTopic(email));
            subscribeList.add(email);
        }
    }

    private MessageListenerAdapter createListenerAdapter(SseController sseController){
        return new MessageListenerAdapter(sseController, "onMessage");
    }

    @GetMapping("/subscribe")
    public SseEmitter subscribe(){
        SseEmitter emitter = new SseEmitter(14400*60*1000L); // 30분정도로 emitter 유효시간 설정
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        emitters.put(email, emitter);
        emitter.onCompletion(()->emitters.remove(email));
        emitter.onTimeout(()->emitters.remove(email));
        try{
            emitter.send(SseEmitter.event().name("connect").data("connected!!!!"));
        }catch (IOException e){
            e.printStackTrace();
        }
        subscribeChannel(email);
        return emitter;
    }
//    실제로 사용자에게 메시지를 준다.
//    주문이 들어오면
    public void publishMessage(OrderListResDto dto, String email) {
        SseEmitter emitter = emitters.get(email);
//        if(emitter != null){
//            try {
////                보내고자 하는 데이터를 사용하지 않다면 그냥 문자열을 보내도 된다.
////                만약 보낸 데이터를 사용하고자 한다면 사용할 데이터를 보내준다.
//                emitter.send(SseEmitter.event().name("ordered").data(dto));
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }else {
//        Redis 에 받는 사람 정보와, 전송할 데이터를 같이 보내준다.
            sseRedisTemplate.convertAndSend(email, dto);
//        }
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
//        message내용 parsing
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            OrderListResDto dto = objectMapper.readValue(message.getBody(), OrderListResDto.class);
            String email = new String(pattern, StandardCharsets.UTF_8);
            SseEmitter emitter = emitters.get(email);
            if(emitter != null)
                emitter.send(SseEmitter.event().name("ordered").data(dto));
            System.out.println("listener");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
