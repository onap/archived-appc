package org.onap.appc.dg.common.impl;

public class DgResolverException extends RuntimeException{

    public DgResolverException(String message) {
        super(message);
    }

    public DgResolverException(Throwable cause) {
        super(cause);
    }
}
