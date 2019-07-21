package com.internship.tmontica.controller;

import com.internship.tmontica.dto.*;
import com.internship.tmontica.dto.request.MenusReq;
import com.internship.tmontica.dto.request.OrderReq;
import com.internship.tmontica.dto.response.MenusResp;
import com.internship.tmontica.dto.response.OrderListResp;
import com.internship.tmontica.dto.response.OrderResp;
import com.internship.tmontica.service.CartMenuService;
import com.internship.tmontica.service.OptionService;
import com.internship.tmontica.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.sql.Date;
import java.text.SimpleDateFormat;
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

    /** 주문 받기(결제하기) - 카트에서 주문하기 */
    @PostMapping
    public Map<String, String> addOrderFromCart(@RequestBody @Valid OrderReq orderReq){
        System.out.println("결제 컨트롤러 ");
        System.out.println(orderReq);

        // 사용자 아이디 받아오기
        String userId = "testid"; //임시

        // 주문번호 생성 : 날짜 + 시분초 + 아이디
        SimpleDateFormat format  = new SimpleDateFormat("yyMMddHHmmss");
        String orderId = format.format(System.currentTimeMillis()) + userId;
        System.out.println(orderId);
        // TODO: 주문테이블에 추가
        Order order = new Order(orderId,orderReq.getPayment(),orderReq.getTotalPrice(),orderReq.getUsedPoint(),
                orderReq.getTotalPrice()-orderReq.getUsedPoint(), "미결제", orderReq.getUserId());
        orderService.addOrder(order);
        // TODO: 주문상세테이블에 추가
        // menus에서 카트 아이디로 정보를 가져와서 order_details 에 추가
        List<MenusReq> menus = orderReq.getMenus();
        for (MenusReq menu: menus) {
            OrderDetail orderDetail = cartMenuService.getCartMenuForOrderDetail(menu.getCartId());
            orderDetail.setOrderId(orderId);
//            System.out.println(orderDetail);
            // 주문 상세 페이지에 추가
            orderService.addOrderDetail(orderDetail);

            // TODO: 장바구니에서 주문할때는 장바구니에서는 삭제처리 -> 바로주문한 애는 장바구니에 넣었다 빼기
            cartMenuService.deleteCartMenu(menu.getCartId());
        }

        // TODO: 주문상태로그테이블에 "미결제" 상태로 추가
        orderService.addOrderStatusLog(new OrderStatusLog("미결제", userId, orderId));

        Map<String, String> map = new HashMap<>();
        map.put("orderId", orderId);
        return map;
    }

//    /** 주문 받기(결제하기) - 바로주문하기 */
//    @PostMapping("/direct")
//    public Map<String, String> addOrderDirect(@RequestBody @Valid ){
//        // TODO: 카트에 추가하고
//        // TODO: 카트에서 추가하기 메서드를 부르기
//
//    }

    /** 주문 취소 */
    @DeleteMapping("/{orderId}")
    public void deleteOrder(@PathVariable("orderId") String orderId){
        // 컬럼을 지우는게 아니라 status를 주문취소로 수정하는것임
        // orders 테이블에서 status 수정
        orderService.deleteOrder(orderId);
        // order_status_log 테이블에도 주문취소 로그 추가
        // TODO: 토큰에서? 사용자 아이디 가져오기 해야함
        OrderStatusLog orderStatusLog = new OrderStatusLog("주문취소","사용자id", orderId);
        orderService.addOrderStatusLog(orderStatusLog);
    }

    /** 주문정보 한개 가져오기(상세내역 포함) */
    @GetMapping("/{orderId}")
    public OrderResp getOrderByOrderId(@PathVariable("orderId")String orderId){
        // TODO: menus에 들어갈 객체 필요, menu select 기능 필요
        // TODO: 주문번호로 주문정보와 주문 상세정보를 객체에 담아 리턴시키
        Order order = orderService.getOrderByOrderId(orderId);
        List<MenusResp> menus = orderService.getOrderDetailByOrderId(orderId);

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

        OrderResp orderResp = new OrderResp(order.getPayment(), order.getStatus(), order.getTotalPrice(),
                                            order.getRealPrice(), order.getUsedPoint(), order.getOrderDate(), menus);


        // 더미 데이터
//        List<MenusResp> menus = new ArrayList<>();
//        menus.add(new MenusResp(1, "americano", "HOT / 샷추가(1개) / SIZE UP", 3, 3800));
//        menus.add(new MenusResp(2, "caffelatte", "ICE / 샷추가(1개) / SIZE UP", 1, 2300));
//        OrderResp orderResp = new OrderResp("현장결제","미결제",6100,4000,2100,
//                Date.valueOf("2019-07-19") ,menus);

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
        //System.out.println(orderListResps);

        Map<String, List> map = new HashMap<>();

        map.put("orders", orderListResps);

        return map;
    }
}
