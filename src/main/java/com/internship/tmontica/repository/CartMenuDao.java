package com.internship.tmontica.repository;

import com.internship.tmontica.dto.CartMenu;
import com.internship.tmontica.dto.OrderDetail;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CartMenuDao {
    // 카트 아이디로 cart_menus 테이블에서 조회하기
    @Select("select * from cart_menus where id = #{cartId}")
    CartMenu getCartMenuByCartId(int cartId);

    // 카트 와 메뉴 테이블 로부터 orderDetails 테이블에 추가할 정보 가져오기
    @Select("select A.quantity, A.option, A.user_id, A.menu_id, B.sell_price+A.option_price as price " +
            "from cart_menus A inner join menus B " +
            "   on A.menu_id = B.id " +
            "where A.id = #{cartId}")
    OrderDetail getCartMenuForOrderDetail(int cartId);

    // 카트에서 삭제하기
    @Delete("delete from cart_menus where id = #{cartId}")
    void deleteCartMenu(int cartId);
}
