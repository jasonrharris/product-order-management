insert into product (name, id) values ('Nike Revolution', 1)
insert into price (amount, creation_date_time, currency, product_id, id) values (47.95, {ts '2019-09-17 18:47:52.69'}, 'GBP', '1', '2')
insert into price (amount, creation_date_time, currency, product_id, id) values (45.95, {ts '2019-09-18 10:47:52.69'}, 'GBP', '1', '3')
insert into product (name, id) values ('Reebok Club C 85 Vintage', 2)
insert into price (amount, creation_date_time, currency, product_id, id) values (74.95, {ts '2019-09-17 18:47:52.69'}, 'GBP', '2', '4')

DROP SEQUENCE IF EXISTS PROD_SEQUENCE_ID
CREATE SEQUENCE PROD_SEQUENCE_ID START WITH (select max(ID) + 1 from Product)
DROP SEQUENCE IF EXISTS PRICE_SEQUENCE_ID
CREATE SEQUENCE PRICE_SEQUENCE_ID START WITH (select max(ID) + 1 from Price)
DROP SEQUENCE IF EXISTS ORDER_SEQUENCE_ID
CREATE SEQUENCE ORDER_SEQUENCE_ID START WITH (select max(ID) + 1 from Order_)
