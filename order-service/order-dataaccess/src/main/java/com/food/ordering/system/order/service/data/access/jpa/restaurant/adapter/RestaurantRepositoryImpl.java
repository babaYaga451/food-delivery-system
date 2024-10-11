package com.food.ordering.system.order.service.data.access.jpa.restaurant.adapter;

import com.food.ordering.system.dataaccess.restaurant.entity.RestaurantEntity;
import com.food.ordering.system.dataaccess.restaurant.repository.RestaurantJpaRepository;
import com.food.ordering.system.order.service.data.access.jpa.restaurant.mapper.RestaurantDataAccessMapper;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.ports.output.repository.RestaurantRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class RestaurantRepositoryImpl implements RestaurantRepository {

  private final RestaurantJpaRepository restaurantJpaRepository;
  private final RestaurantDataAccessMapper restaurantDataAccessMapper;

  public RestaurantRepositoryImpl(RestaurantJpaRepository restaurantJpaRepository,
      RestaurantDataAccessMapper restaurantDataAccessMapper) {
    this.restaurantJpaRepository = restaurantJpaRepository;
    this.restaurantDataAccessMapper = restaurantDataAccessMapper;
  }

  @Override
  public Optional<Restaurant> findRestaurantInformation(Restaurant restaurant) {
    List<UUID> productIds = restaurantDataAccessMapper.restaurantToRestaurantProducts(restaurant);
    Optional<List<RestaurantEntity>> restaurantEntities =
        restaurantJpaRepository.findByRestaurantIdAndAndProductIdIn(
            restaurant.getId().getValue(), productIds);
    return restaurantEntities.map(restaurantDataAccessMapper::restaurantEntityToRestaurant);
  }
}
