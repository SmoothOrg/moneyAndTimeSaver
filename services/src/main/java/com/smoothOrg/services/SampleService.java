package com.smoothOrg.services;

import com.smoothOrg.domain.SampleDomain;

public class SampleService {
    public SampleDomain create(String value) {
        return new SampleDomain(value);
    }
}
