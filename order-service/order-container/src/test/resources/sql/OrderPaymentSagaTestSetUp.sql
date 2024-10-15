insert into "order".orders(id, customer_id, restaurant_id, tracking_id, price, order_status, failure_messages)
values('d215b5f8-0249-4dc5-89a3-51fd148cfb17', 'd215b5f8-0249-4dc5-89a3-51fd148cfb41', 'd215b5f8-0249-4dc5-89a3-51fd148cfb45',
       'd215b5f8-0249-4dc5-89a3-51fd148cfb18', 100.00, 'PENDING', '');

insert into "order".order_items(id, order_id, product_id, price, quantity, sub_total)
values(1, 'd215b5f8-0249-4dc5-89a3-51fd148cfb17', 'd215b5f8-0249-4dc5-89a3-51fd148cfb47', 100.00, 1, 100.00);

insert into "order".order_address(id, order_id, street, postal_code, city)
values('d215b5f8-0249-4dc5-89a3-51fd148cfb15', 'd215b5f8-0249-4dc5-89a3-51fd148cfb17', 'test street', '1000AA', 'test city');