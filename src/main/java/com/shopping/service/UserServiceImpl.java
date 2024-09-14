package com.shopping.service;

import com.shopping.client.OrderClient;
import com.shopping.db.User;
import com.shopping.db.UserDao;
import com.shopping.stubs.order.Order;
import com.shopping.stubs.user.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {
  private final Logger logger = Logger.getLogger(UserServiceImpl.class.getName());
  private final UserDao userDao = new UserDao();

  @Override
  public void getUserDetails(UserRequest request, StreamObserver<UserResponse> responseObserver) {
    User user = userDao.getDetails(request.getUsername());
    UserResponse.Builder userResponseBuilder =
        UserResponse.newBuilder()
            .setId(user.getId())
            .setAge(user.getAge())
            .setName(user.getName())
            .setGender(Gender.valueOf(user.getGender()))
            .setName(user.getName());

    List<Order> orders = extractNoOfOrdersByCallingOrderService(userResponseBuilder);
    // setting the noOfOrders after making grpc call to Order Service
    userResponseBuilder.setNoOfOrders(orders.size());

    UserResponse userResponse = userResponseBuilder.build();
    responseObserver.onNext(userResponse);
    responseObserver.onCompleted();
  }

  private List<Order> extractNoOfOrdersByCallingOrderService(
      UserResponse.Builder userResponseBuilder) {
    // get orders by invoking the OrderClient
    logger.info("creating the channel and invoking the order client");
    ManagedChannel channel =
        ManagedChannelBuilder.forTarget("localhost:50052").usePlaintext().build();
    OrderClient orderClient = new OrderClient(channel);
    List<Order> orders = orderClient.getOrdersForUserId(userResponseBuilder.getId());

    try {
      channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      logger.log(Level.SEVERE, "Channel didn't shutdown", e);
    }
    return orders;
  }
}
