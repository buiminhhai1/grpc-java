package com.github.buiminhhai.pcbook.service;

import com.github.buiminhhai.pcbook.pb.CreateLaptopRequest;
import com.github.buiminhhai.pcbook.pb.CreateLaptopResponse;
import com.github.buiminhhai.pcbook.pb.Laptop;
import com.github.buiminhhai.pcbook.pb.LaptopServiceGrpc;
import com.github.buiminhhai.pcbook.sample.Generator;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

public class LaptopServerTest {

    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule(); // automatic graceful shutdown channel at the end of test

    private LaptopStore store;
    private LaptopServer server;
    private ManagedChannel channel;

    @Before
    public void setUp() throws Exception {
        String serverName = InProcessServerBuilder.generateName();
        InProcessServerBuilder serverBuilder = InProcessServerBuilder.forName(serverName).directExecutor();

        store = new InMemoryLaptopStore();
        server = new LaptopServer(serverBuilder, 0, store);
        server.start();

        channel = InProcessChannelBuilder.forName(serverName).directExecutor().build();
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void createLaptopWithAValidID() {
        Generator generator = new Generator();
        Laptop laptop = generator.NewLaptop();
        CreateLaptopRequest request = CreateLaptopRequest.newBuilder().setLaptop(laptop).build();

        LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
        CreateLaptopResponse response = stub.createLaptop(request);
        assertNotNull(response);
        assertEquals(laptop.getId(), response.getId());

        Laptop found = store.Find(response.getId());
        assertNotNull(found);
    }

    @Test
    public void createLaptopWithEmptyID() {
        Generator generator = new Generator();
        Laptop laptop = generator.NewLaptop().toBuilder().setId("").build();
        CreateLaptopRequest request = CreateLaptopRequest.newBuilder().setLaptop(laptop).build();

        LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
        CreateLaptopResponse response = stub.createLaptop(request);
        assertNotNull(response);
        assertFalse(response.getId().isEmpty());

        Laptop found = store.Find(response.getId());
        assertNotNull(found);
    }

    @Test(expected = StatusRuntimeException.class)
    public void createLaptopWithInvalidID() {
        Generator generator = new Generator();
        Laptop laptop = generator.NewLaptop().toBuilder().setId("Invalid").build();
        CreateLaptopRequest request = CreateLaptopRequest.newBuilder().setLaptop(laptop).build();

        LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
        CreateLaptopResponse response = stub.createLaptop(request);
        assertNull(response);
        assertNull(response.getId());
    }

    @Test(expected = StatusRuntimeException.class)
    public void createLaptopWithAlreadyExistsID() throws AlreadyExistsException, Exception {
        Generator generator = new Generator();
        Laptop laptop = generator.NewLaptop();
        store.Save(laptop);
        CreateLaptopRequest request = CreateLaptopRequest.newBuilder().setLaptop(laptop).build();

        LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
        CreateLaptopResponse response = stub.createLaptop(request);
    }
}