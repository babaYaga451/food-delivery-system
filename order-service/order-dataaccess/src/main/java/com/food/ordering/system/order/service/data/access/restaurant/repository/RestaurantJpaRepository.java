package com.food.ordering.system.order.service.data.access.restaurant.repository;

import com.food.ordering.system.order.service.data.access.restaurant.entity.RestaurantEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantJpaRepository extends JpaRepository<RestaurantEntity, UUID> {

  Optional<List<RestaurantEntity>> findByRestaurantIdAndAndProductIdIn(
      UUID restaurantId, List<UUID> productIds);

}
