package com.github.buiminhhai.pcbook.service;

import com.github.buiminhhai.pcbook.pb.Filter;
import com.github.buiminhhai.pcbook.pb.Laptop;
import io.grpc.Context;

public interface LaptopStore {
    void Save(Laptop laptop) throws Exception, AlreadyExistsException;
    Laptop Find(String id);
    void Search(Context ctx, Filter filter, LaptopStream stream);
}

