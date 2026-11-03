package edu.secure.lab.mapper;

import edu.secure.lab.domain.Customer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CustomerMapper {

    @Select("SELECT customer_id, customer_name, rrn, phone, password_hash FROM customer WHERE customer_id = #{customerId}")
    Customer findById(@Param("customerId") String customerId);
}
