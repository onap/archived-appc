/*-
 * ============LICENSE_START=======================================================
 * APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.openecomp.appc.executor.objects;


import org.apache.commons.lang3.StringUtils;


public class UniqueRequestIdentifier {
    private static final String IDENTIFIER_DELIMITER = "@";

    private String originatorID;
    private String requestID;
    private String subRequestID;

    private UniqueRequestIdentifier(){

    }
    public UniqueRequestIdentifier(String originatorID,
                                   String requestID,
                                   String subRequestID) {
        this();
        this.originatorID = originatorID;
        this.requestID = requestID;
        this.subRequestID = subRequestID;
    }

    public String toIdentifierString(){
        StringBuilder stringBuilder = new StringBuilder();
        if(originatorID != null){
            stringBuilder.append(originatorID);
        }
        stringBuilder.append(IDENTIFIER_DELIMITER);

        if(requestID != null){
            stringBuilder.append(requestID);
        }
        stringBuilder.append(IDENTIFIER_DELIMITER);

        if(subRequestID != null){
            stringBuilder.append(subRequestID);
        }
        return stringBuilder.toString();
    }

    public static UniqueRequestIdentifier getUniqueRequestIdentifier(String identifierString){
        String[] splitIdentifier = identifierString.split(IDENTIFIER_DELIMITER);
        if(splitIdentifier == null || splitIdentifier.length <2){
            throw new IllegalArgumentException("input identifierString is not valid "+identifierString);
        }
        String originatorID = splitIdentifier[0];
        String requestID = StringUtils.isEmpty(splitIdentifier[1])? null :splitIdentifier[1];
        String subRequestID = splitIdentifier.length>=3 ? splitIdentifier[2] : null;
        return new UniqueRequestIdentifier(originatorID,requestID,subRequestID);
    }
    public String toString(){
        return "originatorID = " + originatorID +
                " , requestID = " + requestID +
                " , subRequestID = " + subRequestID;
    }
    @Override
    public boolean equals(Object obj){
        if(obj ==null){
            return false;
        }
        if(!(obj instanceof UniqueRequestIdentifier)){
            return false;
        }
        UniqueRequestIdentifier identifier = (UniqueRequestIdentifier)obj;
        if(this.originatorID == null){
            if(identifier.originatorID !=null)
                return false;
        }
        else if(!this.originatorID.equals(identifier.originatorID))
            return false;

        if(this.requestID == null){
            if(identifier.requestID !=null)
                return false;
        }
        else if(!this.requestID.equals(identifier.requestID))
            return false;

        if(this.subRequestID == null){
            if(identifier.subRequestID !=null)
                return false;
        }
        else if(!this.subRequestID.equals(identifier.subRequestID))
            return false;

        return true;
    }
    @Override
    public int hashCode(){
        final int prime = 31;
        int result = 1;
        result = result * prime + (this.originatorID == null ? 0 :this.originatorID.hashCode());
        result = result * prime + (this.requestID == null ? 0 :this.requestID.hashCode());
        result = result * prime + (this.subRequestID == null ? 0 :this.subRequestID.hashCode());
        return result;
    }


}
