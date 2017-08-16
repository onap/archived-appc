/*-
 * ============LICENSE_START=======================================================
 * ONAP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property.  All rights reserved.
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
 */

package org.openecomp.appc.ccadaptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlUtil
{

  private static final Logger log = LoggerFactory.getLogger(XmlUtil.class);

  public static String getXml(Map<String, String> varmap, String var)
  {
    Object o = createStructure(varmap, var);
    return generateXml(o, 0);
  }

  private static Object createStructure(Map<String, String> flatmap, String var)
  {
    if (flatmap.containsKey(var))
      return flatmap.get(var);

    Map<String, Object> mm = new HashMap<>();
    for (String k : flatmap.keySet())
      if (k.startsWith(var + "."))
      {
        int i1 = k.indexOf('.', var.length() + 1);
        int i2 = k.indexOf('[', var.length() + 1);
        int i3 = k.length();
        if (i1 > 0 && i1 < i3)
          i3 = i1;
        if (i2 > 0 && i2 < i3)
          i3 = i2;
        String k1 = k.substring(var.length() + 1, i3);
        String var1 = k.substring(0, i3);
        if (!mm.containsKey(k1))
        {
          Object str = createStructure(flatmap, var1);
          if (str != null && (!(str instanceof String) || ((String) str).trim().length() > 0))
            mm.put(k1, str);
        }
      }
    if (!mm.isEmpty())
      return mm;

    boolean arrayFound = false;
    for (String k : flatmap.keySet())
      if (k.startsWith(var + "["))
      {
        arrayFound = true;
        break;
      }

    if (arrayFound)
    {
      List<Object> ll = new ArrayList<>();

      int length = Integer.MAX_VALUE;
      String lengthStr = flatmap.get(var + "_length");
      if (lengthStr != null)
      {
        try
        {
          length = Integer.parseInt(lengthStr);
        }
        catch (Exception e)
        {
          log.warn("Invalid number for " + var + "_length:" + lengthStr);
        }
      }

      for (int i = 0; i < length; i++)
      {
        Object v = createStructure(flatmap, var + '[' + i + ']');
        if (v == null)
          break;
        ll.add(v);
      }

      if (!ll.isEmpty())
        return ll;
    }

    return null;
  }

  @SuppressWarnings("unchecked")
  private static String generateXml(Object o, int indent)
  {
    if (o == null)
      return null;

    if (o instanceof String)
      return (String) o;

    if (o instanceof Map)
    {
      StringBuilder ss = new StringBuilder();
      Map<String, Object> mm = (Map<String, Object>) o;
      for (String k : mm.keySet())
      {
        Object v = mm.get(k);
        if (v instanceof String)
        {
          ss.append(pad(indent)).append('<').append(k).append('>');
          ss.append(v);
          ss.append("</").append(k).append('>').append('\n');
        }
        else if (v instanceof Map)
        {
          ss.append(pad(indent)).append('<').append(k).append('>').append('\n');
          ss.append(generateXml(v, indent + 1));
          ss.append(pad(indent)).append("</").append(k).append('>').append('\n');
        }
        else if (v instanceof List)
        {
          List<Object> ll = (List<Object>) v;
          for (Object o1 : ll)
          {
            ss.append(pad(indent)).append('<').append(k).append('>').append('\n');
            ss.append(generateXml(o1, indent + 1));
            ss.append(pad(indent)).append("</").append(k).append('>').append('\n');
          }
        }
      }
      return ss.toString();
    }

    return null;
  }

  private static String pad(int n)
  {
    String s = "";
    for (int i = 0; i < n; i++)
      s += '\t';
    return s;
  }
}
