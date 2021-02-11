package com.github.buiminhhai.pcbook.service;

import com.github.buiminhhai.pcbook.pb.CreateLaptopRequest;
import com.github.buiminhhai.pcbook.pb.CreateLaptopResponse;
import com.github.buiminhhai.pcbook.pb.Laptop;
import com.github.buiminhhai.pcbook.pb.LaptopServiceGrpc;
import io.grpc.Context;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
public class LaptopService extends LaptopServiceGrpc.LaptopServiceImplBase {
    private static final Logger logger = Logger.getLogger(LaptopService.class.getName());

    private LaptopStore store;

    public LaptopService(LaptopStore store) {
        this.store = store;
    }

    @Override
    public void createLaptop(CreateLaptopRequest request, StreamObserver<CreateLaptopResponse> responseObserver) {
        Laptop laptop = request.getLaptop();

        String id = laptop.getId();
        logger.info("got a create-laptop request with ID: " + id);

        UUID uuid;
        if (id.isEmpty()) {
            uuid = UUID.randomUUID();
        } else {
            try {
                uuid = UUID.fromString(id);
            } catch (IllegalArgumentException e) {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
                return;
            }
        }

        // heavy processing
        // try {
        //     TimeUnit.SECONDS.sleep(6);
        // } catch (InterruptedException e) {
        //     e.printStackTrace();
        // }

        if (Context.current().isCancelled()) {
            logger.info("request is canceled");
            responseObserver.onError(
                    Status.CANCELLED.withDescription("request is canceled").asRuntimeException()
            );
        }


        Laptop other = laptop.toBuilder().setId(uuid.toString()).build();

        try {
            store.Save(other);
        } catch (AlreadyExistsException e) {
            responseObserver.onError(
                    Status.ALREADY_EXISTS.withDescription(e.getMessage()).asRuntimeException()
            );
            return;
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException()
            );
            return;
        }
        CreateLaptopResponse response = CreateLaptopResponse.newBuilder().setId(other.getId()).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();

        logger.info("Saved laptop with ID: " + other.getId());
    }
}