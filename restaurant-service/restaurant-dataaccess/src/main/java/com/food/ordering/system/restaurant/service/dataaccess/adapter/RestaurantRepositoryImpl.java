package com.food.ordering.system.restaurant.service.dataaccess.adapter;

import com.food.ordering.system.dataaccess.restaurant.repository.RestaurantJpaRepository;
import com.food.ordering.system.restaurant.service.dataaccess.mapper.RestaurantDataAccessMapper;
import com.food.ordering.system.restaurant.service.domain.entity.Restaurant;
import com.food.ordering.system.restaurant.service.domain.ports.output.repository.RestaurantRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class RestaurantRepositoryImpl implements RestaurantRepository {

  private final RestaurantDataAccessMapper restaurantDataAccessMapper;
  private final RestaurantJpaRepository restaurantJpaRepository;

  public RestaurantRepositoryImpl(RestaurantDataAccessMapper restaurantDataAccessMapper,
      RestaurantJpaRepository restaurantJpaRepository) {
    this.restaurantDataAccessMapper = restaurantDataAccessMapper;
    this.restaurantJpaRepository = restaurantJpaRepository;
  }

  @Override
  public Optional<Restaurant> findRestaurantInformation(Restaurant restaurant) {
    List<UUID> productIds = restaurantDataAccessMapper.restaurantToRestaurantProducts(restaurant);
    return restaurantJpaRepository
        .findByRestaurantIdAndAndProductIdIn(restaurant.getId().getValue(), productIds)
        .map(restaurantDataAccessMapper::restaurantEntityToRestaurant);
  }
}
