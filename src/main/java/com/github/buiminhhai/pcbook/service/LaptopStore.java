package com.github.buiminhhai.pcbook.service;

import com.github.buiminhhai.pcbook.pb.Laptop;

public interface LaptopStore {
    void Save(Laptop laptop) throws Exception, AlreadyExistsException;
    Laptop Find(String id);
}
