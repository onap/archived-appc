package org.onap.appc.artifact.handler.node;

class ArtifactHandlerInternalException extends Exception{

    ArtifactHandlerInternalException(String message, Throwable cause) {
        super(message, cause);
    }

    ArtifactHandlerInternalException(String message) {
        super(message);
    }

    ArtifactHandlerInternalException(Throwable cause) {
        super(cause);
    }
}
