package com.food.ordering.system.order.service.data.access.restaurant.mapper;

import com.food.ordering.system.dataaccess.restaurant.entity.RestaurantEntity;
import com.food.ordering.system.dataaccess.restaurant.exception.RestaurantDataAccessException;
import com.food.ordering.system.domain.valueObject.Money;
import com.food.ordering.system.domain.valueObject.ProductId;
import com.food.ordering.system.domain.valueObject.RestaurantId;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class RestaurantDataAccessMapper {

  public List<UUID> restaurantToRestaurantProducts(Restaurant restaurant) {
    return restaurant.getProducts().stream()
        .map(product -> product.getId().getValue())
        .collect(Collectors.toList());
  }

  public Restaurant restaurantEntityToRestaurant(List<RestaurantEntity> restaurantEntities) {
    RestaurantEntity restaurantEntity = restaurantEntities.stream()
        .findFirst()
        .orElseThrow(() -> new RestaurantDataAccessException("Restaurant could not be found"));

    List<Product> products = restaurantEntities.stream()
        .map(entity -> new Product(
            new ProductId(entity.getProductId()),
            entity.getProductName(),
            new Money(entity.getProductPrice())))
        .toList();

    return Restaurant.Builder.builder()
        .restaurantId(new RestaurantId(restaurantEntity.getRestaurantId()))
        .active(restaurantEntity.getRestaurantActive())
        .products(products)
        .build();

  }

}
