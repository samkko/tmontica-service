package com.internship.tmontica.order;

import com.internship.tmontica.cart.CartMenu;
import com.internship.tmontica.option.Option;
import com.internship.tmontica.order.model.request.OrderReq;
import com.internship.tmontica.order.model.request.Order_MenusReq;
import com.internship.tmontica.order.model.response.OrderListResp;
import com.internship.tmontica.order.model.response.OrderResp;
import com.internship.tmontica.order.model.response.Order_MenusResp;
import com.internship.tmontica.cart.CartMenuService;
import com.internship.tmontica.menu.MenuService;
import com.internship.tmontica.option.OptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private OptionService optionService;
    @Autowired
    private CartMenuService cartMenuService;
    @Autowired
    private MenuService menuService;

    /** 주문 받기(결제하기) */
    @PostMapping
    public ResponseEntity<Map<String, Integer>> addOrder(@RequestBody @Valid OrderReq orderReq){
        // 사용자 아이디 받아오기
        String userId = "testid"; //임시

        Map<String, Integer> map = new HashMap<>(); // 리턴할 객체

        Order order = new Order(0,orderReq.getPayment(),orderReq.getTotalPrice(),orderReq.getUsedPoint(),
                orderReq.getTotalPrice()-orderReq.getUsedPoint(), "미결제", orderReq.getUserId());

        // 주문테이블에 추가
        orderService.addOrder(order);
        int orderId = order.getId();

        // 주문상세테이블에 추가
        // 카트 아이디로 정보를 가져와서 order_details 에 추가
        List<Order_MenusReq> menus = orderReq.getMenus();
        for (Order_MenusReq menu: menus) {
            CartMenu cartMenu = cartMenuService.getCartMenuByCartId(menu.getCartId());
            OrderDetail orderDetail = new OrderDetail(0, orderId, cartMenu.getOption(), cartMenu.getPrice(),
                                                                cartMenu.getQuantity(),cartMenu.getMenuId());

            int stock = menuService.getMenuById(orderDetail.getMenuId()).getStock(); // 메뉴의 현재 재고량
            int quantity = orderDetail.getQuantity(); // 차감할 메뉴의 수량
            if(stock-quantity < 0){ // 재고가 모자랄 경우
                // 재고 없을 경우 400 Bad Request return
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }else {                 // 재고가 남아있는 경우
                // 재고 수량 차감
                menuService.updateMenuStock(orderDetail.getMenuId(), stock-quantity);
                // 주문 상세 테이블에 추가
                orderDetail.setOrderId(orderId); // 주문번호 set
                orderService.addOrderDetail(orderDetail);
                // 장바구니에서는 삭제
                cartMenuService.deleteCartMenu(menu.getCartId());
                // 주문상태로그테이블에 "미결제" 상태로 추가
                orderService.addOrderStatusLog(new OrderStatusLog("미결제", userId, orderId));
            }
        }

        map.put("orderId", orderId); // 반환값 orderId
        return new ResponseEntity<>(map, HttpStatus.OK);
    }



    /** 주문 취소 */
    @DeleteMapping("/{orderId}")
    public void deleteOrder(@PathVariable("orderId") int orderId){
        // orders 테이블에서 status 수정
        orderService.deleteOrder(orderId);
        // order_status_log 테이블에도 주문취소 로그 추가
        // TODO: 토큰에서? 사용자 아이디 가져오기 해야함
        OrderStatusLog orderStatusLog = new OrderStatusLog("주문취소","사용자id", orderId);
        orderService.addOrderStatusLog(orderStatusLog);
    }

    /** 주문정보 한개 가져오기(상세내역 포함) */
    @GetMapping("/{orderId}")
    public OrderResp getOrderByOrderId(@PathVariable("orderId")int orderId){
        // TODO: menus에 들어갈 객체 필요, menu select 기능 필요
        // TODO: 주문번호로 주문정보와 주문 상세정보를 객체에 담아 리턴시키
        Order order = orderService.getOrderByOrderId(orderId);
        List<Order_MenusResp> menus = orderService.getOrderDetailByOrderId(orderId);

        //메뉴 옵션 "1__1/4__2" => "HOT/샷추가(2개)" 로 바꾸는 작업
        for (int i = 0; i < menus.size(); i++) {
            String option = menus.get(i).getOption();
            String convert = ""; // 변환할 문자열
            String[] arrOption = option.split("/");
            for (int j = 0; j < arrOption.length; j++){
               String[] oneOption = arrOption[j].split("__");
               Option tmpOption = optionService.getOptionById(Integer.valueOf(oneOption[0]));
               if(j != 0 ){ convert += "/"; }
               if(tmpOption.getType().equals("Temperature")){
                   convert += tmpOption.getName();
               }else{
                   convert += tmpOption.getName()+"("+oneOption[1]+"개)";
               }
            }
            menus.get(i).setOption(convert);
        }

        OrderResp orderResp = new OrderResp(orderId, order.getPayment(), order.getStatus(), order.getTotalPrice(),
                                            order.getRealPrice(), order.getUsedPoint(), order.getOrderDate(), menus);

        return orderResp;
    }


    /** 사용자 한명의 주문 전체 내역 가져오기 */
    @GetMapping
    public Map<String, List> getOrderByUserId(@RequestParam String userId){
        List<OrderListResp> orderListResps = new ArrayList<>(); // 최종 반환할 리스트

        List<Order> orders = orderService.getOrderByUserId(userId);
        for (Order order: orders) {
            OrderListResp orderListResp = new OrderListResp();

            orderListResp.setOrderID(order.getId());
            orderListResp.setOrderDate(order.getOrderDate());
            orderListResp.setStatus(order.getStatus());
            orderListResp.setMenuNames(orderService.getMenuNamesByOrderId(order.getId()));

            orderListResps.add(orderListResp);
        }

        Map<String, List> map = new HashMap<>();

        map.put("orders", orderListResps);

        return map;
    }
}