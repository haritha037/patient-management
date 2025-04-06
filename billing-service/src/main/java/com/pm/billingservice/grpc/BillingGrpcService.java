package com.pm.billingservice.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc.BillingServiceImplBase;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class BillingGrpcService extends BillingServiceImplBase{
    private static final Logger log = LoggerFactory.getLogger(BillingGrpcService.class);

    @Override
    public void createBillingAccount(
            BillingRequest request,
            StreamObserver<BillingResponse> responseObserver
    ) {

        log.info("createBillingAccount request received {}", request.toString());

        // Business logic - ex: save to database, perform calculations etc

        // create a BillingResponse object
        BillingResponse response = BillingResponse.newBuilder()
                .setAccountId("12345")
                .setStatus("ACTIVE")
                .build();

        // send the response back to the client
        responseObserver.onNext(response);
        // response is completed-> end the req:res cycle
        responseObserver.onCompleted();

        // why 2 lines? because we can send many requests
    }
}
