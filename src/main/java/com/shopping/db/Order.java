package com.shopping.db;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Order {
  private int userId;
  private int orderId;
  private int noOfItems;
  private double totalAmount;
  private Date orderDate;
}
